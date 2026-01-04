# Building a Signed Play Store Bundle (AAB)

This guide explains how to create a signed Android App Bundle (AAB) for Google Play Store submission.

## Prerequisites

- Java JDK installed (for keytool)
- Android SDK configured
- Gradle build system

## Step 1: Create a Keystore (First Time Only)

If you don't have a keystore yet, create one using the provided script:

### Windows (PowerShell):
```powershell
.\scripts\create-keystore.ps1
```

### Linux/Mac (Bash):
```bash
chmod +x scripts/create-keystore.sh
./scripts/create-keystore.sh
```

The script will:
- Create a keystore file at `app/release.keystore`
- Create a `keystore.properties` file with your signing credentials
- Prompt you for passwords and certificate information

**IMPORTANT**: 
- Save your keystore file and passwords securely
- If you lose the keystore, you cannot update your app on Google Play Store
- The `keystore.properties` file contains passwords and is already in `.gitignore`

## Step 2: Build the Signed Bundle

Once you have a keystore configured, build the signed bundle:

```bash
# Windows
.\gradlew bundleRelease

# Linux/Mac
./gradlew bundleRelease
```

The signed bundle will be created at:
```
app/build/outputs/bundle/release/app-release.aab
```

## Step 3: Verify the Bundle

You can verify the bundle is signed using:

```bash
# Windows
jarsigner -verify -verbose -certs app\build\outputs\bundle\release\app-release.aab

# Linux/Mac
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

## Troubleshooting

### "Keystore file not found"
- Ensure `keystore.properties` exists in the project root
- Check that the `storeFile` path in `keystore.properties` is correct (relative to project root)

### "Password incorrect"
- Verify the passwords in `keystore.properties` match your keystore
- You can manually edit `keystore.properties` if needed

### "Signing config not found"
- Ensure `keystore.properties` exists and contains all required fields:
  - `storeFile`
  - `storePassword`
  - `keyAlias`
  - `keyPassword`

## Manual Keystore Creation (Alternative)

If you prefer to create the keystore manually:

```bash
keytool -genkey -v -keystore app/release.keystore -alias release -keyalg RSA -keysize 2048 -validity 9125
```

Then create `keystore.properties` manually:
```properties
storeFile=../app/release.keystore
storePassword=your_store_password
keyAlias=release
keyPassword=your_key_password
```

## Next Steps

After building the signed bundle:
1. Upload `app-release.aab` to Google Play Console
2. Complete the store listing information
3. Submit for review

