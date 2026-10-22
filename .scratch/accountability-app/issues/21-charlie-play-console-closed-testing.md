# 21: [Charlie] Play Console setup and closed testing track

**What to build:** Accountability App available to friends through a Play closed test that satisfies the new-personal-account rule: 12+ testers opted in continuously for 14 days before production access (see ticket 02). Every step lives in Charlie's Play Console account. Claude can draft listing copy and declaration answers on request.

**Blocked by:** 02, 12, 19, 20

**Status:** ready-for-human

- [ ] Generate the upload keystore, store it plus its passwords somewhere safe outside the repo, and fill in the local signing properties file from ticket 20
- [ ] Create the app in the Play Console; enrol in Play App Signing
- [ ] Store listing: title, short and full description (ask Claude for a draft), icon, feature graphic, screenshots, privacy policy URL from ticket 19
- [ ] Data safety form (no data collected or shared)
- [ ] Permission declarations: full-screen intent (alarm-style use) and exact alarms (`SCHEDULE_EXACT_ALARM`: user-facing reminders that must fire at a precise time)
- [ ] Battery-optimization exemption justification: Play allows the request only when the app's core function is adversely affected; state that reminders failing to fire on time is the core-function failure (ask Claude for a draft)
- [ ] Content rating questionnaire and target audience
- [ ] Upload the signed AAB to a **closed testing** track and add the testers recruited in ticket 02 by Google account email
- [ ] Confirm at least 12 testers have actually opted in (the Console shows opted-in count); chase anyone who hasn't
- [ ] Record the date the 12th tester opted in and the earliest production-application date (14 days later) on this ticket
- [ ] Optional: an internal testing track for quick builds to Charlie's own devices (it does not count toward the 14 days)
