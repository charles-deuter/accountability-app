# Accountability App

An Android reminder app for people with ADHD. Its reminders can't be dismissed, only snoozed or completed, so a reminder always demands a decision.

## Language

### Things the user tracks

**Task**:
One thing the user has to do at a specific time. It is the item that appears in the Task List.
_Avoid_: Todo, item, event

**Task List**:
The home screen's single list of every Task that isn't Completed, ordered by Fire time.
_Avoid_: Inbox, agenda, dashboard

**Template**:
A recurring series that owns a Recurrence and keeps exactly one pending Instance alive at a time.
_Avoid_: Recurring task, routine, schedule

**Instance**:
A Task that a Template created. Completing it causes the Template to create the next Instance.
_Avoid_: Occurrence (when you mean the Task itself)

**Recurrence**:
The repeat pattern of a Template: once, daily, specific weekdays, weekly, monthly, quarterly, or yearly. Recurrence belongs to Templates, never to Tasks.
_Avoid_: Repeat rule, RRULE, frequency

### Time

**Fire time**:
The moment a Task's Reminder goes off. It is the Task's scheduled time, the only time the alarm follows. A Snooze moves the scheduled time; the time the user originally picked is kept apart and never fires.
_Avoid_: Due date, deadline, alarm time

**Overdue**:
A Task whose Fire time has passed and that isn't Completed.
_Avoid_: Late, missed, expired

### What happens when a Task comes due

**Reminder**:
The intrusive alert for a Task at its Fire time: a notification that can't be swiped away, plus the lock-screen takeover. A Reminder can't be dismissed; the only ways out are Snooze and Complete.
_Avoid_: Notification (the Android mechanism, not the concept), alarm, alert

**Snooze**:
Postponing a Task's Reminder by one of five preset durations: its scheduled time moves to that long from now. The Task stays visible in the Task List and its Snooze count goes up.
_Avoid_: Delay, postpone, remind me later

**Complete**:
Marking a Task as finished. The user taps **Done** to Complete a Task.
_Avoid_: Dismiss (no such action exists), close, check off

### Task status

**Pending**:
A Task that isn't Completed and has no active Snooze. A Pending Task can also be Overdue.

**Snoozed**:
A Task with an active Snooze. It stays Snoozed until it is Completed or the user picks a new time.

**Completed**:
A Task that has been Completed. It no longer appears in the Task List.
