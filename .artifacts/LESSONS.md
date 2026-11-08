# Lessons

Corrections log. Each entry: what went wrong, and the rule that would have caught it.

## 2026-10-21: emulator ID taken from disk, not from the tool

**Wrong:** Recorded the emulator as `Medium_Phone` from the `~/.android/avd/Medium_Phone.avd` folder name. The ID the tools accept is `Medium_Phone_API_37.0`, which `android emulator list` prints.
**Rule:** Take identifiers that a tool consumes (AVD IDs, package names, device serials) from that tool's own list command, never from file or folder names.

## 2026-10-21: handoff told the next session to put build facts in CONTEXT.md

**Wrong:** The handoff said to record the JDK workaround and build/test commands in `CONTEXT.md`. The `domain-modeling` skill defines `CONTEXT.md` as a domain glossary with no implementation details.
**Rule:** `CONTEXT.md` holds domain terms only. Build, toolchain, and command facts go in the README or the resolved ticket's Comments.

## 2026-10-22: new tickets added to a feature PR

**Wrong:** Added tickets 23 and 24 (CI) to ticket 03's PR because that PR already carried ticket housekeeping. Charlie wanted them in a separate PR.
**Rule:** A feature PR only touches its own ticket (resolving it, plus follow-up items it directly creates). New or reshaped tickets go in their own PR off `main`, in their own commit.

## 2026-10-23: a partial answer read as a retraction

**Wrong:** I asked two questions (which lambdas go in parentheses, and whether to name the argument). Charlie's reply answered only the naming question, and I read it as dropping the parentheses rule altogether.
**Rule:** When a reply answers only some of several open questions, treat the others as still open. Never conclude a request was withdrawn unless the user says so.

## 2026-10-24: a stacked PR merged into its stale base

**Wrong:** PR #2 merged mid-session and I never retargeted PR #3 to `main`, so PR #3 merged into the dead `feat/03` branch and ticket 05 never reached `main`.
**Rule:** In a PR stack, check the base's state (`gh pr view <base>`) before each push and at the end of every turn. Once the base merges, retarget the next PR to `main` straight away. Before pushing to any PR's branch, make the state check gate the push (`[ "$(gh pr view N --json state -q .state)" = OPEN ] || exit 1`): on 2026-11-08 I printed `MERGED` and pushed anyway.

## 2026-11-06: Robolectric hid two real-device behaviours in ticket 26

**Wrong:** Silencing treated any `onStop` as the user leaving, so the 30-second screen timeout silenced an unattended locked phone after about 35 seconds. It also read `activeNotifications` right after `notify()`, which is asynchronous on a phone and synchronous in Robolectric. All JVM tests passed.
**Rule:** Green JVM tests say nothing about timing or system-driven lifecycle. For anything meant to run unattended, leave it unattended on a real device, and never read back system state just written through an asynchronous API.


## 2026-11-08: a flaky test reached main in ticket 26

**Wrong:** `SnoozeViewModelTest` asserted right after `advanceUntilIdle()`, but the ViewModel's work ran through Room's own executor, outside the test scheduler. It passed in my runs and failed 2 of 3 after the rebase. I also nearly took 8 Gradle "passes" as proof, though Gradle had skipped the unchanged runs.
**Rule:** When a test asserts on `viewModelScope` work that touches Room, give the database inline executors or wait on observable state. Count a new test as stable only after several forced reruns (`--rerun`).

## 2026-11-08: I measured screenshot cost by file size

**Wrong:** I ranked screenshots as the session's largest token cost from the characters of their saved image data, and wrote a "uiautomator dump first" rule on that basis. Image tokens are charged by pixel size, about 1,500 for a 700-pixel-wide screenshot, so 16 cost about 25k tokens. A full dump can cost more, and it hides ellipsized text.
**Rule:** Estimate an image's tokens from its pixel size, never its byte size. Check a cost claim before writing a rule on it.
