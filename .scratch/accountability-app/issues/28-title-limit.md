# 28: Limit Task titles to 100 characters and shorten them on display

**What to build:** A Task title has a hard limit of 100 characters, and long titles never push controls off the screen.

**Blocked by:** none

**Status:** resolved

Found in QA on 2026-11-06 (BUG-03 failed). 100 is a first try; Charlie may revise it if it proves too short.

- [x] The title field stops accepting input at 100 characters, including pasted text, and shows a character counter
- [x] The limit is one constant, so it's easy to change
- [x] The Task List shows at most 2 lines of a title, ending in an ellipsis
- [x] The takeover shows at most 3 lines of a title, ending in an ellipsis
- [x] Done and Snooze stay reachable on the takeover with a 100-character title, in portrait and landscape
- [x] Tests cover the limit in the Task Form ViewModel and the line limits at the screen level

## Comments

**2026-11-06, agent:** The limit is `TASK_TITLE_MAX_LENGTH` in `data/Task.kt`.
- `limitedTitleOf` runs on every title change, so pasted text stops at 100 too. An emoji across the limit is dropped whole, because cutting it in half would leave a broken character.
- Typing or pasting before the end of a title with no room is refused, because keeping it would cut off the end of what the user wrote.
- A Task saved before this limit stays as it is until its title is edited. The first edit shortens it to 100. Only the form enforces the limit; `TaskService` doesn't.
- The takeover's title gets only the room the buttons leave, so it can't squash them. It shows up to 3 lines, and fewer if the screen is short.
- `TitleDisplayTest` runs real Compose layout through Robolectric. The landscape case uses 800x360dp, the smallest common phone: the old screen squashed Snooze to 4dp there.
