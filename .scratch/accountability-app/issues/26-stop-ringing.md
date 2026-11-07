# 26: Stop a Reminder ringing when the user responds, or after 5 minutes

**What to build:** A Reminder's sound stops as soon as the user responds to it, and never rings forever. The Reminder itself still can't be dismissed: going silent leaves it in the shade with Done and Snooze.

**Blocked by:** none

**Status:** ready-for-human

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

- [x] Done stops the ringing
- [x] Opening the Snooze picker stops the ringing before anything is picked, from both the notification and the takeover
- [x] Swiping the Reminder out of the shade stops the ringing and still snoozes for 30 minutes
- [x] Leaving the takeover (Home, Back, power button) stops the ringing, keeps the Reminder in the shade, and applies no Snooze
- [x] With no response, the ringing stops after 5 minutes and the Reminder stays in the shade with Done and Snooze
- [x] Vibration stops whenever the sound does
- [x] A silenced Reminder doesn't ring or reopen the takeover on a later sync or app launch
- [x] A Reminder returning from a Snooze rings normally
- [x] Silencing one of two showing Reminders doesn't remove the other
- [x] Tests cover each stop trigger and the silent repost or its equivalent, through the reminder seam (Robolectric)
- [ ] Charlie confirms on the Pixel: every trigger above, including what a heads-up swipe does

## Comments

**2026-11-06, agent:** Code done. The last criterion is waiting on Charlie: a finger swipe on the heads-up, and vibration.
- **Mechanism:** a new silent channel, `reminders_silenced`, at default importance, so the Reminder stays with the alerting notifications. Silencing replaces the notification in place on that channel with no full-screen intent. No foreground service.
- **Not cancel-then-post, as the design notes first suggested.** Android applies these calls asynchronously, so a sync straight after could find the Reminder missing and post it again, ringing. Replacing in place has no such gap, and the Pixel confirms it stops the sound.
- **The 5-minute limit** needs no schema change and survives the process dying. The notification's `when` records when ringing began, and the app's one alarm is set for whichever comes first: the next Fire time or the soonest ring deadline.
- **The takeover keeps the screen on and closes at the limit.** The first version didn't, and the 30-second screen timeout counted as leaving: an unattended locked Pixel went quiet after about 35 seconds.
- **Triggers:** opening the Snooze picker silences from its ViewModel, and leaving the takeover from `onStop`, except rotation. Done and swiping out of the shade already removed the notification.
- **Proved on the Pixel 10a (Android 17),** reading the alarm player from `dumpsys audio`:
  - **Locked, on the takeover:** Back, Home and power stop the sound, and the Reminder stays on the silent channel. Done and Snooze stop the sound too; those two ran before the switch to replacing in place.
  - **Unlocked:** Snooze on the heads-up stops the sound before anything is picked, and Back from the picker then snoozes 15 minutes.
  - **Unlocked:** swiping out of the shade clears the Reminder and moves the alarm 30 minutes on.
  - **Unlocked, no takeover, no response:** the sound stopped between 4:56 and 5:27 after firing, with the alarm set for 5:00, and the Reminder stayed on the silent channel.
- **Heads-up swipe, one adb swipe up:** the heads-up went away and the sound stopped within 5 seconds, and stayed stopped at 30 seconds. The Reminder stayed on the ringing channel with the alarm unchanged, so Android stopped the sound with no signal to the app. This contradicts both the design note above and Charlie's QA report, so it needs a real finger before anyone relies on it.
- **Opening the shade also stops the sound.** Android does this too, with no signal to the app.
- **Not checked:** vibration, which adb can't read reliably here.
- **Behaviour to know:**
  - Tapping the notification body opens the app and doesn't silence, because it wasn't one of the agreed triggers.
  - A reboot clears notifications, so a silenced Reminder rings again afterwards.
  - A second Reminder's takeover closing the first's counts as leaving, so the first goes silent while the second rings.
