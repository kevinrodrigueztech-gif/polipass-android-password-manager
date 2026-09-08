package com.redsytem.passwordapp.Encriptacion;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class Encrypt {
    private static final String KEYSTORE_ALIAS = "ESIMEZ";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final byte[] RECORD_AAD = "PoliPass/vault-record/v1".getBytes(StandardCharsets.UTF_8);

    private Encrypt() {
    }

    public static void initializeKeyStore() {
        if (getSecretKey() == null) {
            generateAndStoreKey();
        }
    }

    public static String encrypt(String text, boolean shouldEncrypt) throws Exception {
        SecretKey secretKey;
        if (shouldEncrypt) {
            initializeKeyStore();
            secretKey = getSecretKey();
        } else {
            secretKey = getSecretKey();
        }
        if (secretKey == null) {
            throw new Exception("Clave secreta no disponible");
        }

        if (shouldEncrypt) {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            byte[] encryptedBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, combined, IV_LENGTH, encryptedBytes.length);
            return Base64.encodeToString(combined, Base64.NO_WRAP);
        }

        byte[] combined = Base64.decode(text, Base64.NO_WRAP);
        if (combined.length <= IV_LENGTH) {
            throw new IllegalArgumentException("Datos cifrados inválidos");
        }

        byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
        byte[] encryptedBytes = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static String encryptRecord(String record) throws Exception {
        initializeKeyStore();
        SecretKey secretKey = getSecretKey();
        if (secretKey == null) {
            throw new Exception("Clave secreta no disponible");
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        cipher.updateAAD(RECORD_AAD);
        byte[] iv = cipher.getIV();
        byte[] encryptedBytes = cipher.doFinal(record.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[IV_LENGTH + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encryptedBytes, 0, combined, IV_LENGTH, encryptedBytes.length);
        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    public static String decryptRecord(String encryptedRecord) throws Exception {
        SecretKey secretKey = getSecretKey();
        if (secretKey == null) {
            throw new Exception("Clave secreta no disponible");
        }
        byte[] combined = Base64.decode(encryptedRecord, Base64.NO_WRAP);
        if (combined.length <= IV_LENGTH) {
            throw new IllegalArgumentException("Registro cifrado inválido");
        }
        byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
        byte[] encryptedBytes = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        cipher.updateAAD(RECORD_AAD);
        return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
    }

    public static void generateAndStoreKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES);
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar la clave de la bóveda", e);
        }
    }

    public static SecretKey getSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                return null;
            }
            KeyStore.Entry entry = keyStore.getEntry(KEYSTORE_ALIAS, null);
            if (!(entry instanceof KeyStore.SecretKeyEntry)) {
                return null;
            }
            return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
        } catch (Exception e) {
            return null;
        }
    }
}
