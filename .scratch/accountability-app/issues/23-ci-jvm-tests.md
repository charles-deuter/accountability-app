# 23: CI — validate the Gradle wrapper and run JVM tests on every PR

**What to build:** Every pull request and every push to `main` gets an automatic check on GitHub. The check fails if the committed Gradle wrapper jar has been tampered with, if the app doesn't compile, or if any JVM test fails. Reviewers can then see a red or green result on the PR instead of trusting a local run.

**Blocked by:** 03

**Status:** ready-for-agent

Design notes:

- Validate the wrapper first with Gradle's official wrapper-validation action, before any Gradle command runs. The committed jar was regenerated with Gradle 9.1.0 and matches the published checksum.
- Run from the `android/` project. Use Temurin JDK 21 (the build uses a Java 21 toolchain), Gradle's official setup action for caching, and an Android SDK with platform 36.
- Dependency locking is on. CI must never write locks, so a version drift fails the job. If the runner resolves something differently, fix the cause; don't loosen the locks.
- Emulator tests stay out of CI for now: they need an emulator on the runner, which is slow and flaky on hosted runners. Revisit when tickets 07–11 add device-only behaviour.
- Give the job a stable, descriptive name. Ticket 24 selects it by name as a required check, and renaming it later silently drops the requirement.

- [ ] A workflow runs on pull requests and on pushes to `main`
- [ ] Wrapper validation runs before any Gradle task and fails the job on a mismatch
- [ ] The job builds the debug app and runs the JVM test suite without writing locks
- [ ] On failure, the unit-test report is uploaded as a downloadable artifact
- [ ] The workflow's own PR shows the check passing on GitHub
- [ ] The README's Android section says what CI checks and names the job, for ticket 24
