# Error Handling

## Rules

- Do not ignore exceptions silently.
- Show user-friendly error messages.
- Keep technical errors in logs where appropriate.
- Distinguish loading, success, empty, and error states.
- Avoid crashing UI because of nullable data.

## UI state

Prefer representing errors in UI state:

```kotlin
data class UiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
