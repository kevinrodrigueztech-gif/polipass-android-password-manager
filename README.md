# PoliPass 🔐

**Local-first Android password manager built in Java.**

PoliPass is an academic and portfolio project focused on Android application development, local data persistence, cryptography, biometric authentication, and the Android Autofill Framework.

> **Security notice:** this is an academic/portfolio password-manager project and is **not intended for real-world production credentials**. The current release documents remaining security debt around full-record encryption, backup/export, and Autofill.

## What this project demonstrates

- Android application development with Java and AndroidX
- Local persistence with SQLite
- AES-GCM encryption for stored password fields
- Android Keystore-backed symmetric key management
- Biometric authentication with `BiometricPrompt`
- Password generation with `SecureRandom`
- CSV import/export flow
- Configurable failed-login protection and vault clearing
- Material UI with dark-theme support
- Initial Android Autofill Service integration

## Architecture

The current V1 codebase is intentionally simple and easy to inspect:

```text
app/
└── src/main/java/com/redsytem/passwordapp/
    ├── AutoFill/            # AutofillService integration (partial)
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

## Security model — V1

The V1 implementation encrypts the **stored password field** using AES-GCM with a 256-bit key generated and retained by Android Keystore. Each encryption operation generates a fresh GCM IV and stores the IV alongside the ciphertext.

The rest of the vault record is not encrypted by the current implementation. The master password is stored only as a PBKDF2 verifier. Recovery answers are also stored as independent salted PBKDF2 verifiers. These limitations are intentionally documented rather than presented as production-grade security.

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
| Autofill | Android Autofill Framework |
| Animation | Lottie |
| CSV | OpenCSV |
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

The current test suite is intentionally small. Expanding coverage is part of the V2 roadmap, especially for encryption, authentication state, database migrations, import/export validation, and Autofill parsing.

## Known V1 limitations

The following items are tracked as technical debt rather than hidden:

1. **Vault key hierarchy:** the current vault key remains device-bound in Android Keystore; a future release can add explicit key-wrapping/rotation semantics and stronger cross-device recovery.
2. **Recovery answers:** answers now use salted PBKDF2 verifiers, but knowledge-based recovery remains weaker than a modern recovery factor because answers may be guessable.
3. **Database upgrades:** `onUpgrade()` currently recreates the table instead of performing versioned migrations.
4. **Vault coverage:** only the password column is encrypted; other record fields remain plaintext in SQLite.
5. **Backup/export:** CSV export is not an encrypted portable vault format and must not be treated as a secure backup mechanism.
6. **Autofill:** the Autofill Service is a partial implementation and should not be considered production-ready.
7. **Clipboard/sensitive UI:** clipboard handling and sensitive-screen protections need further hardening.

## V2 roadmap

- Replace plaintext master-password storage with a password-derived key hierarchy
- Hash or otherwise redesign recovery mechanisms
- Encrypt the complete vault record or move to an encrypted database design
- Add explicit, versioned database migrations
- Introduce encrypted, authenticated backup/restore
- Complete Autofill parsing and dataset generation
- Improve clipboard expiration and sensitive-screen protection
- Expand unit and instrumentation test coverage
- Add static analysis and dependency-security checks to CI
- Refactor toward clearer separation of UI, domain, and data layers

## Portfolio context

This repository is intentionally structured as a **transparent V1 case study**: it shows working Android concepts while clearly documenting the engineering and security work that remains before a production release.

For a hiring review, the most relevant areas to inspect first are:

- [`Encriptacion/Encrypt.java`](app/src/main/java/com/redsytem/passwordapp/Encriptacion/Encrypt.java)
- [`BaseDeDatos/BDHelper.java`](app/src/main/java/com/redsytem/passwordapp/BaseDeDatos/BDHelper.java)
- [`Login_usuario/Logeo_usuario.java`](app/src/main/java/com/redsytem/passwordapp/Login_usuario/Logeo_usuario.java)
- [`AutoFill/PasswordAutofillService.java`](app/src/main/java/com/redsytem/passwordapp/AutoFill/PasswordAutofillService.java)

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


## Seguridad — evolución V4

La bóveda ahora cifra el registro completo con AES-GCM y mantiene la clave fuera de SQLite mediante Android Keystore. La contraseña maestra se almacena únicamente como verificador PBKDF2 con salt independiente, y la recuperación usa verificadores separados.

### Próximo reto de seguridad

Diseñar un flujo de respaldo/restauración de la bóveda que no requiera extraer la clave de Android Keystore en texto plano y revisar la política de exportación/importación para evitar archivos CSV con información sensible fuera del almacenamiento protegido.
