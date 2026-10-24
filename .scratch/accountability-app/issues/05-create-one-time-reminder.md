# 05: Create a one-time reminder and see it in the list

**What to build:** The first tracer bullet through storage and UI. The user taps the FAB on the Task List, enters a title and a date/time, saves, and sees the reminder in a single chronological list. Reminders whose time has passed and are not completed show in red/urgent styling. Reminders survive an app restart. No alarms yet.

**Blocked by:** 03

**Status:** resolved

Domain notes: a **Task** has id, title, scheduledTime, templateId (nullable — set later by ticket 13), status (pending / snoozed / completed), snoozeCount, snoozedUntil, completedAt, createdAt. Recurrence does NOT live on Task; it lives on Template (ticket 13).

- [x] Task List is the home screen and shows non-completed tasks ordered by their next fire time
- [x] FAB opens Add Task with a required title and a date/time picker; saving with an empty title is blocked with a visible message
- [x] Overdue tasks (fire time in the past, not completed) are visually distinct — covered by a test on the overdue rule
- [x] Tasks persist across an app process restart
- [x] A task that becomes overdue while the Task List is open turns red within a minute: the list re-evaluates on a one-minute tick while the screen is visible, and on return to the foreground. Nothing runs in the background for this
- [x] Room integration tests cover insert, query ordering, and the overdue query
- [x] ViewModel/unit test covers the title-required validation

## Comments

**2026-10-21: resolved on branch `feat/05-one-time-reminder` (stacked on ticket 03's branch)**

Tests, each written red first:

- `TaskRepositoryTest` (JVM, real Room via Robolectric): added Task appears; Task List ordered by Fire time; a Completed Task leaves the list; Tasks survive closing and reopening the production database file
- `AddTaskViewModelTest`: a blank title shows "Title is required" and saves nothing; a valid title and time save the Task
- `TaskListViewModelTest`: Overdue versus future Tasks; a Task turns Overdue within a minute while the list is observed, with no database change
- `AddTaskEndToEndTest` (emulator): FAB → title → Save → the Task appears in the Task List
- Smoke check on the emulator: a blank Save shows the error

Notes and deviations:

- **Overdue is computed in the ViewModel, not by a database query.** It has to re-evaluate every minute without a database change, so the Task List ViewModel combines the Task stream with a one-minute tick. The tick runs only while the screen collects state, and restarts with a fresh time when the screen returns. Ticket text said "the overdue query"; the overdue rule is covered by `TaskListViewModelTest` instead.
- **Fire time is the scheduled time for now.** Ticket 08 makes it follow the snoozed-until time.
- **`TaskRepository.complete()` exists** because the agreed "completed Tasks leave the list" test needs it. Ticket 07's Done action should call it.
- **minSdk raised from 24 to 26** so `java.time` works without core-library desugaring.
- **Room schema export is on** (`android/app/schemas/`). Commit each schema version; future migrations need them.
- New Tasks default to the top of the next hour.
- No DI framework: an `AppContainer` on the `Application` holds the database, repository, and clock.
