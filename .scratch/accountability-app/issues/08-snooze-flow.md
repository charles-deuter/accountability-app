# 08: Snooze from the notification

**What to build:** The notification gains a **Snooze** action. Tapping it opens a picker with five presets — 15 minutes, 30 minutes, 1 hour, 2 hours, 1 day. One tap snoozes and returns the user to whatever they were doing. There is no way out without snoozing: back defaults to 15 minutes. Snoozed tasks stay in the list with a badge like "Snoozed 3x — until 3:00 PM". No escalation — each re-fire is identical.

**Blocked by:** 07

**Status:** ready-for-agent

- [ ] Notification shows Snooze alongside Done
- [ ] Snooze opens the picker with exactly the five presets
- [ ] Picking a preset sets status to snoozed, increments snoozeCount, sets snoozedUntil, reschedules the alarm, and clears the current notification
- [ ] Back (or any other exit) from the picker snoozes for 15 minutes
- [ ] The Task List badge shows the snooze count and the snoozed-until time, and the list orders snoozed tasks by snoozedUntil
- [ ] When a snoozed alarm fires, the same notification appears again
- [ ] Swiping the Reminder away (possible on an unlocked phone since Android 14) snoozes it for 30 minutes, so it can't be dismissed (decided 2026-10-24)
- [ ] Unit tests cover snooze-until calculation for every preset and the back-button default
