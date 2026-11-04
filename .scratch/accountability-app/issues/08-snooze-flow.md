# 08: Snooze from the notification

**What to build:** The notification gains a **Snooze** action. Tapping it opens a picker with five presets — 15 minutes, 30 minutes, 1 hour, 2 hours, 1 day. One tap snoozes and returns the user to whatever they were doing. There is no way out without snoozing: back defaults to 15 minutes. Snoozed tasks stay in the list with a badge like "Snoozed 3x — until 3:00 PM". No escalation — each re-fire is identical.

**Blocked by:** 07

**Status:** resolved

- [x] Notification shows Snooze alongside Done
- [x] Snooze opens the picker with exactly the five presets
- [x] Picking a preset sets status to snoozed, increments snoozeCount, sets snoozedUntil, reschedules the alarm, and clears the current notification
- [x] Back (or any other exit) from the picker snoozes for 15 minutes
- [x] The Task List badge shows the snooze count and the snoozed-until time, and the list orders snoozed tasks by snoozedUntil
- [x] When a snoozed alarm fires, the same notification appears again
- [x] Swiping the Reminder away (possible on an unlocked phone since Android 14) snoozes it for 30 minutes, so it can't be dismissed (decided 2026-10-24)
- [x] Unit tests cover snooze-until calculation for every preset and the back-button default

## Comments

**2026-10-24, agent:** Done in PR #9.
- `fireTimeOf` returns snoozed-until while Snoozed, so ticket 07's sync does the rest: snoozing clears the Reminder and moves the alarm; when the Snooze runs out the Reminder comes back. The Task List orders by the same Fire time in SQL.
- Snoozing is one SQL statement (`snoozeCount = snoozeCount + 1`). Editing a snoozed Task ends the Snooze (back to Pending at the new time); the count stays as history.
- The picker is a translucent `SnoozeActivity` over whatever the user was doing. Back snoozes 15 minutes; so does leaving any other way (`onStop`, except rotation). A choice can't be followed by a second, default snooze.
- Swiping the Reminder away (its delete intent) snoozes for 30 minutes.
- **Smoke, emulator API 37:** three Reminders at 3:40. Swiped one → snoozed until 4:10 (30 min from the swipe); picked 1 hour on one → back on the home screen, Reminder cleared; pressed Back in the picker on one → snoozed 15 min. The Task List showed "Snoozed 1x — until …" for each, ordered by snoozed-until.
- **Follow-up observation:** when two or more Reminders are due at once, Android bundles them, and each one's Done/Snooze sits behind an extra expand tap. A per-Reminder notification group didn't stop the bundling on Android 17. Ticket 09's takeover shows Done and Snooze directly; ticket 12 should judge on the real phone whether the bundled case needs more.
