# Architecture Notes

PoliPass V1 uses a deliberately lightweight Android architecture built around Activities, Fragments, a SQLite helper, and a small encryption utility.

## Main data flow

```text
User input
   │
   ├── Credential form ──> BDHelper ──> SQLite
   │                         │
   │                         └── password field ──> Encrypt ──> Android Keystore
   │
   ├── Login ─────────────> SharedPreferences (V1 limitation)
   │                         │
   │                         └── optional BiometricPrompt
   │
   └── Settings ──────────> import/export, recovery, failed attempts
```

## Cryptography boundary

`Encrypt.java` generates an AES key in Android Keystore and uses `AES/GCM/NoPadding`. Encryption obtains a fresh IV from the `Cipher`, stores the IV together with the ciphertext, and authenticates the encrypted payload using GCM.

This protects the password field at rest from simple SQLite inspection, but it does **not** protect the entire record and it does not solve the master-password storage problem.

## Database boundary

`BDHelper` owns the SQLite schema and CRUD operations. `Password` is the data model used by UI components. Database version upgrades currently drop and recreate the table; a future migration layer is required before treating the data model as stable.

## Authentication boundary

`Logeo_usuario` delegates master-password verification to `MasterPasswordStore` and invokes `BiometricPrompt`. The master password itself is never persisted. `MasterPasswordStore` stores a PBKDF2-HMAC-SHA256 verifier, a random salt, and the iteration count.

For existing V1 installations, the store can migrate the legacy plaintext `password` value after a successful login and then removes the legacy keys.

## Autofill boundary

`PasswordAutofillService` registers an Android Autofill Service but its fill/save callbacks are not fully implemented. The service therefore represents framework integration work rather than a complete credential-provider implementation.

## Portfolio review focus

A useful code-review sequence is:

1. `Encrypt.java` — cryptographic primitive and Keystore usage
2. `BDHelper.java` — data persistence and encryption integration
3. `Logeo_usuario.java` — authentication and recovery behavior
4. `F_Ajustes.java` — security settings and export/import behavior
5. `PasswordAutofillService.java` — framework integration status


## Verificación de la contraseña maestra

El flujo de autenticación usa `MasterPasswordStore` y `MasterPasswordHasher`. El almacenamiento contiene `master_password_hash`, `master_password_salt` y `master_password_iterations`; la contraseña maestra original no se persiste.

Para instalaciones heredadas de V1, el repositorio conserva una ruta de migración de una sola vez: valida el valor `password`, genera el nuevo verificador y elimina `password` y `c_password`.

## Recovery architecture (V2+)

Recovery answers are authentication factors, not encryption keys. Their values are never persisted in plaintext: each answer uses its own random salt and PBKDF2-HMAC-SHA256 verifier.

A successful recovery does not reconstruct the master password. Instead, it proves control of the configured recovery factors and lets the user create a new master-password verifier. The vault encryption key remains the Android Keystore key already used by the existing database encryption layer.

This separation means changing or recovering the master password does not require decrypting and re-encrypting every vault record.
