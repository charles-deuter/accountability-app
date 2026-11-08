# 32: A ringing Reminder keeps vibrating

**What to build:** A Reminder vibrates for as long as it rings, not just once as it starts. Vibration stops whenever the sound stops.

**Blocked by:** none

**Status:** ready-for-agent

Found on the Pixel on 2026-11-07 while confirming ticket 26: `dumpsys vibrator_manager` showed each Reminder vibrating once, for about 1 second, as it started, and nothing after that while the sound kept ringing. Charlie decided it should keep vibrating.

Design notes:

- **Likely route: mark the ringing notification insistent** (`Notification.FLAG_INSISTENT`), which makes Android repeat its alert until it is cancelled or updated. It is a flag on the notification, not the channel, so the ringing channel's permanent settings don't change. Unverified: Android's documentation promises the repeat for audio; whether vibration repeats too has to be proved on the Pixel before relying on it.
- Ticket 26 silences a Reminder by replacing its notification in place on the silent channel. The silenced copy must not be insistent, and the Pixel has to show the vibration stopping at the replacement.
- Vibrating from the app with a repeating `Vibrator` pattern would need something running for the whole ring, which points to a foreground service. Ask Charlie before going that way: it adds a Play declaration.
- `setOnlyAlertOnce(true)` stays: it stops a re-posted Reminder from alerting again, which is separate from repeating the first alert.

- [ ] A ringing Reminder vibrates for as long as it rings
- [ ] The vibration stops at the same moment as the sound, for every trigger in ticket 26: Done, Snooze, leaving the takeover, tapping the notification, swiping it away, and the 5-minute limit
- [ ] A silenced Reminder doesn't vibrate
- [ ] A JVM test pins the ringing notification's alert setup, and the silenced copy's
- [ ] Charlie confirms on the Pixel, with `dumpsys vibrator_manager` (see `docs/agents/pixel.md`) showing the vibration running while it rings and stopping with the sound
