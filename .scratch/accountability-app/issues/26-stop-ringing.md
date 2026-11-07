# 26: Stop a Reminder ringing when the user responds, or after 5 minutes

**What to build:** A Reminder's sound stops as soon as the user responds to it, and never rings forever. The Reminder itself still can't be dismissed: going silent leaves it in the shade with Done and Snooze.

**Blocked by:** none

**Status:** ready-for-agent

Found in QA on 2026-11-06. Swiping away the heads-up left the alarm playing with no end, and the sound carried on behind the Snooze picker.

Design notes:

- **Cause:** Android plays the channel's sound for as long as the notification exists, and the app has no code that stops it. The four alarm tones sampled on the Pixel 10a carry `ANDROID_LOOP` metadata, so they repeat. The four sampled on the emulator don't, which is why emulator smoke tests missed this. Reproduce on the phone.
- **Heads-up swipe:** as far as we know, Android gives the app no signal when a heads-up is swiped up, because the notification still exists. Confirm this on the phone. If so, the 5-minute limit is what ends the ringing in that case, and Charlie should hear about it, since this was the case Charlie reported.
- **Mechanism is open.** Pick the simplest one that meets the criteria:
  - Cancel the notification and post it again silently. The repost must leave out the full-screen intent, or the takeover reopens, and must keep the delete intent.
  - A silent channel with the app playing the sound itself, in a foreground service. This gives full control and would simplify ticket 16, but adds a foreground service type to declare to Play.
  - If the choice needs a foreground service, stop and ask Charlie first, because of that Play declaration.
- **Channel settings are permanent on a device:** changing the channel's sound means a new channel id.
- **The 5-minute limit** must survive the process dying. Doze may delay it, so "about 5 minutes" is fine while the phone is idle.

- [ ] Done stops the ringing
- [ ] Opening the Snooze picker stops the ringing before anything is picked, from both the notification and the takeover
- [ ] Swiping the Reminder out of the shade stops the ringing and still snoozes for 30 minutes
- [ ] Leaving the takeover (Home, Back, power button) stops the ringing, keeps the Reminder in the shade, and applies no Snooze
- [ ] With no response, the ringing stops after 5 minutes and the Reminder stays in the shade with Done and Snooze
- [ ] Vibration stops whenever the sound does
- [ ] A silenced Reminder doesn't ring or reopen the takeover on a later sync or app launch
- [ ] A Reminder returning from a Snooze rings normally
- [ ] Silencing one of two showing Reminders doesn't remove the other
- [ ] Tests cover each stop trigger and the silent repost or its equivalent, through the reminder seam (Robolectric)
- [ ] Charlie confirms on the Pixel: every trigger above, including what a heads-up swipe does
