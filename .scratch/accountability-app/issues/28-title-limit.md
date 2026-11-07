# 28: Limit Task titles to 100 characters and shorten them on display

**What to build:** A Task title has a hard limit of 100 characters, and long titles never push controls off the screen.

**Blocked by:** none

**Status:** ready-for-agent

Found in QA on 2026-11-06 (BUG-03 failed). 100 is a first try; Charlie may revise it if it proves too short.

- [ ] The title field stops accepting input at 100 characters, including pasted text, and shows a character counter
- [ ] The limit is one constant, so it's easy to change
- [ ] The Task List shows at most 2 lines of a title, ending in an ellipsis
- [ ] The takeover shows at most 3 lines of a title, ending in an ellipsis
- [ ] Done and Snooze stay reachable on the takeover with a 100-character title, in portrait and landscape
- [ ] Tests cover the limit in the Task Form ViewModel and the line limits at the screen level
