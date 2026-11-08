# Accountability App — ADHD Reminder App

## Overview

Accountability App is an Android reminder app designed for people with ADHD who reflexively dismiss notifications and procrastinate on recurring tasks. Its core differentiator: **reminders cannot be dismissed, only snoozed or completed.** Notifications fire as full-screen intents — taking over the lock screen like an incoming phone call — with sound and vibration that bypass Do Not Disturb.

Snoozed items remain visible in the task list with a badge showing how many times they've been snoozed, providing gentle accountability.

## Target Audience

- Primary: the developer (personal use, ADHD management)
- Secondary: friends and anyone with similar ADHD-driven procrastination patterns
- Distribution: Google Play Store

## Problem Statement

Existing reminder apps fail ADHD users in a specific way: notifications are too easy to dismiss reflexively during hyperfocus. The user swipes away the notification without conscious engagement, and the task is forgotten seconds later. The reminder fired, the user "saw" it, and nothing happened.

Accountability App solves this by removing the dismiss option entirely and making notifications intrusive enough to break through hyperfocus.

## Core Mechanics

### Snooze-Only Notifications

- Notifications are persistent (Android ongoing type) — they cannot be swiped away
- Two interaction options: **"Done"** (marks complete) and **"Snooze"** (opens duration picker)
- Full-screen intent takes over the lock screen with the same two options, no unlock required
- Notifications use the alarm channel, bypassing Do Not Disturb
- Sound and vibration always active — the user must hear it

### Snooze Behavior

- Tapping "Snooze" on the notification opens a picker with preset durations:
  - 15 minutes
  - 30 minutes
  - 1 hour
  - 2 hours
  - 1 day
- Snoozed tasks reappear in the task list with a badge: "Snoozed 3x — until 3:00 PM"
- No auto-escalation — the notification fires at the same intrusiveness level each time

### Overdue Behavior

- When a reminder fires and the user doesn't interact: the full-screen intent appears once, then the notification persists in the shade until acknowledged
- No automatic re-firing or escalation — the persistent notification is the ongoing nudge

### Recurring Tasks

- Completing a recurring task automatically schedules the next occurrence
- No confirmation prompt — next occurrence fires on schedule silently

## Recurrence Model

Tasks support the following recurrence patterns (enum-based, no recurrence engine):

| Pattern | Example |
|---------|---------|
| Once | "Call dentist" — fires once, done |
| Daily | "Take meds" — every day at 9am |
| Specific weekdays | "Gym" — Mon/Wed/Fri at 6am |
| Weekly | "Meal prep" — every Sunday at 10am |
| Monthly | "Pay rent" — 1st of every month |
| Quarterly | "Change HVAC filter" — every 3 months |
| Yearly | "Annual physical" — once a year |

## Screens

### 1. Task List (Home)

- Single chronological list of all upcoming and overdue reminders
- Overdue items highlighted visually (red/urgent styling)
- Snoozed items show badge with snooze count and next fire time
- Tap a task to edit it
- FAB to add a new task

### 2. Add/Edit Task

- **Title** — free text, required
- **Date/time picker** — when the reminder should fire
- **Recurrence picker** — once / daily / weekdays / weekly / monthly / quarterly / yearly
- For recurring: day-of-week selector or date picker depending on pattern
- Minimal input — no categories, priorities, or estimated duration

### 3. Template List

- List of recurring reminder templates
- Shows schedule summary for each (e.g., "Every Monday at 9am")
- Tap to edit, long-press or swipe to delete

### 4. Add/Edit Template

- Same fields as Add/Edit Task but focused on the recurrence pattern
- Template creates task instances on schedule

### 5. Snooze Picker

- Launched from notification "Snooze" action
- Shows 5 preset duration buttons (15 min, 30 min, 1 hour, 2 hours, 1 day)
- Single tap snoozes and returns to whatever the user was doing
- Dismissing without picking is not possible — back button defaults to shortest snooze (15 min)

### 6. Settings

- **Notification sound** — system sound picker (alarm sounds)
- Potential future: default snooze duration, quiet hours (not in v1)

## Architecture

### Platform & Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material Design 3
- **Storage**: Room database (SQLite), on-device only
- **Scheduling**: AlarmManager for exact alarms
- **Notifications**: Alarm-channel notifications with full-screen intents

### Data Model (Room)

**Task**
- id (primary key)
- title (string)
- scheduledTime (timestamp)
- recurrenceType (enum)
- recurrenceData (weekdays bitmask or day-of-month, depending on type)
- templateId (nullable FK — if created from a template)
- status (enum: pending, snoozed, completed)
- snoozeCount (int)
- originalScheduledTime (timestamp — the time the user picked; a Snooze moves scheduledTime and leaves this alone)
- completedAt (nullable timestamp)
- createdAt (timestamp)

**Template**
- id (primary key)
- title (string)
- recurrenceType (enum)
- recurrenceData (weekdays bitmask or day-of-month)
- scheduledTime (time-of-day only)
- isActive (boolean)
- createdAt (timestamp)

### Key Android Components

- **AlarmManager** — schedules exact alarms for each task's fire time
- **BroadcastReceiver** (alarm) — receives alarm, builds and posts notification
- **BroadcastReceiver** (boot) — re-schedules all pending alarms after device restart
- **Full-screen intent Activity** — lock screen takeover UI with Done/Snooze buttons
- **NotificationChannel** — alarm-importance channel created at app startup

### Permissions Required

- `USE_FULL_SCREEN_INTENT` — lock screen takeover (Android 14+ requires user grant)
- `SCHEDULE_EXACT_ALARM` — exact alarm scheduling (Android 12+ requires user grant)
- `RECEIVE_BOOT_COMPLETED` — re-schedule alarms after restart
- `POST_NOTIFICATIONS` — Android 13+ notification permission
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` — prevent OS from killing alarm delivery

## UI Design

- **Material Design 3** with a custom color scheme
- Clean, intentional feel — not default gray, but not over-designed
- Functional-first development: all screens built plain, animation polish pass at end
- Animations budgeted case-by-case during polish phase (completion animations are highest priority)

## Technical Risk Areas

1. **Full-screen intents on Android 14+** — requires special permission the user must grant in system settings. App needs to detect this and guide the user.
2. **Exact alarms on Android 12+** — `SCHEDULE_EXACT_ALARM` permission required. Without it, reminders drift by minutes.
3. **Battery optimization** — Android OEMs aggressively kill background processes. The app must guide users to exempt it, or alarms silently stop firing. This is the #1 support issue for alarm apps.
4. **Boot receiver** — all pending alarms must be re-scheduled after device restart. If this fails, the app silently stops working until opened.
5. **Notification channels** — must be created at first launch. Cannot be modified programmatically after creation on the user's device.

## Development Phases

| Phase | Scope | Estimated Days |
|-------|-------|---------------|
| 1. Setup | Android Studio, project scaffold, hello world on device | 2 |
| 2. Data Layer | Room schema, DAOs, repository, recurrence logic | 3 |
| 3. Core UI | All 6 screens, navigation, basic styling | 5 |
| 4. Notification Engine | Alarms, full-screen intents, snooze flow, boot receiver | 6 |
| 5. Polish | Animations, edge cases, device testing | 4 |
| 6. Play Store | Registration, assets, listing, submission | 5 |
| **Total** | | **~25 working days** |

**Effort estimate**: 60–70 hours part-time over 4–5 weeks. The notification engine (Phase 4) carries the most risk due to Android version fragmentation and device-specific alarm behavior.

## Verification Plan

- **Unit tests**: Recurrence next-occurrence calculation, snooze interval logic
- **Integration tests**: Room database CRUD, alarm scheduling/rescheduling
- **Device testing**: Full-screen intent on lock screen, notification sound/vibration, DND bypass, post-restart alarm delivery, battery optimization exemption
- **Release testing**: Internal testing track on Play Store (invite friends via email) before production release

## Deferred to v2

- **Stats/history** — completion rate, snooze counts, streaks per recurring task
- **Accountability** — notify a friend/accountability partner if a task isn't completed within a time window
- **Backend/sync** — sync tasks across devices via a lightweight API
- **AI task breakdown** — use an LLM to break vague tasks into concrete, timed steps
- **Starter packs** — curated bundles of recurring templates (homeowner, health, pet owner, etc.). User sees a checklist and picks which templates to activate, setting their own times. Data model designed so community-shared packs are possible later.
