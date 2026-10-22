# 10: Reminders survive reboot and clock changes

**What to build:** After the phone restarts, the app is updated, or the user changes the time or timezone, every pending and snoozed reminder still fires at the right moment without the user opening the app. If this breaks, the app silently stops working — the PRD's risk #4.

Design note: Android erases all alarms on reboot, and on Force stop (Settings → Force stop; swiping from recents does not). Under ticket 07's one-alarm design, "rescheduling" means recomputing the single next alarm from the database, so it is cheap to run on every trigger, including app launch.

**Blocked by:** 08

**Status:** ready-for-agent

- [ ] On boot completed, the next alarm is recomputed from all pending tasks (scheduledTime) and snoozed tasks (snoozedUntil)
- [ ] The same recompute runs on time-set, timezone-changed, and app-updated (package replaced) broadcasts, and on every app launch (recovers from Force stop)
- [ ] Tasks whose time passed while the phone was off fire immediately (once) after boot rather than being skipped
- [ ] Completed tasks are never rescheduled
- [ ] Integration test drives the rescheduling routine against a seeded database and asserts the alarms requested
