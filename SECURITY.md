# Security Policy

## Scope

PoliPass is an academic/portfolio project and is **not production-ready password-manager software**.

The most important known security limitations are documented in the repository README and include:

- knowledge-based recovery remains susceptible to weak human-chosen answers
- the local vault key is device-bound to Android Keystore; portable recovery is provided through password-protected backups
- incomplete Autofill implementation
- remaining platform-specific clipboard and sensitive-screen hardening opportunities
- destructive database recreation during upgrades

These limitations are part of the documented project roadmap and are not vulnerabilities newly discovered after release.

## Reporting a new issue

Please do not publish sensitive exploit details in a public issue when they could affect users of a future build.

For a portfolio review or responsible disclosure, open a GitHub issue with:

- a concise description of the issue
- the affected component/file
- steps to reproduce in a local test environment
- expected vs. actual behavior
- a suggested mitigation, when available

Never include real passwords, tokens, private keys, recovery answers, or personal data in an issue or pull request.

## Security goals for future releases

Future releases should establish a formal threat model and address, at minimum:

1. stronger recovery factors and secure recovery semantics
2. explicit, versioned database migrations
3. encrypted backup format evolution and recovery UX
4. clipboard and sensitive-input hardening
5. screenshot/screen-recording protections on sensitive screens
6. complete Autofill authorization and isolation before re-enabling the service
7. automated security regression tests

## Recovery answers

Recovery answers are stored as salted PBKDF2-HMAC-SHA256 verifiers and are compared without storing the original answers. Existing V1 plaintext answers are migrated to the verifier format after a successful recovery attempt.

Recovery is intentionally separate from vault encryption: answering the recovery questions authorizes a master-password reset, but the answers are not used as a key for the encrypted records.

## V4 — cifrado completo de registros

A partir de V4, SQLite no almacena en claro los campos sensibles de cada entrada. Título, cuenta, nombre de usuario, contraseña, sitio web y notas forman un único payload JSON cifrado con AES-256-GCM.

La clave AES vive en Android Keystore y no se persiste dentro de la base de datos. Cada cifrado genera un IV nuevo y AAD específico para el payload.

La migración desde V3 conserva los registros existentes: reconstruye el registro completo, lo cifra como payload y limpia las columnas heredadas.

La pérdida de la clave de Android Keystore puede hacer que la base local quede ilegible; por eso el backup `.ppbk` permite exportar una copia portátil cifrada sin extraer la clave del Keystore.

## Backups

Los respaldos `.ppbk` están diseñados para poder transportarse entre dispositivos. Su seguridad depende de la contraseña de respaldo: sin ella, el contenido autenticado por AES-GCM no puede recuperarse. PoliPass no guarda esa contraseña.

No deben volver a utilizarse exportaciones CSV de la versión anterior para transportar credenciales; el formato CSV no forma parte de la ruta de backup segura de la versión actual.

## V5.3 — hardening de release candidate

- `MainActivity` ya no es un componente exportado; la entrada pública de la aplicación permanece en la actividad lanzadora necesaria.
- El servicio Autofill incompleto dejó de anunciarse en el `AndroidManifest` hasta contar con una implementación completa y revisada.
- Se deshabilitó tráfico HTTP en claro mediante `usesCleartextTraffic=false`.
- Los campos sensibles se marcan para no participar en Autofill del sistema.
- El manejo del portapapeles de contraseñas usa un único temporizador reutilizable, evitando que una copia anterior borre accidentalmente una posterior.
