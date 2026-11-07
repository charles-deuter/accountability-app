# 29: Make the date and time pickers work in landscape

**What to build:** Every screen works when the phone is sideways. The app stays free to rotate rather than locking to portrait, because Android 16+ ignores that lock on large screens for apps targeting API 36.

**Blocked by:** none

**Status:** ready-for-agent

Found in QA on 2026-11-06 (LOOK-01 failed): the date and time forms break in landscape.

- [ ] The date picker dialog fits in landscape, with its OK and Cancel buttons reachable
- [ ] The time picker dialog fits in landscape, with its OK and Cancel buttons reachable
- [ ] A title typed into the Task Form survives rotation
- [ ] All five Snooze presets can be reached on the Snooze picker in landscape
- [ ] Done and Snooze can be reached on the takeover in landscape
- [ ] Screen-level tests cover what can be expressed at a landscape size; the rest gets one smoke pass on a device
