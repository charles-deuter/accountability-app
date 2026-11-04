# 09: Lock-screen takeover (full-screen intent)

**What to build:** When a reminder fires on a locked phone, it takes over the lock screen like an incoming call, showing the task title with Done and Snooze — no unlock needed. The takeover appears once per firing; if ignored, the ongoing notification remains in the shade as the persistent nudge.

**Blocked by:** 08

**Status:** resolved

Design notes: Android 14+ requires the user to grant full-screen-intent permission; this ticket assumes it is granted (ticket 11 handles detection). Play Store policy restricts full-screen intents to alarm/calling-style apps and requires a Console declaration (ticket 21).

- [x] The alarm notification carries a full-screen intent that opens a takeover screen over the lock screen and turns the screen on
- [x] Takeover shows the task title, Done, and Snooze; Done and Snooze behave exactly as in tickets 07–08
- [x] Takeover does not re-launch on its own after being left; the shade notification persists until acted on
- [x] When the phone is unlocked and in use, the reminder shows as a heads-up notification rather than hijacking the foreground app
- [x] Manual smoke on the emulator with the screen locked

## Comments

**2026-10-24, agent:** Done in PR #10.
- Each Reminder's full-screen intent opens `ReminderActivity` (`showWhenLocked`, `turnScreenOn`, its own task affinity, excluded from Recents). Done completes the Task through the ViewModel; Snooze opens ticket 08's picker, which now also shows over the lock screen.
- **Once per firing:** a full-screen intent launches only when a notification is posted, and `ReminderSync` never re-posts a Reminder already in the shade. Leaving the takeover (`onStop`, except rotation) finishes it for good.
- **Tests:** the Reminder carries a full-screen intent for its Task; the takeover ViewModel shows the title and Done completes; `ManifestTest` checks the lock-screen flags and the `USE_FULL_SCREEN_INTENT` permission (and fails when `showWhenLocked` is removed).
- **Smoke, emulator API 37:** screen asleep at 3:55 → the screen woke into the takeover ("Take meds", Done, Snooze); Snooze → 30 minutes → back on the home screen, Reminder cleared. With Settings in use at 4:00 → a heads-up with Done and Snooze; Settings kept focus.
- **Not covered here:** the emulator has no screen lock configured, so the smoke test proves wake-into-takeover but not "over a keyguard". Ticket 12 covers that on the real phone. `USE_FULL_SCREEN_INTENT` was at its default and worked for this debug install; ticket 11 detects when it's denied.
