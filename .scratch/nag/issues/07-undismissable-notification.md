# 07: Reminder fires as a notification you cannot dismiss

**What to build:** The highest-risk path, proven end to end early. When a task's time arrives, the phone rings on the alarm channel and posts an ongoing notification that cannot be swiped away. Its only action in this ticket is **Done**, which marks the task completed, removes it from the list, and clears the notification. Saving, editing, and deleting a task keep its alarm in sync.

**Blocked by:** 06

**Status:** ready-for-agent

Design notes: notification channels cannot be changed by the app after creation on the user's device, so pick the channel ID, importance (alarm/high), sound, and vibration deliberately. Use exact alarms so reminders don't drift.

- [ ] An alarm-importance notification channel is created at app startup, with sound and vibration on
- [ ] Saving a task schedules an exact alarm for its time; editing reschedules it; deleting cancels it
- [ ] When the alarm fires, an ongoing (non-swipeable) notification shows the task title and a Done action
- [ ] Done marks the task completed (sets completedAt), cancels the notification, and the task leaves the Task List
- [ ] The app requests the notifications permission on Android 13+ before the first reminder is saved
- [ ] Integration tests cover schedule-on-save, reschedule-on-edit, and cancel-on-delete through the scheduling abstraction
- [ ] Unit test covers the Done transition (pending → completed)
- [ ] Manual smoke on the emulator: a reminder set one minute out fires and Done clears it
