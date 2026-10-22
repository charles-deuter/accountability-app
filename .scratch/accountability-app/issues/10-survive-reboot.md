# 10: Reminders survive reboot and clock changes

**What to build:** After the phone restarts, or the user changes the time or timezone, every pending and snoozed reminder still fires at the right moment without the user opening the app. If this breaks, the app silently stops working — the PRD's risk #4.

**Blocked by:** 08

**Status:** ready-for-agent

- [ ] On boot completed, all pending tasks are rescheduled at their scheduledTime and all snoozed tasks at their snoozedUntil
- [ ] On time-set and timezone-changed broadcasts, the same rescheduling runs
- [ ] Tasks whose time passed while the phone was off fire immediately (once) after boot rather than being skipped
- [ ] Completed tasks are never rescheduled
- [ ] Integration test drives the rescheduling routine against a seeded database and asserts the alarms requested
