# 07: Reminder fires as a notification you cannot dismiss

**What to build:** The highest-risk path, proven end to end early. When a task's time arrives, the phone rings on the alarm channel and posts an ongoing notification that cannot be swiped away. Its only action in this ticket is **Done**, which marks the task completed, removes it from the list, and clears the notification. Saving, editing, and deleting a task keep its alarm in sync.

**Blocked by:** 06

**Status:** resolved

Design notes:

- Notification channels cannot be changed by the app after creation on the user's device, so pick the channel ID, importance (alarm/high), sound, and vibration deliberately.
- **No polling, no background service.** Android's AlarmManager holds the schedule; the app process can be dead between reminders. When the alarm fires, Android starts the app just long enough for a broadcast receiver to read the database and post the notification (receivers get roughly 10 seconds, so do the database read asynchronously within that budget).
- **One alarm, for the next due time.** Only the soonest fire time across all pending and snoozed tasks has an alarm. When it fires, the receiver posts a notification for every task due at that moment, then schedules the alarm for the next due time. Any change to tasks (save, edit, delete, Done, snooze) recomputes that single alarm. Same-time reminders therefore share one alarm.
- **Use `setExactAndAllowWhileIdle()`**, which fires on time even in Doze (the phone's deep idle state: screen off, unplugged, stationary). Its known limit, per Google's docs: while the phone is in Doze it "cannot fire alarms more than once per nine minutes, per app". Two reminders 1–8 minutes apart during deep Doze can see the second arrive up to ~9 minutes late, at full intrusiveness. Accepted for v1; ticket 12 measures it on the real phone.
- **`setAlarmClock()` was considered and rejected for v1.** It is never delayed, but it lights the system-wide alarm icon whenever any alarm is pending (effectively always, with recurring reminders), shows reminder times as the phone's "next alarm" on the lock screen and clock widgets ahead of the user's real wake-up alarm, and can end a Do Not Disturb schedule early via "Alarm can override end time". If ticket 12 shows the Doze delay matters in practice, later options are: switch to `setAlarmClock()` only for a reminder within nine minutes of the previous one, or a "Strict timing (shows alarm icon)" setting.

- [x] An alarm-importance notification channel is created at app startup, with sound and vibration on
- [x] Saving, editing, and deleting a task recompute the single next alarm with `setExactAndAllowWhileIdle()`; with no pending tasks, no alarm is set
- [x] When the alarm fires, every task due at that moment gets its own ongoing (non-swipeable) notification with the task title and a Done action, and the next alarm is scheduled
- [x] Done marks the task completed (sets completedAt), cancels the notification, and the task leaves the Task List
- [x] The app requests the notifications permission on Android 13+ before the first reminder is saved
- [x] Integration tests through the scheduling abstraction cover: save, edit, and delete each recompute the next alarm; two tasks at the same time produce one alarm and two notifications; firing schedules the following alarm
- [x] Unit test covers the Done transition (pending → completed)
- [x] Manual smoke on the emulator: a reminder set one minute out fires and Done clears it; repeat with the emulator forced into Doze (`adb shell dumpsys deviceidle force-idle`) and confirm it still fires on time

## Comments

**2026-10-24, agent:** Done in PR #8.
- **Design:** one `ReminderSync.sync()` that every trigger calls. It posts a Reminder for each due Task not already in the shade, cancels shown Reminders whose Task is no longer due, and sets the one alarm to the next future Fire time (or cancels it). `TaskService` (renamed from `TaskRepository`) runs it after every write; the alarm receiver runs it when the alarm fires; the Done receiver completes the Task, whose sync clears the Reminder.
- **Exact-alarm permission:** not pre-granted from Android 14. Without it, `ReminderAlarm` falls back to `setAndAllowWhileIdle` (inexact, may be minutes late) instead of throwing. Ticket 11 asks the user for it.
- **Notifications permission:** asked for when the Task Form opens (not on Save, so it never collides with title validation).
- **Smoke, emulator API 37:** with the app process killed, a 3:15 Reminder fired at 3:15:00 and started the app; Done cleared it and the Task left the list. Repeated in forced deep Doze (screen off, unplugged, `IDLE`): the 3:20 Reminder fired at 3:20:00.
- **Known gap, fixed in ticket 08:** since Android 14, an unlocked user can swipe away an ongoing notification. Ticket 08 makes a swipe snooze the Reminder for 30 minutes.
