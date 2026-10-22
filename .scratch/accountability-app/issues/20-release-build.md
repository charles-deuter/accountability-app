# 20: Release build — signing, versioning, AAB, icon

**What to build:** A command that produces a signed, minified Android App Bundle ready for upload to the Play Console, carrying the real app icon.

**Blocked by:** 17, 18

**Status:** ready-for-agent

- [ ] Release signing reads its keystore path and passwords from a gitignored local properties file; a committed example file lists the keys with placeholder values
- [ ] Without the properties file, the release build fails with a clear message (debug builds unaffected)
- [ ] R8 minification on for release, with keep rules so Room, receivers, and the takeover activity still work — verified by installing the release build on the emulator and firing one reminder
- [ ] versionCode/versionName scheme documented in the README
- [ ] Adaptive and monochrome icon from ticket 18 wired in
- [ ] Target SDK meets the current Play requirement
- [ ] README documents how to build the release bundle
