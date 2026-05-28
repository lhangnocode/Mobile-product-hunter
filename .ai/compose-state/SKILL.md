# Compose State Management

## Rules

- Prefer immutable UI state.
- Keep one source of truth.
- Avoid passing too many unrelated parameters.
- Use state hoisting for reusable components.
- Avoid storing derived state unnecessarily.

## ViewModel

Prefer:

```kotlin
data class ScreenUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
