# 34: Recurring reminders across time-zone changes

**What to build:** When the phone's time zone changes, a recurring series keeps firing at its wall-clock time. Today (ticket 13) future Instances use the new zone, but the Instance already pending keeps its old instant: a 9:00 New York reminder fires at 6:00 after landing in Los Angeles.

**Blocked by:** 13

**Status:** needs-triage

Split out of ticket 13 while grilling (2026-11-10). The time-zone scope is large, so this ticket only collects it.

Open questions for triage:

- On `TIMEZONE_CHANGED` (the app already listens for it), should the pending Instance move to its time of day in the new zone? What if that time has already passed there?
- Does a Snoozed Instance move too, or keep its snoozed-until instant?
- Should one-off Tasks follow wall-clock time as well, or stay fixed instants?
- DST: ticket 14 tests the time of day across transitions; does anything else need to change?
