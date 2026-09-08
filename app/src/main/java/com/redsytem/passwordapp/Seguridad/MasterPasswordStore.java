package com.redsytem.passwordapp.Seguridad;

import android.content.SharedPreferences;
import android.util.Base64;

import java.util.Arrays;

/**
 * Stores and verifies the master password without persisting the password itself.
 *
 * The verifier uses PBKDF2-HMAC-SHA256 with a random per-install salt.
 * A one-time migration path is kept for V1 installations that still contain
 * the legacy plaintext value under "password".
 */
public final class MasterPasswordStore {

    public static final String SHARED_PREF = "mi_pref";

    private static final String KEY_PASSWORD_HASH = "master_password_hash";
    private static final String KEY_PASSWORD_SALT = "master_password_salt";
    private static final String KEY_PASSWORD_ITERATIONS = "master_password_iterations";

    // Legacy V1 keys. They are only read during migration and are removed afterwards.
    private static final String LEGACY_KEY_PASSWORD = "password";
    private static final String LEGACY_KEY_CONFIRM_PASSWORD = "c_password";

    private static final int PBKDF2_ITERATIONS = MasterPasswordHasher.PBKDF2_ITERATIONS;

    private MasterPasswordStore() {
        // Utility class.
    }

    /**
     * Creates a new verifier and removes any legacy plaintext master password.
     */
    public static void save(SharedPreferences preferences, String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña maestra no puede estar vacía");
        }

        byte[] salt = MasterPasswordHasher.generateSalt();
        byte[] hash = MasterPasswordHasher.hash(password, salt);

        preferences.edit()
                .putString(KEY_PASSWORD_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
                .putString(KEY_PASSWORD_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putInt(KEY_PASSWORD_ITERATIONS, PBKDF2_ITERATIONS)
                .remove(LEGACY_KEY_PASSWORD)
                .remove(LEGACY_KEY_CONFIRM_PASSWORD)
                .apply();

        Arrays.fill(hash, (byte) 0);
        Arrays.fill(salt, (byte) 0);
    }

    /**
     * Verifies the supplied password. Existing V1 plaintext storage is upgraded
     * automatically after the first successful login.
     */
    public static boolean verify(SharedPreferences preferences, String candidate) {
        if (candidate == null || candidate.isEmpty()) {
            return false;
        }

        String encodedHash = preferences.getString(KEY_PASSWORD_HASH, null);
        String encodedSalt = preferences.getString(KEY_PASSWORD_SALT, null);

        if (encodedHash != null && encodedSalt != null) {
            return verifyStoredHash(preferences, candidate, encodedHash, encodedSalt);
        }

        // V1 compatibility: migrate only after proving knowledge of the old password.
        String legacyPassword = preferences.getString(LEGACY_KEY_PASSWORD, null);
        if (legacyPassword != null && constantTimeEquals(candidate, legacyPassword)) {
            save(preferences, candidate);
            return true;
        }

        return false;
    }

    /**
     * Returns whether a password verifier has already been configured.
     * Legacy V1 installations count as configured as well.
     */
    public static boolean isConfigured(SharedPreferences preferences) {
        return preferences.contains(KEY_PASSWORD_HASH)
                || preferences.contains(LEGACY_KEY_PASSWORD);
    }

    private static boolean verifyStoredHash(
            SharedPreferences preferences,
            String candidate,
            String encodedHash,
            String encodedSalt) {
        try {
            byte[] expectedHash = Base64.decode(encodedHash, Base64.DEFAULT);
            byte[] salt = Base64.decode(encodedSalt, Base64.DEFAULT);
            int iterations = preferences.getInt(KEY_PASSWORD_ITERATIONS, PBKDF2_ITERATIONS);

            if (iterations <= 0) {
                return false;
            }

            if (iterations != PBKDF2_ITERATIONS) {
                // Current V1 verifier format is intentionally fixed; unsupported parameters fail closed.
                return false;
            }

            boolean matches = MasterPasswordHasher.verify(candidate, salt, expectedHash);

            Arrays.fill(expectedHash, (byte) 0);
            Arrays.fill(salt, (byte) 0);
            return matches;
        } catch (IllegalArgumentException e) {
            // Corrupted verifier data must fail closed.
            return false;
        }
    }

    private static boolean constantTimeEquals(String left, String right) {
        return MasterPasswordHasher.constantTimeStringEquals(left, right);
    }

}
