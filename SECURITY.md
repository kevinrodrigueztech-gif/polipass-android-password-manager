# Security Policy

## Scope

PoliPass V1 is an academic/portfolio project and is **not production-ready password-manager software**.

The most important known security limitations are documented in the repository README and include:

- knowledge-based recovery remains susceptible to weak human-chosen answers
- the vault key is currently device-bound to Android Keystore, so secure cross-device recovery is not implemented
- encryption limited to the password field rather than the full record
- non-encrypted CSV export/import
- incomplete Autofill implementation
- incomplete clipboard and sensitive-screen hardening
- destructive database recreation during upgrades

These limitations are part of the documented V1 technical debt and are not vulnerabilities newly discovered after release.

## Reporting a new issue

Please do not publish sensitive exploit details in a public issue when they could affect users of a future build.

For a portfolio review or responsible disclosure, open a GitHub issue with:

- a concise description of the issue
- the affected component/file
- steps to reproduce in a local test environment
- expected vs. actual behavior
- a suggested mitigation, when available

Never include real passwords, tokens, private keys, recovery answers, or personal data in an issue or pull request.

## Security goals for V2

V2 should establish a formal threat model and address, at minimum:

1. explicit vault-key wrapping/rotation and secure recovery semantics
2. authenticated encrypted vault storage
3. encrypted backup/restore with integrity protection
4. safer recovery design
5. clipboard lifetime controls
6. screenshot/screen-recording protections on sensitive screens
7. Autofill authorization and isolation
8. automated security regression tests

## Recovery answers

Recovery answers are stored as salted PBKDF2-HMAC-SHA256 verifiers and are compared without storing the original answers. Existing V1 plaintext answers are migrated to the verifier format after a successful recovery attempt.

Recovery is intentionally separate from vault encryption: answering the recovery questions authorizes a master-password reset, but the answers are not used as a key for the encrypted records.

## V4 — cifrado completo de registros

A partir de V4, SQLite no almacena en claro los campos sensibles de cada entrada. Título, cuenta, nombre de usuario, contraseña, sitio web y notas forman un único payload JSON cifrado con AES-256-GCM.

La clave AES vive en Android Keystore y no se persiste dentro de la base de datos. Cada cifrado genera un IV nuevo. El ciphertext incluye el IV y el tag de autenticación de GCM.

La migración desde V3 conserva los registros existentes: descifra la contraseña heredada, reconstruye el registro completo, lo cifra como payload y limpia las columnas heredadas.

La seguridad de la bóveda depende también de la disponibilidad de la clave de Android Keystore. Desinstalar la aplicación o eliminar las claves del dispositivo puede hacer que una bóveda cifrada quede irrecuperable, por lo que el proyecto todavía necesita un mecanismo de respaldo/restauración de la clave de bóveda antes de considerarse listo para producción.
