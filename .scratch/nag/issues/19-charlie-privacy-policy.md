# 19: [Charlie] Host the privacy policy on GitHub Pages

**What to build:** A public privacy-policy URL, which Play requires for any app that requests sensitive permissions. Nag stores everything on-device and sends nothing anywhere, so the policy is short. Claude drafts the text; Charlie publishes it, because it needs a public GitHub repo (this monorepo has no remote yet) and Pages enabled on Charlie's account.

**Blocked by:** None (can start immediately)

**Status:** ready-for-human

- [ ] Ask Claude to draft the policy (on-device storage only, no data collection or sharing, permissions explained)
- [ ] Review and edit the draft
- [ ] Create the GitHub repo (this monorepo, or a separate public repo for the site) and enable GitHub Pages
- [ ] Policy reachable at a stable public URL, recorded on this ticket for ticket 21
