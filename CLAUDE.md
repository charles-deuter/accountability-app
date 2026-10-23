# Accountability App

ADHD-focused Android reminder app. Product spec: `docs/PRD.md`.

## Agent skills

### Issue tracker

Local markdown under `.scratch/`; v1 tickets live in `.scratch/accountability-app/issues/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role vocabulary; `ready-for-human` marks tickets that need Charlie's direct action. See `docs/agents/triage-labels.md`.

## Coding style

- **Plain functions, not extensions.** Write `fun isOverdue(task: Task, now: Instant)`, not `fun Task.isOverdue(now: Instant)`. An extension still has to be imported, so it adds nothing but dot syntax, and it hides where the function lives. If a library API forces an extension (for example a DSL receiver), reference receiver members with explicit `this.`.
- **Named intermediates over packed expressions.** When a line does more than one thing (a cast plus a property read, a compound condition), give each step a named `val` and use a block body.
- **Function names say what kind of function it is:**
  - **Selectors** (return a value derived from their argument, no side effects): `<noun>Of(subject)`, e.g. `fireTimeOf(task)`. Reads well as a reference too: `tasks.sortedBy(::fireTimeOf)`.
  - **Lookups through another object**: `<noun>From(source)`, e.g. `appContainerFrom(context)`.
  - **Predicates**: `is…` / `has…` / `can…`, e.g. `isOverdue(task, now)`.
  - **Actions and transformations**: a verb, e.g. `formatDateTime(instant)`, `complete(taskId)`.
  - Avoid `get…` (Java getter style), `select…` (Redux vocabulary), and `compute…` (implies expensive work) for selectors, and never name a function with a bare noun.
