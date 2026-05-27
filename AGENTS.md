# StatsUp — Agent Instructions

## Project

Android native app (Kotlin + Jetpack Compose). Single module `:app`. Build system: Gradle KTS with version catalog at `gradle/libs.versions.toml`.

## Guardrail — Before declaring a task complete

Run these checks in order. If one fails, fix it and retry.

### 1. Translations complete
```
bash scripts/check_translations.sh
```
Compares all `<string name>` keys in `app/src/main/res/values/strings.xml` (English, default) against `values-it/`, `values-es/`, `values-fr/`. Fails if any key is missing or extra in any locale.

### 2. Compiles and builds
```
./gradlew assembleDebug
```
Must finish with `BUILD SUCCESSFUL`.

### 3. No lint errors (includes dead code / unused resources)
```
./gradlew lintDebug
```
Must finish with `BUILD SUCCESSFUL`. Lint is configured with `abortOnError = true`. Covers: Compose warnings, unused resources, deprecated attributes, common Android bugs.

### 4. Unit tests pass
```
./gradlew testDebugUnitTest
```
All tests must pass.

### 5. Outdated libraries (reminder — non-blocking)
```
./gradlew dependencyUpdates
```
Report outdated dependencies as a final note to the user. Does not block task completion, but always surface it.

## Key files

| Path | Purpose |
|------|---------|
| `app/src/main/res/values/strings.xml` | Default strings (English) |
| `app/src/main/res/values-it/strings.xml` | Italian |
| `app/src/main/res/values-es/strings.xml` | Spanish |
| `app/src/main/res/values-fr/strings.xml` | French |
| `gradle/libs.versions.toml` | All dependency versions |
| `app/lint.xml` | Lint rules (UnusedResources, etc.) |
| `app/build.gradle.kts` | Android build config |
| `.github/workflows/ci.yml` | CI pipeline |
| `scripts/check_translations.sh` | Translation completeness check |

## Conventions

- All strings must be added to **all 4 locale files** simultaneously.
- New dependencies go in `gradle/libs.versions.toml`, not hardcoded in `build.gradle.kts`.
- Kotlin code style: no unused imports, no unused parameters, no unreachable code.
