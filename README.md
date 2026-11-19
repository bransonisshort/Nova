# NOVA OS Mobile — Android Project

**Version:** 1.0.0  
**Package:** `com.novaos.mobile`  
**Languages:** Kotlin, Java, C++17  
**Minimum Android:** 8.0 (API 26)  
**Compile/target SDK:** 35  
**JDK:** 17

NOVA OS Mobile is an OS-inspired Android application shell with a crimson command deck, allowlisted terminal, launchable-app browser, device information, settings, and Android-mediated app installation/removal.

## Android platform boundaries

This is an Android application, not a replacement operating system, custom ROM, root environment, or unrestricted shell. Android's application sandbox and permissions remain in force.

- Terminal commands are allowlisted; arbitrary shell execution is not provided.
- APK installation opens Android's package installer and requires user approval.
- App removal opens Android's uninstall confirmation and requires user approval.
- App visibility follows Android package visibility rules.
- NOVA cannot uninstall itself from its own running session.

## Included features

- HOME, TERMINAL, APPS, SYSTEM, SETTINGS sections
- Commands: `help`, `clear`, `date`, `time`, `device`, `apps`, `uptime`, `version`, `settings`, `install`, `uninstall <package>`, `open <package>`
- Discover and launch apps with launcher activities
- Choose an APK through Android's document picker
- Request install through Android's package installer
- Request uninstall through Android's confirmation interface
- Read device metadata in Java
- Load C++17 native library using JNI
- Save a settings preference with SharedPreferences

## Build in Android Studio

1. Install Android Studio and Android SDK Platform 35.
2. Install Android SDK Build-Tools, Platform-Tools, NDK, and CMake from SDK Manager.
3. Extract the ZIP and open the `NOVA-OS-Android` folder in Android Studio.
4. Use JDK 17 and allow Gradle sync to finish.
5. Connect a Motorola/Android device with USB debugging enabled, or use an emulator.
6. Select the `app` run configuration and press **Run**.

Gradle wrapper scripts are not included. If Gradle 8.11.1 is installed, generate the wrapper from the project root:

```bash
gradle wrapper --gradle-version 8.11.1
```

Build a debug APK:

```bash
./gradlew assembleDebug
```

Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

Expected output: `app/build/outputs/apk/debug/app-debug.apk`.

## First launch

1. Open NOVA OS and select TERMINAL.
2. Enter `help`.
3. Open APPS to browse launchable applications.
4. Select Install APK and choose an APK. If prompted, allow installs from NOVA in Android settings, return to NOVA, and continue.
5. Select UNINSTALL beside an app to open Android's confirmation screen.

Only install APKs from sources you trust. Review permissions before approving installation.

## Known limitations and next steps

This is a functional starter foundation, not a finished launcher replacement. The UI is programmatic in one Activity. A production build should add automated tests, accessibility review, responsive layouts, persistent terminal history, app-list refresh, robust APK lifecycle handling, and a dedicated navigation architecture. A default-home launcher role, device-owner management, or privileged system integration requires separate Android APIs, explicit user consent, and sometimes enterprise provisioning or OEM support.

## Build an APK using GitHub Actions (Chromebook-friendly)

This repository includes `.github/workflows/build-apk.yml`.

1. Create a GitHub repository and upload the contents of this project.
2. Open the repository's **Actions** tab.
3. Select **Build NOVA OS APK** and choose **Run workflow** (or push to `main`/`master`).
4. When the workflow finishes, open its run page and download the `NOVA-OS-debug-apk` artifact.
5. Extract the artifact to get `app-debug.apk`.

This workflow creates a **debug APK** for testing. It is not a Play Store release build.
The APK still needs to be installed on an Android device and tested there. Android may
require you to approve installation from the app or file manager you use to open it.
