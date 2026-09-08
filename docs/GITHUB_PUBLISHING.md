# GitHub publishing checklist

This document is intended for the first public portfolio upload.

## Recommended repository name

`polipass-android-password-manager`

## Before the first push

1. Open the project in Android Studio and confirm that it syncs with JDK 17.
2. Run the local quality checks:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

3. Review the V1 security limitations in `SECURITY.md`.
4. Do not add `local.properties`, generated APKs/AABs, signing keys, passwords, tokens, or personal vault data.
5. Replace the contact placeholder in `app/src/main/res/values/strings.xml` with a professional public contact address before publishing a portfolio build.

## First push

```bash
git init
git branch -M main
git add .
git commit -m "chore: harden PoliPass release candidate"
git remote add origin https://github.com/<USERNAME>/polipass-android-password-manager.git
git push -u origin main
```

## Suggested repository settings

Enable Issues and Discussions only when you intend to maintain them. Keep Actions enabled so the Android CI workflow remains visible to reviewers. Dependabot is configured for monthly Gradle dependency checks.

For a portfolio profile, add a short repository description such as:

> Local-first Android password manager in Java demonstrating AES-GCM, Android Keystore, biometric authentication, SQLite persistence, and Autofill integration.

## Suggested GitHub topics

`android` `java` `password-manager` `cryptography` `android-keystore` `biometric` `sqlite` `autofill` `portfolio-project`
