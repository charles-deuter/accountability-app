# 04: [Charlie] Enable USB debugging and install the scaffold on your phone

**What to build:** Charlie's physical Android phone set up as a dev device. Full-screen intents, Do Not Disturb bypass, and OEM battery behaviour only show up reliably on real hardware, so ticket 12 depends on this.

**Blocked by:** 01, 03

**Status:** resolved

- [x] Developer options enabled on the phone (tap Build number seven times)
- [x] USB debugging enabled; the Mac authorised on the phone's prompt
- [x] `adb devices` lists the phone as `device` (not `unauthorized`)
- [x] The scaffold build from ticket 03 installed and launched on the phone
- [x] Phone model and Android version recorded on this ticket (OEM matters for battery behaviour)

## Comments

**2026-11-06:** Done.
- **Phone:** Google Pixel 10a (`stallion`), Android 17 (API 37), build `CP3A.261005.005`. It runs Google's own Android, so ticket 12 won't test the heavier battery killing some other manufacturers add.
- `adb devices` lists the phone as `device` over USB.
- Installed the current build instead of the bare scaffold, since tickets 05 to 10 have replaced it. `MainActivity` launched and is the resumed activity, with no crash in the log.
- **State after install:** `USE_FULL_SCREEN_INTENT` is granted. Notifications are not granted yet (the Task Form asks), and neither are exact alarms (nothing asks until ticket 11).
