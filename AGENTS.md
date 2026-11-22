# Repository Guidelines

## Project Structure

This repository is an Android Jetpack Compose app. Application code lives in `app/src/main/java/com/example/workouttracker`, with screens under `ui`, reusable Compose components under `ui/components`, theme tokens under `ui/theme`, data models and persistence under `data`, and shared pure logic under `util`. Unit tests are in `app/src/test`; instrumented tests are in `app/src/androidTest`. Design references and implementation notes are in `docs/`.

## Build, Test, and Development Commands

Run commands from the repository root:

- `./gradlew assembleDebug` builds the debug APK.
- `./gradlew test` runs all local unit tests.
- `./gradlew connectedAndroidTest` runs instrumented tests on an available emulator or device.
- `./gradlew lint` runs Android lint checks.

Use Android Studio to launch the app on an emulator or device. Keep generated output under `app/build/` out of commits.

## Coding Style & Naming

Use Kotlin and four-space indentation. Follow standard Kotlin naming: `PascalCase` for composables, classes, and objects; `camelCase` for functions, properties, and parameters; and descriptive names instead of abbreviations. Keep composables focused and reuse components from `ui/components` before adding screen-specific variants. Match the existing dark-theme tokens and Compose Material 3 patterns. Format code with Android Studio’s Kotlin formatter before submitting.

## Testing Guidelines

Local tests use JUnit and should cover pure business logic in `util`, especially boundary cases and date/weight calculations. Name test classes with a `Test` suffix and test methods after the behavior they verify. Run the narrowest relevant test during development, then `./gradlew test` before opening a pull request. Add or update instrumented tests when changing navigation or device-dependent UI behavior.

## Commit & Pull Requests

Recent commits use short, imperative descriptions such as `Improve screen headers and text overflow handling`. Keep commits focused and describe the user-visible change. Pull requests should include a concise summary, testing commands and results, linked issue or task when available, and screenshots or a short recording for UI changes. Call out any known limitations or follow-up work.

## Configuration & Data

Do not commit secrets, local SDK paths, emulator state, or generated build artifacts. Preserve the existing data model and database behavior when making UI-only changes; inspect the relevant view model and `DatabaseHelper` before changing persistence.
