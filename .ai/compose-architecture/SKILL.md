# Compose Architecture

## Rules

- Keep Composables focused on UI.
- Avoid heavy business logic inside Composables.
- Prefer state hoisting.
- Use ViewModel for screen state and events.
- Separate screen-level Composables from reusable UI components.

## Recommended pattern

For each screen:
- `ScreenRoute`: connects ViewModel to UI
- `Screen`: stateless or mostly stateless UI
- `UiState`: data class representing screen state
- `UiEvent`: user actions or one-time events when needed
