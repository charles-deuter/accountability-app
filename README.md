# Accountability App

An ADHD-focused Android reminder app. Reminders can't be dismissed — only snoozed or completed. Full-screen notifications break through hyperfocus.

## Structure

```
android/   — Kotlin + Jetpack Compose app
backend/   — Future API (v2)
docs/      — PRD and design documents
```

## Status

In development. See [docs/PRD.md](docs/PRD.md) for the full product spec, [CONTEXT.md](CONTEXT.md) for the domain glossary, and `.scratch/accountability-app/issues/` for tickets.

## Android development

### Prerequisites

- Android Studio with the SDK at `~/Library/Android/sdk` and **Android SDK Command-line Tools (latest)** installed (this provides the `android` CLI)
- Shell environment:

  ```bash
  export ANDROID_HOME="$HOME/Library/Android/sdk"
  export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator:$PATH"
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  ```

Gradle runs on Android Studio's bundled JDK. The build compiles and tests with a Java 21 toolchain, which Gradle downloads on first run.

### Build and test

Run these from `android/`:

| What | Command |
|---|---|
| Build a debug APK | `./gradlew :app:assembleDebug` |
| JVM tests (Robolectric, no emulator needed) | `./gradlew :app:testDebugUnitTest` |
| Device tests (needs a running emulator or phone) | `./gradlew :app:connectedDebugAndroidTest` |

Most tests run on the JVM through Robolectric, so the red → green loop needs no emulator. Device tests cover what only a real Android runtime can show.

### Run on the emulator

```bash
android emulator list
android emulator start Medium_Phone_API_37.0
android run --apks=app/build/outputs/apk/debug/app-debug.apk
android screen capture --output=screen.png
```

Use `android sdk install <package>` (not the deprecated `sdkmanager`) for any extra SDK packages.
