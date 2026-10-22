# 11: Permission onboarding and missing-permission warnings

**What to build:** On first launch the app walks the user through the four grants that make reminders reliable, explaining each in one sentence and deep-linking to the right system settings screen. Afterwards, whenever any grant is missing, the Task List shows a banner that leads back to the fix. Re-checks run every time the app returns to the foreground.

**Blocked by:** 09

**Status:** ready-for-agent

Permissions: exact alarms (Android 12+), full-screen intent (Android 14+), notifications (Android 13+), battery-optimization exemption (all versions; OEMs kill alarms otherwise — PRD risk #3).

Play policy notes (from Google's Alarms and Doze docs):

- Declare `SCHEDULE_EXACT_ALARM` (user-granted, revocable), not `USE_EXACT_ALARM`. `USE_EXACT_ALARM` is auto-granted but Play restricts it to a limited set of use cases such as alarm clocks and calendars, and a reminder app may not pass review. Revocable means the app must handle losing the grant at any time; the banner covers that.
- Play prohibits asking for the battery-optimization exemption unless "the core function of the app is adversely affected". On-time reminders are this app's core function, and the onboarding copy for that step should say so plainly; ticket 21 makes the same case in the Console declaration.

- [ ] Onboarding shows only the steps relevant to the device's Android version
- [ ] Each step opens the correct system settings screen and reflects the new state on return
- [ ] The Task List banner appears when any grant is missing and disappears once all are granted
- [ ] Unit tests cover the "which grants are missing" logic across API levels using a fake permission source
- [ ] Without the exact-alarm grant, saving a reminder still works and the banner explains reminders may drift
