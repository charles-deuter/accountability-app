# 16: Settings — notification sound

**What to build:** A Settings destination in the bottom navigation where the user picks the reminder sound from the system's alarm sounds. The chosen sound plays on every subsequent reminder.

**Blocked by:** 15

**Status:** ready-for-agent

Design note: Android does not let an app change a notification channel's sound after the channel exists on the device. Changing the sound therefore means deleting the old channel and creating a new one under a new ID (and pointing future notifications at it). Any pending notification on the old channel must keep working until acted on.

- [ ] Settings appears in the bottom navigation
- [ ] The sound row opens the system ringtone picker filtered to alarm sounds and shows the current choice
- [ ] Choosing a sound results in the next reminder playing it
- [ ] Unit test covers the channel-ID versioning logic (new ID per change, old ID retired)
- [ ] The chosen sound persists across app restarts
