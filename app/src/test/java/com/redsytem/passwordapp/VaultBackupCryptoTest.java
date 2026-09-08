package com.redsytem.passwordapp;

import com.redsytem.passwordapp.Seguridad.VaultBackupCrypto;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class VaultBackupCryptoTest {

    @Test
    public void roundTripRestoresPlaintext() throws Exception {
        byte[] source = "PoliPass confidential vault payload".getBytes(StandardCharsets.UTF_8);
        byte[] backup = VaultBackupCrypto.encrypt(source, "correct-horse-battery");
        byte[] restored = VaultBackupCrypto.decrypt(backup, "correct-horse-battery");
        assertArrayEquals(source, restored);
    }

    @Test
    public void wrongPasswordFailsWithoutReturningPlaintext() throws Exception {
        byte[] backup = VaultBackupCrypto.encrypt(
                "secreto".getBytes(StandardCharsets.UTF_8),
                "correct-horse-battery");
        try {
            VaultBackupCrypto.decrypt(backup, "incorrect-password");
            throw new AssertionError("Se esperaba fallo de autenticación");
        } catch (SecurityException expected) {
            assertTrue(expected.getMessage().contains("incorrecta"));
        }
    }

    @Test
    public void tamperingFailsIntegrityCheck() throws Exception {
        byte[] backup = VaultBackupCrypto.encrypt(
                "integrity".getBytes(StandardCharsets.UTF_8),
                "correct-horse-battery");
        backup[backup.length - 1] ^= 0x01;
        try {
            VaultBackupCrypto.decrypt(backup, "correct-horse-battery");
            throw new AssertionError("Se esperaba fallo de integridad");
        } catch (SecurityException expected) {
            assertTrue(expected.getMessage().contains("alterado"));
        }
    }

    @Test
    public void everyBackupUsesDifferentRandomSaltOrIv() throws Exception {
        byte[] first = VaultBackupCrypto.encrypt(
                "same payload".getBytes(StandardCharsets.UTF_8),
                "correct-horse-battery");
        byte[] second = VaultBackupCrypto.encrypt(
                "same payload".getBytes(StandardCharsets.UTF_8),
                "correct-horse-battery");
        assertTrue(!Arrays.equals(first, second));
    }
}
