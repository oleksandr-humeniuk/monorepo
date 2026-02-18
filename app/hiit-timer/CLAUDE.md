# Android Hiit timer(tabata, high intensity intervals) App — Claude Context

## Project Overview

---

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM + Repository pattern
- **DI:** Hilt
- **Navigation:** Compose Navigation (single activity)
- **Networking:** Retrofit + OkHttp + Gson
- **Local DB:** Room
- **Async:** Kotlin Coroutines + Flow
- **Image loading:** Coil
- **Build:** Gradle with version catalog (`libs.versions.toml`)

---

## Architecture Rules

- **ViewModel** holds UI state and handles user events. Never import Android Context or View inside ViewModel.
- **Repository** abstracts data sources. ViewModel never calls Room or Retrofit directly.
- **Use cases** are optional but used when logic is reused across ViewModels or gets complex.
- **Domain models** are plain Kotlin data classes — no Room annotations, no Gson annotations there.
- **UI state** is a sealed class or data class held in `StateFlow` in the ViewModel.

### Typical data flow:
```
Composable → ViewModel → UseCase (optional) → Repository → Room / API
```

---

## Coding Conventions

### General
- Prefer `val` over `var` everywhere possible.
- Use `data class` for models, `sealed class` for UI state and results.
- Keep functions short and focused — if it does two things, split it.
- Name booleans clearly: `isLoading`, `hasError`, `isSynced`.

### Kotlin
- Use `when` over chains of `if/else`.
- Use extension functions for reusable logic tied to a type.
- Avoid nullable types unless truly optional — handle nullability at the data layer.
- Use `Result<T>` or a custom `Resource<T>` wrapper for API responses.

```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
```

### Compose
- Each screen has a stateless `@Composable` that receives state + callbacks.
- ViewModel is only accessed at the top-level screen composable, not deep in the tree.
- Break large composables into smaller named functions — no single composable over ~80 lines.
- Use `remember` and `derivedStateOf` wisely to avoid unnecessary recompositions.

### Room
- Entities live in `data/local/entity/` with `Entity` suffix (e.g. `CropEntity`).
- DAOs return `Flow<T>` for observable queries, `suspend fun` for writes.
- Use migrations for any schema change — never use `fallbackToDestructiveMigration` in production.

### Retrofit
- DTOs live in `data/remote/dto/` with `Dto` suffix (e.g. `CropDto`).
- Map DTOs to domain models inside the Repository, not in the ViewModel.
- All API calls are `suspend fun` inside a `try/catch` in the repository.

---

## Naming Conventions
| Type | Convention | Example |
|---|---|---|
| Screen composable | `[Feature]Screen` | `CropsScreen` |
| ViewModel | `[Feature]ViewModel` | `CropsViewModel` |
| Repository interface | `[Feature]Repository` | `CropRepository` |
| Repository impl | `[Feature]RepositoryImpl` | `CropRepositoryImpl` |
| Room entity | `[Feature]Entity` | `CropEntity` |
| API DTO | `[Feature]Dto` | `CropDto` |
| Use case | verb + noun + `UseCase` | `GetCropsUseCase` |
| Hilt module | `[Feature]Module` | `CropModule` |

---

## UI / UX Guidelines
- App is used outdoors — high contrast, large tap targets (min 48dp).
- Primary actions should be reachable with one thumb.
- Always show loading state, empty state, and error state for any list or data screen.
- Error messages should be human-readable — not raw exceptions.
- Use snackbars for transient feedback, dialogs only for destructive actions.

---

## Common Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Check for lint issues
./gradlew lint

# Clean build
./gradlew clean assembleDebug
```

---

## What to Avoid
- No business logic in Composables.
- No direct Room/Retrofit calls in ViewModel.
- No hardcoded strings in UI — use `strings.xml`.
- No hardcoded colors outside of `Theme.kt`.
- Don't suppress lint warnings without a comment explaining why.
- Don't use `GlobalScope` — use `viewModelScope` or `lifecycleScope`.
- Don't make the app crash silently — log errors and show user-friendly messages.

---

## Notes for Claude
- This is a startup project — pragmatic solutions over over-engineering.
- Prefer simple, readable code over clever abstractions.
- If something can be done in 30 lines cleanly, don't make it a framework.
- When suggesting code, match the conventions above and keep it consistent with the existing structure.
- Always handle loading/error/empty states when generating screen-level code.
