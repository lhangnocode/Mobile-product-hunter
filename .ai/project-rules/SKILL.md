# Project Rules

## Before changing code

1. Understand the existing structure.
2. Identify the smallest set of files to change.
3. Explain the plan before editing.
4. Avoid unrelated refactors.

## Code style

- Prefer readable Kotlin over clever code.
- Keep functions small.
- Use meaningful names.
- Avoid duplicating logic.
- Avoid hardcoded strings in UI when possible.

## Safety

- Do not edit `local.properties`.
- Do not commit secrets or API keys.
- Do not change Gradle config unless necessary.
- Do not change app architecture without approval.
