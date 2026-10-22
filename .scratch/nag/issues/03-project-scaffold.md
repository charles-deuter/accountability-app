# 03: Project scaffold — Kotlin + Compose + Material 3 app runs on the emulator

**Environment:** see ticket 01's Comments for the SDK path, API level, AVD name, and JDK.

**What to build:** An empty Nag app in the `android/` directory of the monorepo that builds, installs, and launches on the emulator, showing a placeholder home screen. The test harness is wired so every later ticket can prove behaviour with unit tests (JVM) and instrumented tests (emulator) from the command line.

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] Gradle project with Kotlin, Jetpack Compose, and Material 3; Gradle wrapper committed; dependency versions in a version catalog
- [ ] Room, Navigation Compose, and a ViewModel/coroutines setup are on the classpath, ready for ticket 05
- [ ] App launches on the emulator and shows a placeholder home screen titled "Nag"
- [ ] One passing JVM unit test and one passing instrumented test, both runnable from the CLI
- [ ] README documents the build, install, unit-test, and instrumented-test commands
- [ ] README documents how to boot the `Medium_Phone` emulator and install/launch the app with the Android CLI (`android emulator`, `android run`); use `android sdk` rather than the deprecated `sdkmanager` for any extra SDK packages
- [ ] `.gitignore` covers local SDK paths and build outputs (already partly in place)
