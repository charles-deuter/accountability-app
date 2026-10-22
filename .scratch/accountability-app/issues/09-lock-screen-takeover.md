# 09: Lock-screen takeover (full-screen intent)

**What to build:** When a reminder fires on a locked phone, it takes over the lock screen like an incoming call, showing the task title with Done and Snooze — no unlock needed. The takeover appears once per firing; if ignored, the ongoing notification remains in the shade as the persistent nudge.

**Blocked by:** 08

**Status:** ready-for-agent

Design notes: Android 14+ requires the user to grant full-screen-intent permission; this ticket assumes it is granted (ticket 11 handles detection). Play Store policy restricts full-screen intents to alarm/calling-style apps and requires a Console declaration (ticket 21).

- [ ] The alarm notification carries a full-screen intent that opens a takeover screen over the lock screen and turns the screen on
- [ ] Takeover shows the task title, Done, and Snooze; Done and Snooze behave exactly as in tickets 07–08
- [ ] Takeover does not re-launch on its own after being left; the shade notification persists until acted on
- [ ] When the phone is unlocked and in use, the reminder shows as a heads-up notification rather than hijacking the foreground app
- [ ] Manual smoke on the emulator with the screen locked
