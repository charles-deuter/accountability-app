# 19: [Charlie] Host the privacy policy on GitHub Pages

**What to build:** A public privacy-policy URL, which Play requires for any app that requests sensitive permissions. Accountability App stores everything on-device and sends nothing anywhere, so the policy is short. Claude drafts the text; Charlie publishes it, because it needs GitHub Pages enabled on Charlie's account. The `charles-deuter/accountability-app` repo is private, and Pages on a private repo needs a paid GitHub plan, so on the free plan host the policy from a small separate public repo.

**Blocked by:** None (can start immediately)

**Status:** ready-for-human

- [ ] Ask Claude to draft the policy (on-device storage only, no data collection or sharing, permissions explained)
- [ ] Review and edit the draft
- [ ] Enable GitHub Pages (on a separate public repo, unless the account has a paid plan that allows Pages on the private monorepo)
- [ ] Policy reachable at a stable public URL, recorded on this ticket for ticket 21
