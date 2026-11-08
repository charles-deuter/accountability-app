# Driving the Pixel over adb

Charlie's Pixel 10a (serial `5A091JEA316773`) is the real device for checks the emulator can't stand in for. These were found the slow way during ticket 26.

## Installing

- Install builds with `adb install -r <apk>`, which keeps the app's data. `connectedDebugAndroidTest` uninstalls the app when it finishes and wipes Charlie's Tasks, so run it on the emulator only.
- Notifications can be off after an install: `adb shell pm grant dev.accountabilityapp android.permission.POST_NOTIFICATIONS`. Exact alarms: `adb shell appops set dev.accountabilityapp SCHEDULE_EXACT_ALARM allow`.
- Leave `--no-restart` off `am instrument`; on this phone it hangs. Instrumentation force-stops the app when it starts and when it finishes, which cancels its alarms, so launch the app afterwards (`adb shell am start -n dev.accountabilityapp/.MainActivity`) to reschedule them.
- `adb shell` drops quotes, so an `am instrument -e` value with a space fails with `Invalid userId`. Use values without spaces.

## Lock screen

- Unlocking needs Charlie. Ask, then poll until `adb shell dumpsys window | grep isKeyguardShowing` reads `false`. Use that, not `mScreenLocked` in `dumpsys deviceidle`, which disagreed with the screen during ticket 26.
- The phone locks again about 30 seconds after Charlie unlocks it. While a script runs, send a no-op key every few seconds to keep it awake: `adb shell input keyevent KEYCODE_UNKNOWN`.

## Reading state as text

- Ringing: `adb shell dumpsys audio | grep 'usage=USAGE_ALARM' | grep -c 'state:started'` is `1` while a Reminder's sound plays.
- A Reminder's channel: `adb shell dumpsys notification --noredact | grep -oE "effectiveNotificationChannel=NotificationChannel\{mId='reminders[a-z_]*'"`.
- Front screen: `adb shell dumpsys window | grep -m1 mCurrentFocus`. It reads `NotificationShade` on the lock screen.
- Next alarm: `adb shell dumpsys alarm | grep -A3 dev.accountabilityapp | grep origWhen`.
- Vibration: `adb shell dumpsys vibrator_manager | grep "usage: ALARM"` lists each buzz with its start time and length. A Reminder vibrates once, for about 1 second, when it starts.

## Heads-up notifications

`uiautomator dump` leaves heads-up notifications out, so act on them by coordinates. In portrait, the heads-up's Snooze action is at `adb shell input tap 768 422`, and `adb shell input swipe 540 300 540 10 150` swipes it away. A layout change moves these; take one screenshot to find the new position if a tap misses.
