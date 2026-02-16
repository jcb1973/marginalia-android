# CLAUDE.md — Android Development (API 24+, Modern Android)

## Tech Stack

* **Environment:** Kotlin (stable), Android API 24+. Use coroutines + `suspend`. Prefer structured concurrency; avoid `GlobalScope`.
* **UI:** Jetpack Compose + Material 3.
* **Architecture:** MVVM with `ViewModel` + `StateFlow`.
* **Data:** Room for persistence; DataStore (Preferences) for simple flags.

---

## Architecture Rules

### Views (Composable Screens)

* Passive UI only.
* Collect state via `collectAsStateWithLifecycle()`.
* Use `LaunchedEffect` for lifecycle-triggered suspend work.
* No business logic inside composables.
* Extract composables if a function exceeds ~40 lines.

### ViewModels

* Extend `ViewModel`.
* Use `viewModelScope`.
* Expose immutable `StateFlow<UiState>`.
* Use `MutableStateFlow` internally only.
* No Android UI classes inside ViewModel.
* No direct navigation calls; emit events.

### Models

* Use `data class` for domain/data models.
* Room entities annotated with `@Entity`.
* No mutable public properties.
* No Android framework types in domain layer.

### Dependency Injection

* Prefer constructor injection.
* Use manual wiring initially.
* Avoid global singletons.
* Introduce Hilt only if complexity demands it.

---

## State Pattern

Use a single immutable UI state:

```kotlin
data class QuizUiState(
    val isLoading: Boolean = false,
    val question: Question? = null,
    val score: Int = 0,
    val error: UiError? = null
)
```

ViewModel exposes:

```kotlin
val uiState: StateFlow<QuizUiState>
```

No multiple unrelated flows for screen state.

---

## Code Standards

* Prefer `when` over nested `if`.
* No `!!` or unsafe casts.
* No `lateinit` unless strictly required.
* Avoid magic numbers or strings.
* Extract constants.
* Use sealed classes for events and errors.
* No `error()` or `throw RuntimeException()` in production logic.

---

## Coroutines

* All suspend work inside `viewModelScope.launch`.
* CPU-heavy work → `withContext(Dispatchers.Default)`.
* IO work → `Dispatchers.IO`.
* Never block threads.
* Never use `runBlocking` in production code.

---

## Navigation

Use:

* `androidx.navigation.compose.NavHost`

Rules:

* Pass IDs, not objects.
* Decode arguments in ViewModel.
* Keep navigation definitions centralized.

---

## Accessibility (Compose)

* Add `Modifier.testTag("addBookButton")` to all interactive elements.
* Use stable camelCase tags.
* Provide `contentDescription` for icon-only buttons.
* Use `semantics { heading() }` for section headers.
* Mark decorative images with `contentDescription = null`.
* Use Material typography; do not hardcode font sizes.
* Test with large font scale in emulator.

---

## Testing Guidelines

* Unit test all non-trivial logic.
* Test ViewModels independently from UI.
* Use coroutine test APIs (`runTest`).
* Inject dispatchers for determinism.
* Inject time and randomness.
* No real network, disk, or database I/O.
* Prefer fake repositories over mocks.

---

## Gradle Rules

* Use Kotlin DSL (`build.gradle.kts`).
* Keep dependencies minimal.
* No unused plugins.
* Keep module count low (single app module unless justified).

---

## Project Structure

```
data/
domain/
ui/
    home/
    quiz/
    results/
util/
```

Keep shallow.
Avoid premature modularisation.

---

## Error Handling

Use sealed error types:

```kotlin
sealed interface QuizError {
    data object Network : QuizError
    data object InvalidAnswer : QuizError
}
```

Map domain errors → UI errors explicitly.

---

## Performance Rules

* Avoid recomposition churn (remember state correctly).
* Hoist state.
* Use `derivedStateOf` for computed UI values.
* Do not allocate objects inside frequently recomposed blocks.

---

## Output Guidelines

* Provide complete, compilable Kotlin code.
* Use modern Compose patterns.
* Ensure logic is unit-testable.
* Avoid unnecessary explanation or alternative architectures.

