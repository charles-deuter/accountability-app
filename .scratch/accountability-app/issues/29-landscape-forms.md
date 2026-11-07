# 29: Make the date and time pickers work in landscape

**What to build:** Every screen works when the phone is sideways. The app stays free to rotate rather than locking to portrait, because Android 16+ ignores that lock on large screens for apps targeting API 36.

**Blocked by:** none

**Status:** resolved

Found in QA on 2026-11-06 (LOOK-01 failed): the date and time forms break in landscape.

- [x] The date picker dialog fits in landscape, with its OK and Cancel buttons reachable
- [x] The time picker dialog fits in landscape, with its OK and Cancel buttons reachable
- [x] A title typed into the Task Form survives rotation
- [x] All five Snooze presets can be reached on the Snooze picker in landscape
- [x] Done and Snooze can be reached on the takeover in landscape
- [x] Screen-level tests cover what can be expressed at a landscape size; the rest gets one smoke pass on a device

## Comments

**2026-11-06, agent:**
- **Short screens:** the two changes below apply when the screen is under 600dp tall, so a sideways tablet keeps the full calendar and dial.
- **Date picker:** drops its header and mode toggle, and the calendar scrolls. The dialog keeps OK and Cancel on screen. The year list still opens.
- **Time picker:** uses `TimeInput`, because the clock dial can't fit. The user types the time, with AM/PM buttons.
- **Snooze picker:** scrolls, so all five presets can be reached.
- **Task Form:** scrolls, so Save stays reachable above the keyboard.
- **Takeover:** ticket 28 keeps Done and Snooze on screen with any title.
- **Rotation:** the title already survived it, because it lives in the ViewModel. `RotationTest` now locks that in, and it passed before any change.
- `LandscapeTest` runs these at 800x360dp, the smallest common phone. They fail on the old screens.
- **Smoke, emulator API 37, landscape:** date picker, scrolling to the 30th and the year list; time picker.
- **Not smoked on a device:** the time picker with the on-screen keyboard open (the emulator showed only a keyboard toolbar), and the takeover and Snooze picker sideways, which only have `LandscapeTest` and `TitleDisplayTest`. Charlie can check these on the Pixel.
