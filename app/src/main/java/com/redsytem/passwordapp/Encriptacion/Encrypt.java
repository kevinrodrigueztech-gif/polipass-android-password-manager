package com.redsytem.passwordapp.Encriptacion;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
public class Encrypt {
    private static final String KEYSTORE_ALIAS = "ESIMEZ";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    // Generar y almacenar la clave si no está generada
    public static void initializeKeyStore() {
        if (getSecretKey() == null) {
            generateAndStoreKey();
        }
    }

    public static String encrypt( String password, boolean opcion) throws Exception {
        initializeKeyStore();

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKey secretKey = getSecretKey();

        if (secretKey == null) {
            throw new Exception("Clave secreta no disponible");
        }

        if (opcion) {
            // Encriptar
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            byte[] encryptedBytes = cipher.doFinal(password.getBytes("UTF-8"));

            byte[] combinedIvAndCipherText = new byte[IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, combinedIvAndCipherText, 0, IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, combinedIvAndCipherText, IV_LENGTH, encryptedBytes.length);

            return Base64.encodeToString(combinedIvAndCipherText, Base64.DEFAULT);
        } else {
            // Desencriptar
            byte[] combinedIvAndCipherText = Base64.decode(password, Base64.DEFAULT);

            // Extraer el IV (Initialization Vector)
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combinedIvAndCipherText, 0, iv, 0, IV_LENGTH);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Desencriptar los bytes cifrados (sin el IV)
            byte[] encryptedBytes = new byte[combinedIvAndCipherText.length - IV_LENGTH];
            System.arraycopy(combinedIvAndCipherText, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, "UTF-8");
        }
    }
    public static void generateAndStoreKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES);

            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256)
                    .build();

            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SecretKey getSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEYSTORE_ALIAS, null)).getSecretKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
