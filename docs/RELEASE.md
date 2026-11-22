# Release & Signing Guide

This document describes how to generate production signing credentials, configure release builds, and verify release artifacts for **Scroll the Gita**.

---

## 1. Generating a Production Keystore

To generate a new, secure production keystore using Java's `keytool`:

```bash
keytool -genkeypair -v \
  -keystore release.jks \
  -alias scrollthegita \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=Scroll The Gita, OU=Mobile, O=ScrollTheGita, L=City, ST=State, C=Country"
```

> [!CAUTION]
> - Store your production keystore and passwords in a secure password manager or secrets vault.
> - **NEVER** commit `.jks`, `.keystore`, or `keystore.properties` to version control.
> - If you lose this keystore, you will not be able to publish updates to existing installations on Google Play or other app stores.

---

## 2. Configuring Signing Credentials

You can supply signing credentials using either a local configuration file or CI/CD environment variables.

### Option A: Local `keystore.properties` (Recommended for Local Release Testing)
Copy `keystore.properties.example` to `keystore.properties` at the project root:

```properties
storeFile=release.jks
storePassword=your_secure_keystore_password
keyAlias=scrollthegita
keyPassword=your_secure_key_password
```

### Option B: Environment Variables (Recommended for CI/CD)
Set the following environment variables:
- `RELEASE_STORE_FILE`: Absolute path or path relative to the root project (e.g. `release.jks`).
- `RELEASE_STORE_PASSWORD`: Keystore password.
- `RELEASE_KEY_ALIAS`: Key alias.
- `RELEASE_KEY_PASSWORD`: Key password.

---

## 3. Building the Release Artifacts

To compile and package a signed, R8-minified, and resource-shrunk release APK:

```bash
./gradlew assembleRelease
```

The output will be placed at:
```
app/build/outputs/apk/release/app-release.apk
```

To build an Android App Bundle (AAB) for Google Play:

```bash
./gradlew bundleRelease
```

The bundle will be placed at:
```
app/build/outputs/bundle/release/app-release.aab
```

---

## 4. Verifying the Signature

Verify the cryptographic signature with Android SDK's `apksigner`:

```bash
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
```

Expected output should confirm:
- `Verifies: true`
- `Verified using v1 scheme (JAR signing): true`
- `Verified using v2 scheme (APK Signature Scheme v2): true`

---

## 5. Installing on a Connected Test Device

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell am start -n com.scrollthegita/.app.MainActivity
```
