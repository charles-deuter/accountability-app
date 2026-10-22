# 11: Permission onboarding and missing-permission warnings

**What to build:** On first launch the app walks the user through the four grants that make reminders reliable, explaining each in one sentence and deep-linking to the right system settings screen. Afterwards, whenever any grant is missing, the Task List shows a banner that leads back to the fix. Re-checks run every time the app returns to the foreground.

**Blocked by:** 09

**Status:** ready-for-agent

Permissions: exact alarms (Android 12+), full-screen intent (Android 14+), notifications (Android 13+), battery-optimization exemption (all versions; OEMs kill alarms otherwise — PRD risk #3).

- [ ] Onboarding shows only the steps relevant to the device's Android version
- [ ] Each step opens the correct system settings screen and reflects the new state on return
- [ ] The Task List banner appears when any grant is missing and disappears once all are granted
- [ ] Unit tests cover the "which grants are missing" logic across API levels using a fake permission source
- [ ] Without the exact-alarm grant, saving a reminder still works and the banner explains reminders may drift
