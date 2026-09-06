# PoliPass 🔐

**Local-first Android password manager built in Java.**

PoliPass is an academic and portfolio project focused on Android application development, local data persistence, cryptography, biometric authentication, and the Android Autofill Framework.

> **Security notice:** this repository contains the original **V1** implementation. It is intended for technical review and learning, **not for storing real-world production credentials**. The current version has documented security debt, especially around master-password storage, recovery answers, backup/export, and Autofill.

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

The rest of the vault record is not encrypted by the current implementation. In addition, the master password and recovery answers are currently stored directly in `SharedPreferences`. These are known limitations and are intentionally called out rather than being presented as production-grade security.

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

1. **Master password storage:** the master password is stored directly in `SharedPreferences` and should be redesigned around a password-derived verification/key-unwrapping scheme.
2. **Recovery answers:** recovery answers are stored in plaintext and should be replaced with a safer recovery model.
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
