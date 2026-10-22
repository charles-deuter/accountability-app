# 13: Recurring reminders — daily, specific weekdays, weekly

**What to build:** The user picks a recurrence in Add Task — once, daily, specific weekdays (with a day-of-week selector), or weekly. A recurring reminder is backed by a **Template**: saving creates the Template and its first pending Task instance. Completing an instance silently creates and schedules the next one. Snoozing an instance does not shift the series.

**Blocked by:** 07

**Status:** ready-for-agent

Domain decision (made while ticketing): **Template owns recurrence.** A Template has id, title, recurrenceType, recurrenceData (weekday bitmask or day-of-month), time of day, isActive, createdAt. A Template keeps exactly one pending Task instance alive (linked via the Task's templateId). Task itself has no recurrence fields. One code path answers "when's next?". This also matches the v2 starter-pack data model.

- [ ] Add Task shows a recurrence picker; choosing specific weekdays shows a day-of-week selector requiring at least one day
- [ ] Saving a recurring reminder creates one Template and one pending Task instance scheduled at the first occurrence on or after now
- [ ] Done on an instance completes it and creates the next instance from its Template, scheduled with an alarm, with no prompt
- [ ] The next occurrence is computed from the series schedule, not from when the user tapped Done or how long they snoozed
- [ ] Next-occurrence logic is a pure function with unit tests for daily, each weekday combination edge (e.g. Fri → Mon), weekly, and same-day-already-passed
- [ ] Room integration test covers the complete-and-spawn transaction (no duplicate pending instances)
- [ ] Editing a recurring instance from the Task List edits its Template (applies to future instances)
