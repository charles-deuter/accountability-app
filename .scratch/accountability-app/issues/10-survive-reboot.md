# 10: Reminders survive reboot and clock changes

**What to build:** After the phone restarts, the app is updated, or the user changes the time or timezone, every pending and snoozed reminder still fires at the right moment without the user opening the app. If this breaks, the app silently stops working — the PRD's risk #4.

Design note: Android erases all alarms on reboot, and on Force stop (Settings → Force stop; swiping from recents does not). Under ticket 07's one-alarm design, "rescheduling" means recomputing the single next alarm from the database, so it is cheap to run on every trigger, including app launch.

**Blocked by:** 08

**Status:** resolved

- [x] On boot completed, the next alarm is recomputed from all pending tasks (scheduledTime) and snoozed tasks (snoozedUntil)
- [x] The same recompute runs on time-set, timezone-changed, and app-updated (package replaced) broadcasts, and on every app launch (recovers from Force stop)
- [x] Tasks whose time passed while the phone was off fire immediately (once) after boot rather than being skipped
- [x] Completed tasks are never rescheduled
- [x] Integration test drives the rescheduling routine against a seeded database and asserts the alarms requested

## Comments

**2026-10-24, agent:** Done in PR #11.
- `RescheduleReceiver` runs `ReminderSync.sync()` on `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED` and `MY_PACKAGE_REPLACED`; `MainActivity` runs it on every launch (Force stop recovery). The receiver is not exported: system broadcasts still reach it (confirmed on the emulator).
- The sync is serialized with a `Mutex`, since a launch and a receiver can now overlap.
- **Tests:** `ManifestTest` checks that all four broadcasts route to the receiver and that `RECEIVE_BOOT_COMPLETED` is declared. `RemindersTest` drives the sync against a database seeded behind its back (as before a reboot): overdue Pending and Snoozed Tasks are reminded, a Completed one never, and the alarm goes to the next Fire time. `LaunchRecoveryTest` launches `MainActivity` with an overdue Task and expects its Reminder.
- **Smoke, emulator API 37:** rebooted at 4:09:30 with Reminders at 4:10 and 4:15, app not opened. Android delivered the boot broadcast 36 seconds after boot completed (it queues them); the receiver then posted the 4:10 Reminder at once and set the 4:15 alarm. Force stop cleared the Reminder; opening the app restored it and the alarm.
- **Not smoke-tested:** clock and timezone changes (setting the emulator clock needs root). The manifest test and the seeded-database test cover the same routine.
- **Observation for ticket 12:** boot broadcasts can arrive tens of seconds after the phone is usable, so a Reminder due during that gap posts late by that much.
