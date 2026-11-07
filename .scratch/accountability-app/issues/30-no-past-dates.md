# 30: Don't allow past dates and times in the Task Form

**What to build:** A new Task, or an edit that changes the time, can't be saved with a time in the past.

**Blocked by:** 26, 27, 28, 29

**Status:** ready-for-agent

**On hold:** don't start this until Charlie says so. Past times are handy for testing right now, because they make a Reminder fire on Save.

Design notes:

- Renaming an Overdue Task without changing its time must still save, so the check only runs when the time changes.
- QA test FIRE-08 and any other test relying on a past time will need another way to fire a Reminder at once.

- [ ] The date picker greys out days before today
- [ ] Saving a past time shows an error and saves nothing
- [ ] The current minute counts as past
- [ ] A default time that passes while the form sits open gets the same error
- [ ] Renaming an Overdue Task without changing its time still saves
- [ ] Tests cover these in the Task Form ViewModel
