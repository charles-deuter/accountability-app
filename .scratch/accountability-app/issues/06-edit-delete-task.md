# 06: Edit and delete a task

**What to build:** The user taps a task in the list and lands on the same form, prefilled. Saving updates the task in place; a delete action removes it after a confirmation. (The PRD lists delete only for templates; this ticket adds it for tasks so a mistaken one-time reminder can be removed.)

**Blocked by:** 05

**Status:** resolved

- [x] Tapping a task opens the form prefilled with its title and time
- [x] Saving changes updates the existing task rather than creating a new one
- [x] Delete asks for confirmation, then removes the task from the list
- [x] Room integration tests cover update and delete
- [x] ViewModel test covers load-for-edit and save-as-update

## Comments

**2026-10-24, agent:** Done in PR #7.
- The Add Task form is now the Task Form (`ui/taskform`) and serves both add and edit; the Task List row opens it with an `EditTask(taskId)` key.
- Fixed a ticket 05 bug found on the way: Navigation 3 kept ViewModels at Activity scope, so a second Add Task visit reused the saved ViewModel and closed at once. Each back-stack entry now gets its own ViewModel store.
- Tests: Room update and delete; ViewModel prefill, save-as-update, delete; device tests for adding twice, editing, and delete-with-confirmation.
