# 03: Project scaffold — Kotlin + Compose + Material 3 app runs on the emulator

**Environment:** see ticket 01's Comments for the SDK path, API level, AVD name, and JDK.

**What to build:** An empty Accountability App project in the `android/` directory of the monorepo that builds, installs, and launches on the emulator, showing a placeholder home screen. The test harness is wired so every later ticket can prove behaviour with unit tests (JVM) and instrumented tests (emulator) from the command line.

**Blocked by:** 01

**Status:** resolved

- [x] Gradle project with Kotlin, Jetpack Compose, and Material 3; Gradle wrapper committed; dependency versions in a version catalog
- [x] Room, Navigation Compose, and a ViewModel/coroutines setup are on the classpath, ready for ticket 05
- [x] App launches on the emulator and shows a placeholder home screen titled "Accountability App"
- [x] One passing JVM unit test and one passing instrumented test, both runnable from the CLI
- [x] README documents the build, install, unit-test, and instrumented-test commands
- [x] README documents how to boot the `Medium_Phone_API_37.0` emulator and install/launch the app with the Android CLI (`android emulator`, `android run`); use `android sdk` rather than the deprecated `sdkmanager` for any extra SDK packages
- [x] `.gitignore` covers local SDK paths and build outputs (already partly in place)

## Comments

**2026-10-21: resolved on branch `feat/03-project-scaffold`**

- Scaffolded with `android create empty-activity` (AGP 9.0.1, Kotlin 2.3.20, Gradle 9.1, Compose BOM 2026.03.01). The template uses **Navigation 3**, not Navigation Compose, so navigation stays on Navigation 3.
- compileSdk and targetSdk are **36**, the template's default; the template installed platform 36 on its own. Moving to 37 can wait for an AGP release that supports it.
- The applicationId and namespace are the **placeholder** `dev.accountabilityapp`, which Charlie chose to defer. Ticket 20 now has a checklist item to replace it before the first Play upload.
- Added Room 2.8.5 with KSP 2.3.12, and Robolectric 4.17 with Compose UI testing on the JVM.
- Java toolchain is 21, not the template's 17, because Robolectric needs Java 21 to emulate SDK 35+. Unit tests also pass `--add-exports=java.base/jdk.internal.access=ALL-UNNAMED`; without it, Robolectric's FileDescriptor interceptor fails on JDK 21.
- Gradle 9.1 runs on Android Studio's bundled JDK 25 without problems.
- Tests: `AppLaunchTest` (JVM) and `AppLaunchDeviceTest` (emulator) both check that launching the app shows "Accountability App". Both pass, and a smoke launch on `Medium_Phone_API_37.0` showed the title bar.
- Removed the template's sample repository, screen, and tests.
