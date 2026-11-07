# 27: Renaming a Snoozed Task keeps the Snooze

**What to build:** Changing only a Snoozed Task's title leaves its Snooze alone. Only changing its time ends the Snooze. Today any save ends it, so fixing a typo rings the Reminder straight away once the original time has passed.

**Blocked by:** none

**Status:** resolved

Found in QA on 2026-11-06 (BUG-01 failed).

Design notes:

- **Cause:** the update query always sets the Task back to Pending and clears its snoozed-until time, and the form sends back the original time.
- **No schema change.** Charlie raised giving each firing and Snooze its own immutable record, and will look at that once the app is further along. This ticket doesn't start that work.
- **Default chosen while ticketing, Charlie can overrule:** when editing a Snoozed Task, the form shows its Fire time (the snoozed-until time, matching the list badge), not the original time. Changing that value is what counts as changing the time.

- [x] Changing only the title keeps the Task Snoozed, with the same snoozed-until time and Snooze count
- [x] Changing only the title fires no Reminder and doesn't move the alarm
- [x] The form shows a Snoozed Task's Fire time
- [x] Changing the time ends the Snooze: the Task is Pending at the new time, and the Snooze count stays as history
- [x] Editing a Task that isn't Snoozed behaves as it does today
- [x] Tests cover these through `TaskService` on real Room and the Task Form ViewModel

## Comments

**2026-11-06, agent:** Bug fix only, no schema change.
- `TaskService.update` compares the submitted time with the Task's Fire time. A match renames the Task and leaves its status, snoozed-until time and Snooze count alone. Any other time goes through the existing update, which ends the Snooze and keeps the count.
- The form loads `fireTimeOf(task)`, so a Snoozed Task shows the same time as its list badge. **Charlie hasn't confirmed this default yet.**
- The comparison is by the minute. The time picker has no seconds and a snoozed-until time does, so tapping OK without changing the time would otherwise have counted as a change.
- `TaskServiceTest` and `TaskFormViewModelTest` cover the behaviour, and `RemindersTest` covers the alarm and notification.
