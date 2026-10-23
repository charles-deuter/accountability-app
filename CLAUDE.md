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
- **Lambdas read as functions:**
  - **Name every lambda parameter; never use the implicit `it`.** Write `tasks.map({ task -> task.title })`, not `tasks.map { it.title }`. Declare an unused parameter as `_` (`collect({ _ -> })`).
  - **Lambdas go inside the call's parentheses, so a call reads as a call** and not as a declaration or an `if`: `tasks.map({ task -> task.title })`, `remember({ -> mutableStateOf(false) })`, `viewModelScope.launch(block = { -> … })`.
    - Pass the lambda positionally when it is the call's only argument. Otherwise name it: `combine(flowA, flowB, transform = { a, b -> … })`. `launch`, `runTest`, `LaunchedEffect` and `viewModel` take other parameters first, so a positional lambda there won't compile.
    - A lambda with no parameters gets an explicit empty arrow: `{ -> … }`.
    - Android Studio's "Lambda argument should be moved out of parentheses" hint is expected; ignore it.
  - **Event callbacks use a named argument and an explicit empty arrow:** `onAddTask = { -> backStack.add(AddTask) }`, `onClick = { -> showDatePicker = true }`, and `{ -> }` for a no-op. Pass a function directly when it already exists (`onClick = onSave`).
  - **The one exception is UI content blocks and builders, which stay trailing with no arrow:** `Column { … }`, `Scaffold(…) { innerPadding -> … }`, `entry<TaskList> { … }`, `setContent { … }`, slots like `confirmButton = { … }`, and Gradle DSL blocks (`android { … }`).
  - When a callback grows past one line, give it a name with an explicit type: `val openAddTask: () -> Unit = { -> backStack.add(AddTask) }`.
