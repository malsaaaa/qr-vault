# QR Vault

A private, offline QR-code vault for Android. Store QR codes you use in daily life (banking, payments, membership cards) with provider, name, description and color labels, then view, share, search and manage them — all on-device, with no account, analytics, ads or network access.

Built with modern Android: Kotlin, Jetpack Compose, Room, CameraX.

## Features

- Add QR codes from the **gallery** or directly with the **camera** (preview, capture, retake)
- Rich metadata: provider, friendly name, optional description and color label
- **Grid / list** views, **search** (name, provider, description) and **sort** (name, created date)
- Item **details** with created/updated timestamps, fullscreen **viewer** and native **share**
- **Edit** and **delete** (with optional confirmation dialog)
- **App lock** with numeric PIN or **biometric** unlock (fingerprint/face/device credential)
- **Auto-lock** on background (Immediate / 1 / 5 / 10 minutes / Never) and manual "Lock now"
- **Themes**: system default, light or dark
- Fully offline: works without network and never phones home

## Security

- **No `INTERNET` permission** — data cannot be transmitted off the device
- QR images stored in private app storage, exposed only through a narrow `FileProvider` path
- PIN protected with AES/GCM keyed by the Android Keystore; the raw PIN is never persisted
- Brute-force protection: 5 failed attempts trigger a 30-second lockout, persisted across restarts
- Backups fully excluded (all Android versions) via `fullBackupContent` / `dataExtractionRules`

## Tech stack

- Kotlin 2.1, JVM target 17
- Jetpack Compose (Material 3, BOM 2024.12.01), Navigation Compose, Coil
- Room (Kapt), DataStore Preferences, CameraX, Biometric
- minSdk 26 / targetSdk 36 (`compileSdk 36`)

## Requirements

- JDK 17
- Android SDK (compileSdk 36) — set your SDK path in `local.properties` if not already configured
- A physical device (or emulator) running Android 8.0+

## Build

```bash
# Debug build + install
./gradlew :app:assembleDebug
./gradlew :app:installDebug

# Unit tests
./gradlew :app:testDebugUnitTest

# Lint
./gradlew :app:lintDebug

# Release build (see signing below)
./gradlew :app:assembleRelease
```

On Windows use `.\gradlew.bat` instead of `./gradlew`.

## Release signing

The release build is signed with a keystore stored **outside** the repository
(`C:\Users\HP VICTUS\qrvault-release.jks`). Gradle reads signing credentials from
`keystore.properties` in the project root, which is git-ignored.

1. Create `keystore.properties` from the template:

   ```
   cp keystore.properties.example keystore.properties
   ```

2. Fill in the real values (the keystore file path and the two passwords you set when
   creating the keystore with `keytool`):

   ```
   storeFile=C:/Users/HP VICTUS/qrvault-release.jks
   storePassword=<your store password>
   keyAlias=qrvault
   keyPassword=<your key password>
   ```

3. Build:

   ```bash
   ./gradlew :app:assembleRelease
   ```

Signed APK: `app/build/outputs/apk/release/app-release.apk`.

Keep `keystore.properties` and `.jks` files private. Never commit them. If
`keystore.properties` is missing, the release build still completes but produces an
**unsigned** APK (`app-release-unsigned.apk`) that cannot be installed.

## Project structure

```
app/src/main/java/com/qrvault/
├── MainActivity.kt
├── data/          # Room database, repositories, DataStore settings, image storage
├── security/      # PIN encryption (Keystore) and throttling
├── ui/            # Compose screens: home, add, camera, details, viewer, lock, settings
│   ├── components/  # shared composables (QR card, pin pad, etc.)
│   └── navigation/  # navigation graph
└── util/          # QR processing helpers
```

## License

Private project.