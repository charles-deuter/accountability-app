# 33: Make an existing Task recurring

**What to build:** Editing a one-off Task offers the repeat picker, so the user can turn it into a series without deleting it and starting again from Add Task. Choosing Daily or Weekly creates a Template, and the Task becomes its first Instance.

**Blocked by:** 13

**Status:** needs-triage

Split out of ticket 13 while grilling (2026-11-10): ticket 13's edit form has no repeat picker, because opening a Task from the Task List changes only that one Task.

Open questions for triage:

- Does the Task keep its date and time as the series' first slot, or move to the first occurrence of the new pattern?
- What happens to an active Snooze on the Task?
- Can an Instance be turned back into a one-off (detached from its Template), or is that only done by deleting the Template?
