package com.redsytem.passwordapp.Seguridad;

import android.content.SharedPreferences;
import android.util.Base64;

import java.util.Arrays;
import java.util.Locale;

/**
 * Stores recovery answers as password verifiers instead of plaintext values.
 * Recovery is an authentication factor only; it never derives the vault key.
 */
public final class RecoveryAnswerStore {

    public static final String SHARED_PREF = "MyPrefs";

    private static final String[] HASH_KEYS = {
            "recovery_answer_hash_1", "recovery_answer_hash_2", "recovery_answer_hash_3"
    };
    private static final String[] SALT_KEYS = {
            "recovery_answer_salt_1", "recovery_answer_salt_2", "recovery_answer_salt_3"
    };
    private static final String[] LEGACY_KEYS = {
            "RespuestaUno", "RespuestaDos", "RespuestaTres"
    };

    private RecoveryAnswerStore() {
    }

    public static void saveAnswer(SharedPreferences preferences, int index, String answer) {
        validateIndex(index);
        String normalized = normalize(answer);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("La respuesta no puede estar vacía");
        }

        byte[] salt = MasterPasswordHasher.generateSalt();
        byte[] hash = MasterPasswordHasher.hash(normalized, salt);

        preferences.edit()
                .putString(HASH_KEYS[index], Base64.encodeToString(hash, Base64.NO_WRAP))
                .putString(SALT_KEYS[index], Base64.encodeToString(salt, Base64.NO_WRAP))
                .remove(LEGACY_KEYS[index])
                .apply();

        Arrays.fill(hash, (byte) 0);
        Arrays.fill(salt, (byte) 0);
    }

    public static boolean verifyAnswer(SharedPreferences preferences, int index, String candidate) {
        validateIndex(index);
        String normalized = normalize(candidate);
        if (normalized.isEmpty()) {
            return false;
        }

        String encodedHash = preferences.getString(HASH_KEYS[index], null);
        String encodedSalt = preferences.getString(SALT_KEYS[index], null);

        if (encodedHash != null && encodedSalt != null) {
            try {
                byte[] expectedHash = Base64.decode(encodedHash, Base64.DEFAULT);
                byte[] salt = Base64.decode(encodedSalt, Base64.DEFAULT);
                boolean matches = MasterPasswordHasher.verify(normalized, salt, expectedHash);
                Arrays.fill(expectedHash, (byte) 0);
                Arrays.fill(salt, (byte) 0);
                return matches;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        // One-time migration for old V1 installations that stored answers in plaintext.
        String legacy = preferences.getString(LEGACY_KEYS[index], null);
        if (legacy != null && MasterPasswordHasher.constantTimeStringEquals(normalized, normalize(legacy))) {
            saveAnswer(preferences, index, normalized);
            return true;
        }

        return false;
    }

    public static void clearAnswer(SharedPreferences preferences, int index) {
        validateIndex(index);
        preferences.edit()
                .remove(HASH_KEYS[index])
                .remove(SALT_KEYS[index])
                .remove(LEGACY_KEYS[index])
                .apply();
    }

    public static boolean isConfigured(SharedPreferences preferences, int index) {
        validateIndex(index);
        return preferences.contains(HASH_KEYS[index]) || preferences.contains(LEGACY_KEYS[index]);
    }

    public static String normalize(String answer) {
        if (answer == null) {
            return "";
        }
        return answer.trim().toLowerCase(Locale.ROOT);
    }

    private static void validateIndex(int index) {
        if (index < 0 || index >= HASH_KEYS.length) {
            throw new IllegalArgumentException("Índice de respuesta inválido");
        }
    }
}
