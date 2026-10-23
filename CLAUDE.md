# Accountability App

ADHD-focused Android reminder app. Product spec: `docs/PRD.md`.

## Agent skills

### Issue tracker

Local markdown under `.scratch/`; v1 tickets live in `.scratch/accountability-app/issues/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role vocabulary; `ready-for-human` marks tickets that need Charlie's direct action. See `docs/agents/triage-labels.md`.

## Coding style

- **Explicit `this.` in extension functions and properties.** Inside `fun Foo.bar()` or `val Foo.baz`, reference the receiver's members as `this.member`, not bare `member`. In Kotlin a bare call could equally be a top-level function, an imported extension, a local function, or a function-typed variable; `this.` shows at a glance that it's the receiver. Not required in ordinary class members or DSL/builder lambdas (Compose modifiers, Gradle blocks), where the bare form is the idiom.
