package com.redsytem.passwordapp.Seguridad;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Pure-Java authenticated encryption format for PoliPass portable backups. */
public final class VaultBackupCrypto {

    public static final int FORMAT_VERSION = 1;
    public static final int SALT_LENGTH = 16;
    public static final int IV_LENGTH = 12;
    public static final int TAG_LENGTH_BITS = 128;
    public static final int MIN_PASSWORD_LENGTH = 8;

    private static final int MAGIC = 0x5050424B; // PPBK
    private static final byte[] AAD = "PoliPass/backup/v1".getBytes(StandardCharsets.UTF_8);
    private static final SecureRandom RANDOM = new SecureRandom();

    private VaultBackupCrypto() {
    }

    public static byte[] encrypt(byte[] plaintext, String password) throws Exception {
        requirePassword(password);
        if (plaintext == null) {
            throw new IllegalArgumentException("Contenido de respaldo nulo");
        }

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] keyBytes = MasterPasswordHasher.hash(password, salt);
        byte[] iv = null;
        byte[] ciphertext = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
            cipher.updateAAD(AAD);
            iv = cipher.getIV();
            ciphertext = cipher.doFinal(plaintext);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream(16 + salt.length + iv.length + ciphertext.length);
            DataOutputStream out = new DataOutputStream(buffer);
            out.writeInt(MAGIC);
            out.writeInt(FORMAT_VERSION);
            out.writeByte(salt.length);
            out.writeByte(iv.length);
            out.writeInt(ciphertext.length);
            out.write(salt);
            out.write(iv);
            out.write(ciphertext);
            out.flush();
            return buffer.toByteArray();
        } finally {
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(keyBytes, (byte) 0);
            if (iv != null) Arrays.fill(iv, (byte) 0);
            if (ciphertext != null) Arrays.fill(ciphertext, (byte) 0);
        }
    }

    public static byte[] decrypt(byte[] backup, String password) throws Exception {
        requirePassword(password);
        if (backup == null) {
            throw new IllegalArgumentException("Archivo de respaldo nulo");
        }

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(backup))) {
            int magic = in.readInt();
            int version = in.readInt();
            int saltLength = in.readUnsignedByte();
            int ivLength = in.readUnsignedByte();
            int ciphertextLength = in.readInt();

            if (magic != MAGIC || version != FORMAT_VERSION
                    || saltLength != SALT_LENGTH
                    || ivLength != IV_LENGTH
                    || ciphertextLength < TAG_LENGTH_BITS / 8
                    || ciphertextLength > 25_000_000) {
                throw new IllegalArgumentException("Archivo de respaldo inválido o incompatible");
            }

            int expectedLength = 4 + 4 + 1 + 1 + 4 + saltLength + ivLength + ciphertextLength;
            if (backup.length != expectedLength) {
                throw new IllegalArgumentException("Archivo de respaldo incompleto o manipulado");
            }

            byte[] salt = new byte[saltLength];
            byte[] iv = new byte[ivLength];
            byte[] ciphertext = new byte[ciphertextLength];
            in.readFully(salt);
            in.readFully(iv);
            in.readFully(ciphertext);

            byte[] keyBytes = MasterPasswordHasher.hash(password, salt);
            try {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
                cipher.updateAAD(AAD);
                return cipher.doFinal(ciphertext);
            } catch (Exception e) {
                throw new SecurityException("La contraseña de respaldo es incorrecta o el archivo fue alterado");
            } finally {
                Arrays.fill(salt, (byte) 0);
                Arrays.fill(iv, (byte) 0);
                Arrays.fill(ciphertext, (byte) 0);
                Arrays.fill(keyBytes, (byte) 0);
            }
        }
    }

    private static void requirePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("La contraseña de respaldo debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
    }
}
