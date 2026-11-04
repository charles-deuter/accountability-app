# 25: [Charlie] Review alarm settings and polish the alarm manager

**What to build:** Decisions on how Reminders use Android's alarms, made while they are still cheap to change. Only Charlie can do this: the answers trade on-time Reminders against Play review risk and battery use, which is a product call. Each decision gets recorded on this ticket, and any code change it needs becomes its own agent ticket.

**Blocked by:** 10

**Status:** ready-for-human

Decide this before tickets 11, 13 and 21 where possible, because the answers can change what they build and what the Play declarations say. Ticket 12's Doze measurements feed question 2 if they exist by then, but this review does not wait for them.

## How it works today

- **One alarm:** only the soonest Fire time across all open Tasks has an alarm. When it fires, the app posts a Reminder for every Task that is due and sets the alarm for the next Fire time.
- **Exact when allowed:** the alarm uses `setExactAndAllowWhileIdle()` with `RTC_WAKEUP`. Without the exact-alarm grant it falls back to `setAndAllowWhileIdle()`.
- **Permissions:** the app declares `SCHEDULE_EXACT_ALARM` (user-granted, revocable), not `USE_EXACT_ALARM`. Ticket 07 rejected `setAlarmClock()` for v1 because of its system alarm icon and "next alarm" side effects.

## Questions

### 1. Do recurring alarms on a specific day need to be exact?

Recurrence isn't built yet (tickets 13 and 14). A Template's Instance is a Task, so as designed it would share the one alarm with everything else.

Google's alarm guide says most apps can use inexact alarms, and that exact alarms are OK when core functionality depends on precise timing. Its inexact options:

- `setAndAllowWhileIdle()`: never fires early, and on Android 12+ fires within an hour of the trigger time unless battery-saving restrictions such as Doze apply.
- `setWindow()`: fires within a chosen window, but windows under 10 minutes are typically clipped to 10 minutes.

- [ ] Decide whether a "Tuesday at 9:00" Reminder can arrive late, and by how much
- [ ] Decide whether one-time Tasks, recurring Instances and Snoozes should get different precision, or all be the same
- [ ] If they differ, decide which rule picks the precision when the one alarm covers Tasks of both kinds

### 2. What happens when two alarms land at the same time?

Same Fire time: both Tasks share the one alarm, and each gets its own Reminder. A JVM test covers this. Still open:

- **Bundling (seen on the Android 17 emulator):** Android groups two or more Reminders, so each one's Done and Snooze sit behind an extra expand tap. A per-Reminder notification group did not stop it.
- **Takeover (untested):** each Reminder carries its own full-screen intent. Nothing yet shows which Task's takeover appears, or what happens to the other one once the first is answered.
- **Sound (untested):** whether two Reminders ring once or twice.

Close Fire times: while the phone is in Doze, exact alarms fire at most once per nine minutes per app. A Reminder 1–8 minutes after another can arrive up to about nine minutes late (ticket 07).

- [ ] On a device or emulator, watch two Tasks come due at the same minute, with the screen on and with it locked, and record what appears
- [ ] Decide what should happen: for example, takeovers one after another, or one takeover listing every due Task
- [ ] Decide whether the nine-minute Doze delay is acceptable, or worth `setAlarmClock()` for those cases

### 3. Which alarm APIs are easiest to get through Play review?

From Google's documentation, checked 2026-11-04:

| API or permission | Grant | Review position |
|---|---|---|
| Inexact alarms | No special permission | None of the exact-alarm rules apply |
| `SCHEDULE_EXACT_ALARM` | User grants it, and can revoke it; not pre-granted to fresh installs targeting Android 13+ | Google's suggestion for exact timing outside the `USE_EXACT_ALARM` categories |
| `USE_EXACT_ALARM` | Automatic, can't be revoked | Restricted: alarm or timer apps and calendar apps showing event notifications; needs a Console declaration, and apps that don't qualify may not publish |
| `setAlarmClock()` | Listed among the exact-alarm methods, which need one of the two permissions above | Google calls it the most critical kind of alarm; never delayed, highest battery cost |
| `USE_FULL_SCREEN_INTENT` | Automatic only when the core function is setting alarms or receiving calls; otherwise the user must grant it (Android 14+) | Needs a Console declaration when targeting Android 14+ |

- [ ] Decide whether the app presents itself to Play as an alarm app, a reminder app, or something else, since exact-alarm and full-screen-intent eligibility both depend on it
- [ ] Confirm or change ticket 11's choice of `SCHEDULE_EXACT_ALARM`
- [ ] Update ticket 21's declaration wording to match

Sources:

- [Schedule alarms](https://developer.android.com/develop/background-work/services/alarms)
- [Permissions and APIs that Access Sensitive Information](https://support.google.com/googleplay/android-developer/answer/9888170)
- [Exact alarm and full-screen intent declarations](https://support.google.com/googleplay/android-developer/answer/13161072)

## Polish

- [ ] Decide whether a new exact-alarm grant should take effect at once. Today nothing listens for `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`, so an alarm set while inexact stays inexact until the next sync. Google's guide says to reschedule on that broadcast
- [ ] Review the wording users see when exact alarms are off (ticket 11's banner), given that a fallback alarm can be up to an hour late or later under Doze

## Done when

- [ ] Each question above has a recorded decision and its reasoning in `## Comments`
- [ ] Each code change those decisions need has its own agent ticket, and tickets 11, 13, 14 and 21 are updated wherever a decision changes them
