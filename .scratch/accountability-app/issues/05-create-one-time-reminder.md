# 05: Create a one-time reminder and see it in the list

**What to build:** The first tracer bullet through storage and UI. The user taps the FAB on the Task List, enters a title and a date/time, saves, and sees the reminder in a single chronological list. Reminders whose time has passed and are not completed show in red/urgent styling. Reminders survive an app restart. No alarms yet.

**Blocked by:** 03

**Status:** ready-for-agent

Domain notes: a **Task** has id, title, scheduledTime, templateId (nullable — set later by ticket 13), status (pending / snoozed / completed), snoozeCount, snoozedUntil, completedAt, createdAt. Recurrence does NOT live on Task; it lives on Template (ticket 13).

- [ ] Task List is the home screen and shows non-completed tasks ordered by their next fire time
- [ ] FAB opens Add Task with a required title and a date/time picker; saving with an empty title is blocked with a visible message
- [ ] Overdue tasks (fire time in the past, not completed) are visually distinct — covered by a test on the overdue rule
- [ ] Tasks persist across an app process restart
- [ ] Room integration tests cover insert, query ordering, and the overdue query
- [ ] ViewModel/unit test covers the title-required validation
