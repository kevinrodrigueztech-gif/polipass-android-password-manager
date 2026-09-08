package com.redsytem.passwordapp.Seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Pure-Java password hashing primitives used by the Android storage layer. */
public final class MasterPasswordHasher {

    public static final int SALT_LENGTH_BYTES = 16;
    public static final int HASH_LENGTH_BITS = 256;
    public static final int PBKDF2_ITERATIONS = 600_000;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private MasterPasswordHasher() {
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] hash(String password, byte[] salt) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña maestra no puede estar vacía");
        }
        if (salt == null || salt.length != SALT_LENGTH_BYTES) {
            throw new IllegalArgumentException("Salt inválido");
        }

        PBEKeySpec keySpec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                PBKDF2_ITERATIONS,
                HASH_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(keySpec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo derivar el verificador de contraseña", e);
        } finally {
            keySpec.clearPassword();
        }
    }

    public static boolean verify(String password, byte[] salt, byte[] expectedHash) {
        if (password == null || salt == null || expectedHash == null) {
            return false;
        }

        byte[] actualHash = hash(password, salt);
        try {
            return MessageDigest.isEqual(expectedHash, actualHash);
        } finally {
            Arrays.fill(actualHash, (byte) 0);
        }
    }

    public static boolean constantTimeStringEquals(String left, String right) {
        if (left == null || right == null) {
            return left == right;
        }
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8));
    }
}
