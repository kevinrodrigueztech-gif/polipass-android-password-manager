# PoliPass 🔐

**Local-first Android password manager built in Java.**

PoliPass is an academic and portfolio project focused on Android application development, local data persistence, cryptography, biometric authentication, and secure portable backups.

> **Security notice:** this is an academic/portfolio password-manager project and is **not intended for real-world production credentials**. The current release documents remaining security debt around recovery design, database migrations, backup lifecycle, and large-vault performance.

## What this project demonstrates

- Android application development with Java and AndroidX
- Local persistence with SQLite
- AES-GCM encryption for complete vault records
- Android Keystore-backed symmetric key management
- Biometric authentication with `BiometricPrompt`
- Password generation with `SecureRandom`
- Password-protected encrypted backup/restore
- Configurable failed-login protection and vault clearing
- Material UI with dark-theme support

## Architecture

The current codebase is intentionally simple and easy to inspect:

```text
app/
└── src/main/java/com/redsytem/passwordapp/
    ├── BaseDeDatos/         # SQLiteOpenHelper and schema constants
    ├── Detalle/             # Credential detail screen
    ├── Encriptacion/        # Android Keystore + AES/GCM/NoPadding
    ├── Fragmentos/          # Main fragments and settings
    ├── Login_usuario/       # Master-password and biometric login
    ├── Modelo/              # Password data model
    ├── OpcionesPassword/    # Create/update credential flow
    └── Registro_usuario/    # Initial master-password setup
```

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the main data flows and security boundaries.

## Security model

The vault record is encrypted as a single authenticated payload using AES-256-GCM with a 256-bit key generated and retained by Android Keystore. Each record gets a fresh GCM IV and authenticated additional data (AAD).

The master password is stored only as a PBKDF2-HMAC-SHA256 verifier. Recovery answers are stored as independent salted verifiers and are used only to authorize a master-password reset.

Backups use a separate user-chosen backup password and an encrypted `.ppbk` format, so the portable backup does not depend on the source device's Android Keystore key.

For a deeper review, see [`SECURITY.md`](SECURITY.md).

## Technology stack

| Area | Technology |
| --- | --- |
| Language | Java 8 source/target compatibility |
| UI | AndroidX AppCompat, Material Components, ConstraintLayout |
| Persistence | SQLite / `SQLiteOpenHelper` |
| Cryptography | AES-GCM / Android Keystore |
| Authentication | `BiometricPrompt` |
| Password generation | `SecureRandom` |
| Animation | Lottie |
| Build | Gradle + Android Gradle Plugin |

The project currently uses **Android Gradle Plugin 9.3.2** with **Gradle 9.5.0**. Android's current compatibility documentation lists Gradle 9.5.0 as the minimum required version for the 9.3 line, and AGP 9.3 requires JDK 17. citeturn688531search0turn688531search4

## Requirements

- Android Studio compatible with AGP 9.3.x
- JDK 17
- Android SDK Platform 34
- Gradle Wrapper included in the repository

## Build locally

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
```

`local.properties` is intentionally ignored because it contains the local Android SDK path.

## Testing and quality gates

The repository includes starter unit/instrumentation tests and a GitHub Actions workflow that runs Gradle tests, Android Lint, and a debug build on every push and pull request.

The current test suite is intentionally small. Expanding coverage is part of the roadmap, especially for encryption, authentication state, database migrations, backup validation, and future Autofill parsing.

## Known limitations

1. **Recovery factors:** knowledge-based recovery can still be weak if users choose predictable answers.
2. **Database migrations:** the schema now supports full-record encryption, but future schema changes should remain explicitly versioned and migration-safe.
3. **Backup lifecycle:** encrypted backups are portable, but the user must remember the backup password; PoliPass cannot recover it.
4. **Autofill:** not advertised in the current release; the previous partial service was removed until it can be implemented and reviewed as a complete feature.
5. **Clipboard/sensitive UI:** password clipboard lifetime is controlled, while further platform-specific hardening can still be added.
6. **Performance at scale:** full-record search decrypts records in memory, which is appropriate for a small local vault but should be revisited for very large datasets.

## Roadmap

- Strengthen recovery with a less guessable recovery factor
- Add explicit, versioned database migrations
- Add backup format version migration and stronger recovery UX
- Implement and review Autofill as a complete feature
- Improve clipboard expiration and sensitive-screen protection
- Expand unit and instrumentation coverage
- Add static analysis and dependency-security checks to CI
- Refactor toward clearer separation of UI, domain, and data layers

## Portfolio context

This repository is intentionally structured as a **transparent security-engineering case study**: it shows working Android concepts while clearly documenting the engineering and security work that remains before a production release.

For a hiring review, the most relevant areas to inspect first are:

- [`Encriptacion/Encrypt.java`](app/src/main/java/com/redsytem/passwordapp/Encriptacion/Encrypt.java)
- [`BaseDeDatos/BDHelper.java`](app/src/main/java/com/redsytem/passwordapp/BaseDeDatos/BDHelper.java)
- [`Login_usuario/Logeo_usuario.java`](app/src/main/java/com/redsytem/passwordapp/Login_usuario/Logeo_usuario.java)

## License

No open-source license has been assigned to this repository yet. Add a license that matches the intended distribution model before publishing the project as reusable open-source software.

## Authors

- Neyra García Keila Yael
- Rodríguez García Kevin Fernando

## Seguridad

La contraseña maestra no se almacena en texto plano. Se guarda un verificador derivado mediante **PBKDF2-HMAC-SHA256**, con salt aleatorio de 16 bytes y 600,000 iteraciones. La contraseña original no puede recuperarse desde el verificador.

Las instalaciones de V1 que todavía contienen `password` en `SharedPreferences` se migran automáticamente al nuevo formato después del primer inicio de sesión correcto; las claves heredadas se eliminan al finalizar la migración.

La contraseña maestra funciona como mecanismo de autenticación y nunca se almacena en texto plano. La clave AES-GCM usada por la capa actual de cifrado sigue gestionándose mediante Android Keystore y es independiente del verificador de la contraseña.

Las respuestas de recuperación tampoco se almacenan en texto plano. Cada respuesta usa un salt independiente y PBKDF2-HMAC-SHA256. Una recuperación exitosa no revela ni reconstruye la contraseña anterior: únicamente autoriza al usuario a establecer una nueva contraseña maestra que genera otro verificador.

Las instalaciones heredadas con respuestas V1 se migran de forma gradual después de una verificación correcta.

## Rendimiento y seguridad

La derivación PBKDF2 usa 600,000 iteraciones de HMAC-SHA256 de forma deliberadamente costosa para dificultar ataques de fuerza bruta. Para evitar que esta protección congele la interfaz, las operaciones de autenticación y generación de verificadores se ejecutan en un hilo de trabajo y la interfaz muestra un estado temporal como “Verificando...” o “Guardando...”. No se reduce el número de iteraciones para obtener velocidad a costa de seguridad.


## Respaldo y restauración cifrados

PoliPass ya no utiliza CSV para exportar credenciales. El respaldo se genera como un archivo `.ppbk` protegido con una contraseña de respaldo que el usuario elige y que PoliPass no almacena.

El contenido de la bóveda se serializa y se cifra con AES-256-GCM usando una clave derivada mediante PBKDF2-HMAC-SHA256. El archivo incluye salt, IV, versión de formato y ciphertext autenticado. Durante la restauración se valida y descifra el archivo completo antes de reemplazar la bóveda; la sustitución de registros se realiza dentro de una transacción de SQLite.

La contraseña de respaldo es independiente de la contraseña maestra. El respaldo no depende de la clave del Android Keystore del dispositivo de origen, por lo que puede restaurarse en otro dispositivo que tenga PoliPass instalado.

## Release hardening

Version 1.2 applies a release-candidate hardening pass: internal activities are not exported unnecessarily, cleartext HTTP is disabled, sensitive input fields opt out of Autofill, and password clipboard clearing uses a single replaceable timer. The incomplete Autofill service is intentionally not registered until it is complete.
