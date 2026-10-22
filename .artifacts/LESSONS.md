# Lessons

Corrections log. Each entry: what went wrong, and the rule that would have caught it.

## 2026-10-21: emulator ID taken from disk, not from the tool

**Wrong:** Recorded the emulator as `Medium_Phone` from the `~/.android/avd/Medium_Phone.avd` folder name. The ID the tools accept is `Medium_Phone_API_37.0`, which `android emulator list` prints.
**Rule:** Take identifiers that a tool consumes (AVD IDs, package names, device serials) from that tool's own list command, never from file or folder names.

## 2026-10-21: handoff told the next session to put build facts in CONTEXT.md

**Wrong:** The handoff said to record the JDK workaround and build/test commands in `CONTEXT.md`. The `domain-modeling` skill defines `CONTEXT.md` as a domain glossary with no implementation details.
**Rule:** `CONTEXT.md` holds domain terms only. Build, toolchain, and command facts go in the README or the resolved ticket's Comments.
