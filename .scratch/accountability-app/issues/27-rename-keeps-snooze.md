# 27: Renaming a Snoozed Task keeps the Snooze

**What to build:** Changing only a Snoozed Task's title leaves its Snooze alone. Only changing its time ends the Snooze. Today any save ends it, so fixing a typo rings the Reminder straight away once the original time has passed.

**Blocked by:** none

**Status:** ready-for-agent

Found in QA on 2026-11-06 (BUG-01 failed).

Design notes:

- **Cause:** the update query always sets the Task back to Pending and clears its snoozed-until time, and the form sends back the original time.
- **No schema change.** Charlie raised giving each firing and Snooze its own immutable record, and will look at that once the app is further along. This ticket doesn't start that work.
- **Default chosen while ticketing, Charlie can overrule:** when editing a Snoozed Task, the form shows its Fire time (the snoozed-until time, matching the list badge), not the original time. Changing that value is what counts as changing the time.

- [ ] Changing only the title keeps the Task Snoozed, with the same snoozed-until time and Snooze count
- [ ] Changing only the title fires no Reminder and doesn't move the alarm
- [ ] The form shows a Snoozed Task's Fire time
- [ ] Changing the time ends the Snooze: the Task is Pending at the new time, and the Snooze count stays as history
- [ ] Editing a Task that isn't Snoozed behaves as it does today
- [ ] Tests cover these through `TaskService` on real Room and the Task Form ViewModel
