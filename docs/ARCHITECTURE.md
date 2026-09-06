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

`Logeo_usuario` performs master-password comparison and invokes `BiometricPrompt`. In V1, the master password is retrieved from `SharedPreferences`, which is the primary authentication/security debt to address in V2.

## Autofill boundary

`PasswordAutofillService` registers an Android Autofill Service but its fill/save callbacks are not fully implemented. The service therefore represents framework integration work rather than a complete credential-provider implementation.

## Portfolio review focus

A useful code-review sequence is:

1. `Encrypt.java` — cryptographic primitive and Keystore usage
2. `BDHelper.java` — data persistence and encryption integration
3. `Logeo_usuario.java` — authentication and recovery behavior
4. `F_Ajustes.java` — security settings and export/import behavior
5. `PasswordAutofillService.java` — framework integration status
