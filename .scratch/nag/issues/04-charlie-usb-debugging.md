# 04: [Charlie] Enable USB debugging and install the scaffold on your phone

**What to build:** Charlie's physical Android phone set up as a dev device. Full-screen intents, Do Not Disturb bypass, and OEM battery behaviour only show up reliably on real hardware, so ticket 12 depends on this.

**Blocked by:** 01, 03

**Status:** ready-for-human

- [ ] Developer options enabled on the phone (tap Build number seven times)
- [ ] USB debugging enabled; the Mac authorised on the phone's prompt
- [ ] `adb devices` lists the phone as `device` (not `unauthorized`)
- [ ] The scaffold build from ticket 03 installed and launched on the phone
- [ ] Phone model and Android version recorded on this ticket (OEM matters for battery behaviour)
