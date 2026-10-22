# 14: Recurring reminders — monthly, quarterly, yearly

**What to build:** The remaining recurrence patterns. Monthly ("Pay rent on the 1st"), quarterly ("Change HVAC filter every 3 months"), and yearly ("Annual physical") use a date picker for the anchor day. Month-length edges behave predictably.

**Blocked by:** 13

**Status:** ready-for-agent

- [ ] Recurrence picker offers monthly, quarterly, and yearly with a day-of-month / date picker
- [ ] A series anchored on the 29th–31st fires on the last day of shorter months and returns to the anchor day in longer months (no drift)
- [ ] Yearly on Feb 29 fires on Feb 28 in non-leap years
- [ ] Quarterly advances three months from the series anchor
- [ ] Unit tests on the next-occurrence function cover every edge above plus DST transitions for the time of day
