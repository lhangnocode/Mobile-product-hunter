# Android Skills

## General Android Guidelines

- Keep Android components simple.
- Avoid putting business logic directly inside Activity or UI components.
- Prefer ViewModel for screen logic.
- Prefer repository/use-case style when data logic grows.
- Keep permissions minimal.
- Handle lifecycle-aware operations carefully.

## Testing

When changing logic:
- add or update unit tests if possible
- run `./gradlew test`

When changing UI:
- check build and lint
- run `./gradlew lint` if available
