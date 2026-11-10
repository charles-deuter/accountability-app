# 11: Permission onboarding and missing-permission warnings

**What to build:** On first launch the app walks the user through the four grants that make reminders reliable, explaining each in one sentence and deep-linking to the right system settings screen. Afterwards, whenever any grant is missing, the Task List shows a banner that leads back to the fix. Re-checks run every time the app returns to the foreground.

**Blocked by:** 09

**Status:** resolved

Permissions: exact alarms (Android 12+), full-screen intent (Android 14+), notifications (Android 13+), battery-optimization exemption (all versions; OEMs kill alarms otherwise — PRD risk #3).

Play policy notes (from Google's Alarms and Doze docs):

- Declare `SCHEDULE_EXACT_ALARM` (user-granted, revocable), not `USE_EXACT_ALARM`. `USE_EXACT_ALARM` is auto-granted but Play restricts it to a limited set of use cases such as alarm clocks and calendars, and a reminder app may not pass review. Revocable means the app must handle losing the grant at any time; the banner covers that.
- Play prohibits asking for the battery-optimization exemption unless "the core function of the app is adversely affected". On-time reminders are this app's core function, and the onboarding copy for that step should say so plainly; ticket 21 makes the same case in the Console declaration.

- [x] Onboarding shows only the steps relevant to the device's Android version
- [x] Each step opens the correct system settings screen and reflects the new state on return
- [x] The Task List banner appears when any grant is missing and disappears once all are granted
- [x] Unit tests cover the "which grants are missing" logic across API levels using a fake permission source
- [x] Without the exact-alarm grant, saving a reminder still works and the banner explains reminders may drift

## Comments

**2026-11-10, agent:**
- **Onboarding** opens on first launch and lists only the steps that the device's Android version asks for. Finishing it sets a flag in SharedPreferences. Backing out of the app leaves the flag unset, so onboarding shows again on the next launch. Afterwards the Task List banner opens the same screen.
- **Steps:**
  - Full-screen reminders, exact alarms and the battery exemption each open their system screen.
  - The battery step uses the one-tap `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` dialog and declares `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`. Its copy says on-time reminders are the app's core function.
  - Notifications use the system permission dialog, as the Task Form does. The step opens notification settings only when Android answers no without showing the dialog, which happens after two refusals.
  - Each step re-checks when the app resumes.
- **Banner:** shows while any grant is missing and re-checks on every resume. With only exact alarms off it says reminders may run a few minutes late. Saving still sets an inexact alarm (`RemindersTest.withoutTheExactAlarmPermission_savingStillSetsAnInexactAlarmThatFiresInDoze`).
- **Exact-alarm grant:** `RescheduleReceiver` now also handles `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`, so an alarm set while exact alarms were off becomes exact once the user allows them.
- **Tests, at the three approved seams:**
  - `MissingGrantsTest`: which grants count at each API level.
  - `TaskListViewModelTest`: the banner shows, hides, and gives the run-late message.
  - `OnboardingTest` at API 31, 33 and 34: which steps appear, each step's intent, the notification dialog and its Settings fallback, done-on-resume, and Continue staying on screen.
  - Launch tests that expect the Task List now mark onboarding complete first (`OnboardingCompleteRule`).
- **Smoke, emulator API 37, fresh install:**
  - Onboarding opened with all four steps; full-screen reminders already read as allowed.
  - "Allow exact alarms" opened `Settings$AlarmsAndRemindersAppActivity`. After the grant and Back, the step read "Exact alarms allowed".
  - Continue led to the Task List, whose banner said a setting is off.
  - This pass found Continue half-hidden under the gesture bar, so it now sits in a bottom bar.
- **Pixel 10a, Android 17, blank install:**
  - The notifications step opened the system dialog (`GrantPermissionsActivity`).
  - The exact-alarm step opened Settings › Alarms & reminders.
  - The battery step opened the one-tap "Let app always run in background?" dialog. Charlie denied it, the exemption stayed off, and the banner stayed up.
  - The real permission reads in `AndroidPermissionSource` matched what the phone reported.
- **Not proven by a test:** the first-launch flag through `MainActivity`, because no approved seam covers it. The emulator smoke pass covered it.

