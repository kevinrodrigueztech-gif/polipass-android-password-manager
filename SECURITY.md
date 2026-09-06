# Security Policy

## Scope

PoliPass V1 is an academic/portfolio project and is **not production-ready password-manager software**.

The most important known security limitations are documented in the repository README and include:

- plaintext master-password storage in `SharedPreferences`
- plaintext recovery answers in `SharedPreferences`
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

1. password-derived key hierarchy and secure key rotation
2. authenticated encrypted vault storage
3. encrypted backup/restore with integrity protection
4. safer recovery design
5. clipboard lifetime controls
6. screenshot/screen-recording protections on sensitive screens
7. Autofill authorization and isolation
8. automated security regression tests
