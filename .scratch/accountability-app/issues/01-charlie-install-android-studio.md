# 01: [Charlie] Install Android Studio, SDK, and an emulator

**What to build:** A working Android toolchain on Charlie's Mac so agents can build, test, and run the app from the command line. Nothing here is coding work; the agent cannot install a GUI IDE or accept SDK licences on Charlie's behalf.

**Blocked by:** None (can start immediately)

**Status:** resolved

- [x] Android Studio installed (current stable)
- [x] Android SDK installed with the latest stable platform and build tools; SDK licences accepted
- [x] Android SDK Command-line Tools (latest) installed from the SDK Tools tab
- [x] `ANDROID_HOME` exported in the shell profile; `platform-tools`, `cmdline-tools/latest/bin`, and `emulator` on PATH
- [x] `JAVA_HOME` points at Android Studio's bundled JBR
- [x] One emulator (AVD) created on the latest API with a Google Play arm64 image
- [x] SDK path and AVD name recorded below for ticket 03

## Comments

**2026-10-21 — environment as installed**

- Host: Apple M2 Pro (arm64), macOS 26.5
- SDK: `~/Library/Android/sdk`
- SDK Platform: API 37 (`android-37.0`)
- System image: API 37, `google_apis_playstore`, `arm64-v8a`
- AVD: `Medium_Phone_API_37.0` (API 37). The folder on disk is `Medium_Phone.avd`, but the CLI and the emulator use this ID
- JDK: Android Studio JBR, OpenJDK 25.0.3
- `adb` 1.0.41 (platform-tools 37.0.1)
- Command-line Tools ship the new **Android CLI** (`android`, version 1.0.16625370). `sdkmanager` still runs but prints a deprecation warning; `android sdk install|list|update|remove` replaces it. The CLI also covers emulator start/stop, app install/run, screenshots, UI layout trees, input gestures, and docs search. Docs: https://d.android.com/tools/agents/android-cli
- `android init` run 2026-10-21: installed the `android-cli` agent skill machine-wide into `~/.claude/skills`, `~/.agents/skills`, and `~/.copilot/skills` (separate copies, not symlinks; update with `android skills update`). Nothing written to the repo, so other machines or cloud sessions need their own `android init`.
- Not run: `android auth` (Google sign-in). Not needed by any ticket.
