# 24: [Charlie] Require CI to pass before merging to main

**What to build:** GitHub refuses to merge a PR into `main` until ticket 23's check passes, so a red build can't land by accident. Only Charlie can do this: it's repository settings on Charlie's GitHub account, and it may involve a plan change.

**Blocked by:** 23 (the check must have run at least once before GitHub lets you pick it as required)

**Status:** ready-for-human

Plan constraint (checked 2026-10-22): the repo is private on a free personal plan. GitHub's API answers both branch protection and rulesets with "Upgrade to GitHub Pro or make this repository public to enable this feature." So decide first:

- **Upgrade to GitHub Pro:** keeps the repo private and enables required checks on it. Check current pricing on GitHub.
- **Make the repository public:** free, enables required checks. It also lets GitHub Pages host the privacy policy from this repo (ticket 19). The trade-off is that all code, tickets, and planning docs become public. Review the repo for anything private first.
- **Skip enforcement:** CI still reports red or green on every PR; Charlie just commits to not merging red PRs. No settings change.

### If enforcing (Pro or public)

- [ ] Decide between Pro, public, or skipping, and record the choice on this ticket
- [ ] Settings → Rules → Rulesets → **New branch ruleset**, targeting the default branch (`main`), enforcement **Active**
- [ ] Turn on **Require a pull request before merging**
- [ ] Turn on **Require status checks to pass**, and add ticket 23's job by the name the README gives
- [ ] Turn on **Block force pushes** and **Restrict deletions**
- [ ] Decide on the bypass list: leaving it empty means even Charlie can't merge a red PR; adding Charlie allows emergency merges, but the rule then becomes a reminder rather than a guarantee
- [ ] Prove it: open a throwaway PR with a deliberately failing test, confirm the Merge button is blocked, then close the PR without merging
