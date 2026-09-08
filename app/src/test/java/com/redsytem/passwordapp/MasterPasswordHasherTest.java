package com.redsytem.passwordapp;

import com.redsytem.passwordapp.Seguridad.MasterPasswordHasher;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MasterPasswordHasherTest {

    @Test
    public void samePasswordAndSaltProduceSameHash() {
        byte[] salt = MasterPasswordHasher.generateSalt();

        byte[] first = MasterPasswordHasher.hash("PoliPass-demo-123", salt);
        byte[] second = MasterPasswordHasher.hash("PoliPass-demo-123", salt);

        assertArrayEquals(first, second);
    }

    @Test
    public void differentPasswordsDoNotVerify() {
        byte[] salt = MasterPasswordHasher.generateSalt();
        byte[] hash = MasterPasswordHasher.hash("CorrectPassword123", salt);

        assertTrue(MasterPasswordHasher.verify("CorrectPassword123", salt, hash));
        assertFalse(MasterPasswordHasher.verify("WrongPassword123", salt, hash));
    }

    @Test
    public void saltsAreRandom() {
        byte[] firstSalt = MasterPasswordHasher.generateSalt();
        byte[] secondSalt = MasterPasswordHasher.generateSalt();

        assertNotEquals(java.util.Arrays.toString(firstSalt), java.util.Arrays.toString(secondSalt));
    }
}
