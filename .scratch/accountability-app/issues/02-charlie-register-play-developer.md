# 02: [Charlie] Register a Google Play developer account

**What to build:** A Google Play Console developer account that can publish Accountability App, with a public contact email on a domain Charlie owns rather than a personal address. Start this early: payment-method verification alone can take up to 5 days, and new personal accounts must run a 14-day closed test with 12+ testers before production access (tickets 21–22), so tester recruitment starts here too.

**Blocked by:** None (can start immediately)

**Status:** ready-for-human

Facts (Play Console Help, checked 2026-10-21):

- A personal account shows the **legal name, country, and developer email** publicly on Google Play. Full address shows only if the app is monetized (this one is free).
- Contact email, contact phone, and developer email are all verified by one-time code and must stay working for the life of the account. Contact email and phone stay private.
- The developer name is public and can be changed any time.
- Personal accounts created after 2023-11-13 need a closed test with **at least 12 testers opted in continuously for the 14 days** before applying for production. Internal testing does not count. (Older guides say 20; Google lowered it to 12 in December 2024.)
- Device verification needs a non-rooted physical Android phone on Android 10+ (the ticket 04 phone works).

Decision: **public email on an owned domain with forwarding** (not Gmail). Gmail `+` aliases expose the base address and dotted variants are the same address, so neither hides the personal email. Name the domain after Charlie or a studio, not the app: the app has already been renamed once, and one developer account can publish several apps.

### Domain and email

- [ ] Pick a domain name (studio or personal brand, not the app name) and record it on this ticket
- [ ] Register it. Cloudflare Registrar is the simplest single stop (at-cost pricing, and Email Routing is built in); any registrar plus a forwarding service such as ImprovMX also works
- [ ] Set up a forwarding address (e.g. `apps@<domain>` or `support@<domain>`) that delivers to the personal inbox, and confirm a test email arrives
- [ ] Skip "send as" (replying from the domain address) until there is user email to answer

### Play Console signup

- [ ] Decide which Google account owns the developer account (it can be the personal login; the public email is set separately) and turn on 2-step verification for it
- [ ] Sign up at `play.google.com/console/signup` and choose **Personal** (Organization requires a registered business and D-U-N-S number)
- [ ] Create or link the Google Payments profile (supplies the legal name and address)
- [ ] Enter developer name, developer email (`<address>@<domain>`), contact email, and contact phone; verify each one-time code
- [ ] Answer the background questions about Android experience and planned apps
- [ ] Pay the one-time $25 registration fee
- [ ] Complete identity verification (upload ID if asked) and wait for approval
- [ ] On the Console Home page, complete "Verify that you have access to an Android mobile device" with the Play Console app on the phone

### Testers for the closed test

- [ ] Recruit at least 12 testers, ideally 14–15 for slack: each needs a Google account and an Android phone, and must agree to stay opted in for 14 days straight once ticket 21 opens the closed test
- [ ] Record each tester's Google account email somewhere private (not in this repo) for ticket 21
