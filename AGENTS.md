# Mobile Product Hunter - Agent Instructions

## Project Overview

This is an Android project for Mobile Product Hunter.

Main stack:
- Kotlin
- Gradle Kotlin DSL
- Android app module: `app/`
- Project docs: `docs/`

## Repository Structure

- `app/`: Android application source code.
- `docs/`: project documentation.
- `gradle/`: Gradle wrapper/config files.
- `.ai/`: AI coding guidance and project rules.
- `build.gradle.kts`: root Gradle config.
- `settings.gradle.kts`: Gradle settings.

## General Rules

- Always read `.ai/context.md` before making changes.
- Prefer small, focused changes.
- Do not rewrite unrelated files.
- Do not change package names, Gradle versions, or app architecture unless explicitly asked.
- Before editing code, briefly explain the plan.
- After editing, summarize changed files and why.
- Run relevant Gradle checks when possible.

## Useful Commands

Build project:

```bash
./gradlew build
