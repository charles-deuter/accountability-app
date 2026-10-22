# 19: [Charlie] Host the privacy policy on your domain

**What to build:** A public privacy-policy URL on the domain from ticket 02 (e.g. `https://<domain>/privacy`), which Play requires for any app that requests sensitive permissions. Accountability App stores everything on-device and sends nothing anywhere, so the policy is short. Claude drafts the text and can build the static page; Charlie connects the hosting account and DNS.

**Blocked by:** 02 (domain registered; the rest of ticket 02 need not be finished)

**Status:** ready-for-human

Hosting note: the `charles-deuter/accountability-app` repo is private, and GitHub Pages on a private repo needs a paid plan. Two free routes:

- **Cloudflare Pages** (pairs with a Cloudflare-registered domain): deploys from a private GitHub repo, so the page can live in this monorepo
- **GitHub Pages** from a small separate public repo, with the custom domain pointed at it

- [ ] Ask Claude to draft the policy (on-device storage only, no data collection or sharing, each permission explained) and build it as a single static page
- [ ] Review and edit the draft
- [ ] Choose a hosting route above, connect it, and point the domain (or a `/privacy` path) at it
- [ ] Policy reachable over HTTPS at a stable URL, recorded on this ticket for ticket 21
