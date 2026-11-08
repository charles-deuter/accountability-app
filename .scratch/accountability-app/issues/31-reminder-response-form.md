# 31: Open a form with Done and the Snooze presets when a Reminder is tapped

**What to build:** Tapping a Reminder's notification opens a small form for that Task, instead of the Task List: its title, Done, and the five Snooze presets on one screen. One tap decides, and the user goes back to what they were doing.

**Blocked by:** 26

**Status:** needs-info

Raised by Charlie on 2026-11-06 while deciding ticket 26 ("it may make sense"), so the idea isn't committed yet.

**Open question for Charlie:** what happens when the user leaves the form without choosing?

- **Recommended: nothing.** The Reminder stays silenced in the shade with no Snooze, as when leaving the takeover. A tap may only mean "let me see this", and the Reminder is still there to decide on.
- **Alternative: Snooze 15 minutes,** as when leaving the Snooze picker. This follows "a Reminder always demands a decision" more strictly, but an accidental tap would postpone the Task.

Design notes:

- Ticket 26 makes the tap silence from `MainActivity`. This form takes over that job, and the tap no longer opens the Task List.
- It can reuse `SnoozeScreen`'s presets, and needs the same landscape scrolling (ticket 29).
- The notification's own Snooze button and the takeover stay as they are.

- [ ] Tapping a Reminder opens the form for that Task and stops the ringing
- [ ] The form shows the title (at most 3 lines), Done, and all five presets, reachable in portrait and landscape
- [ ] Done completes the Task, and a preset snoozes it for that long, each returning the user to what they were doing
- [ ] Leaving without choosing does what Charlie decides above
- [ ] Rotating doesn't count as leaving
- [ ] Tests cover these at the ViewModel and screen level
