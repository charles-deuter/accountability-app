# 13: Recurring reminders — daily and weekly

**What to build:** The user picks a recurrence in Add Task: once, daily, or weekly on chosen weekdays. A recurring reminder is backed by a **Template**: saving creates the Template and its first pending Instance. Completing an Instance silently creates and schedules the next one. Snoozing, completing late or early, and moving an Instance never shift the series. The Task List handles one Instance at a time; changing the series itself belongs to the Template List (ticket 15).

**Blocked by:** 07

**Status:** ready-for-agent

Domain decision (made while ticketing): **Template owns recurrence.** A Template has id, title, recurrenceType, recurrenceData (weekday bitmask or day-of-month), time of day, isActive, createdAt. A Template keeps at most one pending Instance alive (linked via the Task's templateId), and exactly one while it is active and has a next occurrence. Task itself has no recurrence fields. One code path answers "when's next?". This also matches the v2 starter-pack data model.

Decisions from grilling with Charlie (2026-11-10):

- **Picker:** Repeat offers **Once / Daily / Weekly**. Weekly shows day chips (M T W T F S S) and covers both "every Tuesday" and "Mon, Wed, Fri". Daily and Weekly show only a time picker; Once keeps today's date and time pickers.
- **Paused switch:** the repeat section has a switch for the Template's `isActive`. Unticking the last day flips it to Paused, and it stays locked there while no days are ticked. Ticking a day flips it back to Active only if it auto-paused during this edit; a Template saved paused stays paused. Saving a paused series creates the Template and no Instance. The switch itself is the signal: no extra hint text.
- **First Instance:** the first matching time still ahead of now. Saving "Mondays 9:00" at 8:00 on a Monday gives today 9:00; at 9:30, next Monday.
- **Next Instance:** the first occurrence after the later of the Instance's series slot and now. The slot is the Instance's `originalScheduledTime`, which never changes after a Task is created (reschedule stops overwriting it). Missed occurrences are skipped, not replayed.
- **Time zone:** occurrences use the phone's current zone. A pending Instance keeps its instant when the zone changes (ticket 34 covers moving it).
- **Database:** version 3 adds the Template table through a Room AutoMigration from 2, so existing Tasks survive.

- [ ] Add Task shows Once / Daily / Weekly; Weekly shows day chips and a time picker, Daily a time picker
- [ ] The Paused switch follows the rules above: auto-pause on the last untick, locked while no days are ticked, auto-resume only within the same edit
- [ ] Saving an active recurring reminder creates one Template and one pending Instance at the first occurrence still ahead of now; saving a paused one creates the Template only
- [ ] Done on an Instance (in the app or from the Reminder) completes it and creates the next Instance from its Template, scheduled with an alarm, with no prompt
- [ ] The next occurrence comes from the series slot, not from when the user tapped Done, how long they snoozed, or where they moved the Instance
- [ ] Next-occurrence logic is a pure function with unit tests for daily, weekday edges (e.g. Fri → Mon), a single weekday, and same-day-already-passed
- [ ] Room integration test covers the complete-and-spawn transaction (no duplicate pending Instances)
- [ ] Opening an Instance from the Task List edits only that Instance; the form has no repeat picker and shows a read-only line such as "Repeats Mon, Wed, Fri at 9:00"
- [ ] On an Instance, Delete becomes **Skip**: the confirmation names the next run, and skipping removes this Instance and creates the next
- [ ] Instance rows in the Task List show a small repeat icon
- [ ] Upgrading from database version 2 keeps existing Tasks
