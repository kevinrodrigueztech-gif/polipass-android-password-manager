# Architecture Notes

PoliPass uses a deliberately lightweight Android architecture built around Activities, Fragments, a SQLite helper, and a small encryption utility.

## Main data flow

```text
User input
   │
   ├── Credential form ──> BDHelper ──> SQLite
   │                         │
   │                         └── full record ──> Encrypt ──> Android Keystore
   │
   ├── Login ─────────────> SharedPreferences (verifier storage)
   │                         │
   │                         └── optional BiometricPrompt
   │
   └── Settings ──────────> backup/restore, recovery, failed attempts
```

## Cryptography boundary

`Encrypt.java` generates an AES key in Android Keystore and uses `AES/GCM/NoPadding`. Encryption obtains a fresh IV from the `Cipher`, stores the IV together with the ciphertext, and authenticates the encrypted payload using GCM.

This protects the complete record from simple SQLite inspection while keeping the vault key outside SQLite. The master password is handled separately as a verifier.

## Database boundary

`BDHelper` owns the SQLite schema and CRUD operations. `Password` is the data model used by UI components. Database version upgrades currently drop and recreate the table; a future migration layer is required before treating the data model as stable.

## Authentication boundary

`Logeo_usuario` delegates master-password verification to `MasterPasswordStore` and invokes `BiometricPrompt`. The master password itself is never persisted. `MasterPasswordStore` stores a PBKDF2-HMAC-SHA256 verifier, a random salt, and the iteration count.

For existing V1 installations, the store can migrate the legacy plaintext `password` value after a successful login and then removes the legacy keys.

## Autofill

Autofill is intentionally deferred. The previous partial service was removed from the manifest so the current release does not advertise an incomplete credential-provider surface. A future implementation should define authorization, vault-unlock behavior, field parsing, dataset creation, and save flows before being enabled.

## Portfolio review focus

A useful code-review sequence is:

1. `Encrypt.java` — cryptographic primitive and Keystore usage
2. `BDHelper.java` — data persistence and encryption integration
3. `Logeo_usuario.java` — authentication and recovery behavior
4. `F_Ajustes.java` — security settings and encrypted backup/restore behavior


## Verificación de la contraseña maestra

El flujo de autenticación usa `MasterPasswordStore` y `MasterPasswordHasher`. El almacenamiento contiene `master_password_hash`, `master_password_salt` y `master_password_iterations`; la contraseña maestra original no se persiste.

Para instalaciones heredadas de V1, el repositorio conserva una ruta de migración de una sola vez: valida el valor `password`, genera el nuevo verificador y elimina `password` y `c_password`.

## Recovery architecture (V2+)

Recovery answers are authentication factors, not encryption keys. Their values are never persisted in plaintext: each answer uses its own random salt and PBKDF2-HMAC-SHA256 verifier.

A successful recovery does not reconstruct the master password. Instead, it proves control of the configured recovery factors and lets the user create a new master-password verifier. The vault encryption key remains the Android Keystore key already used by the existing database encryption layer.

This separation means changing or recovering the master password does not require decrypting and re-encrypting the vault records.


## Rendimiento de autenticación

Las operaciones PBKDF2 son deliberadamente costosas. `SecurityTaskRunner` las ejecuta en un `ExecutorService` de fondo para que login, cambio de contraseña, recuperación y registro no bloqueen el hilo principal. La interfaz deshabilita temporalmente la acción mientras se calcula el verificador.

## Cifrado completo de la bóveda (V4)

Los registros de la bóveda ya no almacenan en claro título, cuenta, usuario, contraseña, sitio web ni notas.

Cada registro se serializa como un objeto JSON y se cifra con **AES-256-GCM** usando una clave almacenada en **Android Keystore**. El ciphertext se guarda en `REGISTRO_CIFRADO`; `ID`, `TIEMPO_REGISTRO` y `TIEMPO_ACTUALIZACION` permanecen como metadatos necesarios para la aplicación.

El cifrado de registros utiliza AAD (`PoliPass/vault-record/v1`) para evitar reutilizar el mismo contexto criptográfico con otros datos de la aplicación.

La V4 incorpora una migración desde la versión anterior. Los registros existentes se leen desde las columnas antiguas, se convierten a un único payload cifrado y después se eliminan sus valores en claro. La operación no elimina la tabla ni los registros.

Como los campos sensibles ya no están disponibles para consultas SQL, la búsqueda por título y la coincidencia por sitio web se realizan sobre los registros descifrados en memoria. Para una bóveda local pequeña esta estrategia mantiene la privacidad de los datos almacenados a costa de recorrer los registros durante estas operaciones.

## Backup seguro

La exportación utiliza el selector de documentos de Android y crea archivos `.ppbk`. El contenido completo de la bóveda se serializa a JSON y se cifra con AES-GCM. La contraseña del backup se convierte en una clave AES mediante PBKDF2-HMAC-SHA256 con un salt aleatorio por archivo.

La restauración primero descifra y valida el backup; solo después ejecuta una transacción que reemplaza los registros existentes. Un backup corrupto o una contraseña incorrecta no modifica la bóveda actual.
