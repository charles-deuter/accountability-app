# 21: [Charlie] Play Console setup and internal testing track

**What to build:** Nag available to friends through Play's testing tracks. Every step lives in Charlie's Play Console account. Claude can draft listing copy and declaration answers on request.

**Blocked by:** 02, 12, 19, 20

**Status:** ready-for-human

- [ ] Generate the upload keystore, store it plus its passwords somewhere safe outside the repo, and fill in the local signing properties file from ticket 20
- [ ] Create the app in the Play Console; enrol in Play App Signing
- [ ] Store listing: title, short and full description (ask Claude for a draft), icon, feature graphic, screenshots, privacy policy URL from ticket 19
- [ ] Data safety form (no data collected or shared)
- [ ] Permission declarations: full-screen intent (alarm-style use) and exact alarms
- [ ] Content rating questionnaire and target audience
- [ ] Upload the signed AAB to the internal testing track and invite friends by email
- [ ] If the account needs a closed test before production (see ticket 02), start it here and note the end date
