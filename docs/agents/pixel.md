# Driving the Pixel over adb

Charlie's Pixel 10a (serial `5A091JEA316773`) is the real device for checks the emulator can't stand in for. This guide is for running those checks quickly and with as little of Charlie's time as possible. The app's data on the phone doesn't matter: wiping Tasks is fine. Most of these were found the slow way during ticket 26.

## Installing

- Start every check from a blank install: `adb uninstall dev.accountabilityapp`, then `adb install <apk>`. Charlie asked for this on 2026-11-10, so no check waits on leftover state.
- `connectedDebugAndroidTest` may run on the Pixel; it uninstalls the app when it finishes. Gradle runs it on every connected device, so with the emulator also attached, set `ANDROID_SERIAL` to pick one.
- A blank install starts with notifications, exact alarms and the battery exemption off, and opens onboarding on first launch. When a check doesn't cover onboarding, grant what it needs and tap Continue: `adb shell pm grant dev.accountabilityapp android.permission.POST_NOTIFICATIONS`, `adb shell appops set dev.accountabilityapp SCHEDULE_EXACT_ALARM allow`.
- Leave `--no-restart` off `am instrument`; on this phone it hangs. Instrumentation force-stops the app when it starts and when it finishes, which cancels its alarms, so launch the app afterwards (`adb shell am start -n dev.accountabilityapp/.MainActivity`) to reschedule them.
- `adb shell` drops quotes, so an `am instrument -e` value with a space fails with `Invalid userId`. Use values without spaces.

## Lock screen

- Unlocking needs Charlie. Ask, then poll until `adb shell dumpsys window | grep isKeyguardShowing` reads `false`. Use that, not `mScreenLocked` in `dumpsys deviceidle`, which disagreed with the screen during ticket 26.
- The phone locks again about 30 seconds after Charlie unlocks it. Don't keep it awake with injected keys: on this phone even `input keyevent KEYCODE_UNKNOWN` activates the focused control. During ticket 11 a loop of them finished onboarding, opened two of its steps and switched on a Settings toggle. Keep a check inside the 30 seconds, or ask Charlie to turn on Developer options › Stay awake, which keeps the screen on while the phone charges.

## Reading state as text

- Ringing: `adb shell dumpsys audio | grep 'usage=USAGE_ALARM' | grep -c 'state:started'` is `1` while a Reminder's sound plays.
- A Reminder's channel: `adb shell dumpsys notification --noredact | grep -oE "effectiveNotificationChannel=NotificationChannel\{mId='reminders[a-z_]*'"`.
- Front screen: `adb shell dumpsys window | grep -m1 mCurrentFocus`. It reads `NotificationShade` on the lock screen.
- Next alarm: `adb shell dumpsys alarm | grep -A3 dev.accountabilityapp | grep origWhen`.
- Vibration: `adb shell dumpsys vibrator_manager | grep "usage: ALARM"` lists each buzz with its start time and length. A Reminder vibrates once, for about 1 second, when it starts.
- Who did what: `adb logcat -d | grep -E "adbd service requested|ActivityTaskManager: START"` lists every adb shell command and every activity start with their times. Line an unexpected action up against it before putting it down to Charlie.

## Heads-up notifications

`uiautomator dump` leaves heads-up notifications out, so act on them by coordinates. In portrait, the heads-up's Snooze action is at `adb shell input tap 768 422`, and `adb shell input swipe 540 300 540 10 150` swipes it away. A layout change moves these; take one screenshot to find the new position if a tap misses.
