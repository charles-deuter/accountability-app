# 15: Template List and Edit Template

**What to build:** A second top-level destination. Bottom navigation switches between Tasks and Templates. The Template List shows every recurring series with a readable schedule summary ("Every Monday at 9am", "1st of every month at 10am"). Tapping opens Edit Template; swipe or long-press deletes. Each template can be paused and resumed with the Paused switch that ticket 13 adds to the repeat section; Edit Template follows the same switch rules (a Template saved paused stays paused when a day is ticked).

**Blocked by:** 14

**Status:** ready-for-agent

- [ ] Bottom navigation with Tasks and Templates; state survives switching tabs
- [ ] Each template row shows its title and a schedule summary for every recurrence type
- [ ] A paused Template's row looks disabled and says why: "Paused" when its switch is off, "No days picked" when a Weekly series has no days
- [ ] Editing a template reschedules its pending instance to the new next occurrence
- [ ] Deleting a template (after confirmation) removes its pending instance and cancels that alarm; completed history is untouched
- [ ] Pausing (isActive off) removes the pending instance and its alarm; resuming creates the next instance from now
- [ ] Add Template from this screen uses the same recurrence form as Add Task
- [ ] The read-only "Repeats …" line on an Instance's form (ticket 13) links to its Edit Template
- [ ] Unit tests for the schedule-summary formatter across all recurrence types
- [ ] Integration tests for edit-reschedule, delete-cancel, and pause/resume
