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

In Android Studio, open the `android/` folder. The run-configuration dropdown has **JVM tests (Robolectric)** and **Device tests (emulator)**, shared from `android/.run/`. They run the same Gradle tasks as the table above and show results in Studio's test runner. To run a single test, use the green arrow beside it in the editor.

Most tests run on the JVM through Robolectric, so the red → green loop needs no emulator. Device tests cover what only a real Android runtime can show.

### Dependencies and lock files

Versions live in `android/gradle/libs.versions.toml`. Gradle dependency locking pins every resolved dependency, transitives included, in committed lock files:

- `android/app/gradle.lockfile`: the app's libraries
- `android/buildscript-gradle.lockfile`: Gradle plugins (AGP, Kotlin, KSP)
- `android/settings-gradle.lockfile`: settings plugins

A build fails if anything resolves to a version other than the locked one. After you change a version or add a dependency, rewrite the locks from `android/` and commit them with the change:

```bash
./gradlew buildEnvironment :app:dependencies :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest --write-locks --no-configuration-cache
```

The Gradle wrapper (`gradlew`, `gradle/wrapper/gradle-wrapper.jar`) is committed on purpose: it pins Gradle 9.1.0 and verifies the download against the checksum in `gradle-wrapper.properties`. To upgrade, run `./gradlew wrapper --gradle-version <version> --gradle-distribution-sha256-sum <sha>` and check that the new jar's SHA-256 matches `https://services.gradle.org/distributions/gradle-<version>-wrapper.jar.sha256`.

### Run on the emulator

```bash
android emulator list
android emulator start Medium_Phone_API_37.0
./gradlew :app:assembleDebug
android run --apks=app/build/outputs/apk/debug/app-debug.apk
android screen capture --output=screen.png
```

For device tests, start the emulator with `--headless`. With a window, its display compositor can take most of the emulator's CPU on a Mac, and tests slow from about 4 seconds each to a minute or more, with flaky "Failed to inject touch input" errors. Use the window when you want to try the app yourself.

Use `android sdk install <package>` (not the deprecated `sdkmanager`) for any extra SDK packages.
