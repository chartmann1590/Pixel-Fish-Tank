# Setup Guide

This guide will help you set up the Pixel Fish Tank development environment.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: Hedgehog (2023.1.1) or later
  - Download from: https://developer.android.com/studio
- **JDK**: Version 17 or higher
  - Android Studio includes JDK, or download from: https://adoptium.net/
- **Android SDK**: API 24 (Android 7.0) or higher
  - Install via Android Studio SDK Manager
- **Git**: For version control
  - Download from: https://git-scm.com/

## Step-by-Step Setup

### 1. Clone the Repository

```bash
git clone http://10.0.0.129:3000/charles/Pixel-Fish-Tank.git
cd Pixel-Fish-Tank
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open" or "File > Open"
3. Navigate to the cloned repository folder
4. Click "OK"

### 3. Configure Firebase

The app uses Firebase for analytics, crash reporting, and push notifications. You need to set up Firebase:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use existing)
3. Add an Android app to your project:
   - Package name: `com.charles.virtualpet.fishtank`
   - Download the `google-services.json` file
4. Place the `google-services.json` file in the `app/` directory
   - **Note**: This file is gitignored and should not be committed

### 4. Sync Gradle

1. Android Studio should prompt you to sync Gradle
2. If not, click "Sync Now" or go to "File > Sync Project with Gradle Files"
3. Wait for Gradle to download dependencies (this may take a few minutes)

### 5. Set Up an Emulator (Optional)

1. Open "Tools > Device Manager"
2. Click "Create Device"
3. Select a device (e.g., Pixel 5)
4. Select a system image (API 24 or higher)
5. Download if needed
6. Finish the setup

### 6. Build and Run

1. Connect a physical device via USB (with USB debugging enabled) OR start an emulator
2. Click the "Run" button (green play icon) or press `Shift + F10`
3. Select your device/emulator
4. The app should build and launch

## Troubleshooting

### Gradle Sync Fails

- **Check internet connection**: Gradle needs to download dependencies
- **Check JDK version**: Ensure JDK 17+ is configured in Android Studio
- **Invalidate caches**: "File > Invalidate Caches / Restart"

### Build Errors

- **Missing google-services.json**: Ensure the file is in `app/` directory
- **SDK version mismatch**: Update `compileSdk` in `build.gradle.kts` if needed
- **Dependency conflicts**: Try "File > Sync Project with Gradle Files"

### Firebase Issues

- **google-services.json not found**: Ensure the file is in the correct location
- **Package name mismatch**: Verify package name matches Firebase project
- **Build errors**: Ensure Firebase plugins are applied in `build.gradle.kts`

### Emulator Issues

- **Slow performance**: Enable hardware acceleration in AVD settings
- **Cannot start emulator**: Check HAXM/Virtualization is enabled in BIOS

## Development Workflow

1. **Create a branch** for your feature/fix
2. **Make changes** to the code
3. **Test** on emulator or device
4. **Commit** with clear messages
5. **Push** and create a Pull Request

## Building Release APK

To build a release APK:

1. Go to "Build > Generate Signed Bundle / APK"
2. Select "APK"
3. Create a keystore (first time only) or use existing
4. Select release build variant
5. Click "Finish"

The APK will be generated in `app/build/outputs/apk/release/`

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/docs)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Firebase Documentation](https://firebase.google.com/docs)

## Getting Help

If you encounter issues:

1. Check the [README.md](README.md) for general information
2. Review [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines
3. Open an issue in the repository
4. Check Android Studio's error messages and logs

---

Happy coding! 🐠

