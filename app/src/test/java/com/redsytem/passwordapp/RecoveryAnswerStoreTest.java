package com.redsytem.passwordapp;

import com.redsytem.passwordapp.Seguridad.MasterPasswordHasher;
import com.redsytem.passwordapp.Seguridad.RecoveryAnswerStore;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RecoveryAnswerStoreTest {

    @Test
    public void answersAreNormalizedForUserFriendlyVerification() {
        assertEquals("juan carlos", RecoveryAnswerStore.normalize("  Juan Carlos  "));
        assertEquals("mexico", RecoveryAnswerStore.normalize("MEXICO"));
    }

    @Test
    public void normalizedAnswerCanBeVerifiedWithPbkdf2() {
        String answer = RecoveryAnswerStore.normalize("  Blue  ");
        byte[] salt = MasterPasswordHasher.generateSalt();
        byte[] hash = MasterPasswordHasher.hash(answer, salt);

        assertTrue(MasterPasswordHasher.verify(RecoveryAnswerStore.normalize("BLUE"), salt, hash));
        assertFalse(MasterPasswordHasher.verify(RecoveryAnswerStore.normalize("GREEN"), salt, hash));
    }
}
