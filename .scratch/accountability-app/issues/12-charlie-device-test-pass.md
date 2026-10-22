# 12: [Charlie] Grant special permissions and run the device test pass

**What to build:** Proof on real hardware that the notification engine does its job. Only Charlie can do this: it needs the physical phone, system-settings taps, and a reboot.

**Blocked by:** 04, 10, 11

**Status:** ready-for-human

- [ ] Install the latest build on the phone and complete the in-app onboarding: exact alarms, full-screen intent, notifications, battery-optimization exemption
- [ ] Lock-screen takeover: lock the phone, set a reminder two minutes out — takeover appears with Done/Snooze, no unlock needed
- [ ] DND bypass: enable Do Not Disturb, repeat — sound and vibration still fire
- [ ] Sound and vibration fire with the phone on vibrate-only and on silent
- [ ] Snooze from the lock screen re-fires at the chosen time
- [ ] Reboot: set a reminder ten minutes out, restart the phone, don't open the app — it still fires
- [ ] Leave a reminder overnight on battery — it fires (checks OEM battery killing)
- [ ] Record pass/fail per item and any OEM quirks in a `## Comments` section on this ticket; open new tickets for failures
