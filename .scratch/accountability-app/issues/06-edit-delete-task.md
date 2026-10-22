# 06: Edit and delete a task

**What to build:** The user taps a task in the list and lands on the same form, prefilled. Saving updates the task in place; a delete action removes it after a confirmation. (The PRD lists delete only for templates; this ticket adds it for tasks so a mistaken one-time reminder can be removed.)

**Blocked by:** 05

**Status:** ready-for-agent

- [ ] Tapping a task opens the form prefilled with its title and time
- [ ] Saving changes updates the existing task rather than creating a new one
- [ ] Delete asks for confirmation, then removes the task from the list
- [ ] Room integration tests cover update and delete
- [ ] ViewModel test covers load-for-edit and save-as-update
