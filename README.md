# WorkoutTracker

WorkoutTracker is a modern Android fitness companion for recording workouts, tracking health metrics, monitoring weekly progress, and turning meal descriptions into editable nutrition estimates with AI.

The project is designed around a simple daily logging workflow: capture the data once, see meaningful trends over time, and use lightweight feedback to stay consistent.

## Why WorkoutTracker was created

Keeping track of your vitals, workouts, and nutrition (such as protein and calorie intake) is essential for studying your strength, growth, stamina, and overall fitness progress. 

Before this app, recording workouts often relied on a physical diary or a basic notes app. While those methods are okay for jotting things down, they offer **no way to compare and analyze your data**. Without comparison and analysis, it is impossible to see where you lack and where you can improve.

WorkoutTracker solves this problem by providing a simple, user-friendly UI packed with powerful features:
- **Comparison & Analysis**: Easily compare workout performance and health metrics over time to identify strengths and areas for improvement.
- **Vitals & Nutrition Tracking**: Monitor your weight, protein intake, and **water intake** daily.
- **Daily Notes**: Keep daily logs and reflections on your training and recovery.
- **AI-Powered Estimation**: Highlighted by **AI-driven calorie estimation**—describe any meal in natural language to instantly get nutritional estimates that you can review and edit before saving.

## Product overview

WorkoutTracker brings the core parts of a personal fitness journal into one focused mobile experience:

- Log workouts, exercises, sets, reps, duration, cardio speed, and incline.
- Track daily weight, protein, water, and other health metrics.
- Set weekly workout, cardio, weight, protein, water, and calorie goals.
- Review history by day and inspect weekly progress with charts and goal bars.
- Add meals manually or describe a meal in natural language for an AI nutrition estimate.
- Edit AI-generated meal items before saving them to the local food log.
- Generate concise, weekend-only AI summaries comparing current and previous weeks.
- Schedule fitness reminders and notifications for recurring logging habits.
- Use a responsive bottom navigation layout that remains usable on narrow phones.

## Highlights for reviewers

This repository demonstrates:

- Production-oriented Android UI built entirely with Jetpack Compose.
- Reusable design-system components for cards, buttons, progress bars, charts, navigation, and status chips.
- Offline-first local persistence for workout and health data using SQLite through a small database helper.
- A Kotlin ViewModel that coordinates UI state, persistence, date ranges, weekly calculations, and async work.
- A server-side AI integration that keeps the Gemini API key out of the Android client.
- Firebase callable functions with anonymous authentication, regional deployment, quotas, and Firestore-backed usage counters.
- Input validation, bounded AI output, error handling, loading states, and user confirmation before saving AI estimates.
- Responsive behavior tuned for compact Android devices as well as larger screens.

## Screens and flows

| Area | What it provides |
| --- | --- |
| Home | Daily overview, quick actions, goals, and key health stats |
| This Week | Weekly workout and metric progress at a glance |
| History | Browse logged days and reopen detailed entries |
| Trends | Weekly charts, comparisons, goals, and weekend AI summary |
| Food | Food log, nutrition totals, meal types, and saved estimates |
| Add workout | Create or edit exercises, sets, reps, duration, and cardio details |
| Add meal | Describe a meal, receive an estimate, edit the result, and save it |
| Notifications | Configure and review habit reminders |

## Tech stack

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Android Gradle Plugin with Kotlin DSL
- Android SDK 37; minimum SDK 33
- Jetpack Navigation Compose
- AndroidX ViewModel and lifecycle libraries
- Kotlin Coroutines and Play Services Tasks
- JUnit for local unit tests
- AndroidX Test and Espresso/Compose test dependencies for instrumented tests

### Data and backend

- SQLite for local workout, metric, goal, food, and AI-summary persistence
- Firebase Authentication with anonymous sign-in
- Firebase Cloud Functions for the AI boundary
- Firebase Firestore for server-side usage counters
- Google Gemini through the Generative Language API
- Node.js 22 Cloud Functions runtime
- Firebase Secret Manager integration for `GEMINI_API_KEY`

## Architecture

The app follows a lightweight layered structure:

```text
Compose screens
      │
      ▼
AppViewModel ───────► Fitness utilities and weekly calculations
      │
      ├─────────────► DatabaseHelper ───► SQLite
      │
      └─────────────► FoodAiClient ─────► Firebase callable functions
                                           │
                                           ▼
                                  Gemini + usage controls
```

Important source areas:

```text
app/src/main/java/com/example/workouttracker/
├── data/          Models, SQLite persistence, Firebase and AI clients
├── ui/            Compose screens and screen-specific UI
├── ui/components/ Reusable cards, buttons, charts, navigation, and controls
├── ui/theme/      Colors, typography, and Compose theme tokens
├── util/          Pure date, fitness, progress, and formatting logic
└── viewmodel/     Shared application state and async operations

backend/
├── functions/     Firebase callable functions and Gemini integration
├── firestore.rules
└── firebase.json
```

## AI meal and summary flow

The Android client does not call Gemini directly.

1. The user accepts the first-use meal disclosure.
2. The app signs in anonymously with Firebase Authentication.
3. `FoodAiClient` calls `parseFood` or `weeklySummary` in the `asia-south1` region.
4. The Cloud Function validates the request and applies per-user and global daily quotas.
5. The function reads `GEMINI_API_KEY` from Firebase Secret Manager and calls Gemini.
6. The response is constrained to a small JSON contract and normalized before returning.
7. Meal estimates remain editable and are saved only after the user confirms them.

Weekly summaries are intentionally available only on Saturdays and Sundays. They compare current-week values with the previous week and use the configured goals for context.

## Getting started

### Requirements

- Android Studio with an Android SDK installed
- JDK compatible with the project Gradle setup
- Node.js 22 or newer for backend work
- Firebase CLI for deploying functions
- A Firebase project with billing enabled for Cloud Functions/Gemini usage

### Configure Firebase locally

`app/google-services.json` is intentionally ignored and must be supplied locally:

1. Create or select a Firebase project.
2. Register the Android application ID `com.example.workouttracker`.
3. Download `google-services.json` into `app/`.
4. Enable Anonymous Authentication.
5. Configure Firestore in the `asia-south1` region.
6. Confirm the project ID in `backend/.firebaserc` matches the Android Firebase project.

Never commit `google-services.json`, Gemini keys, service-account files, or local SDK configuration.

### Configure and deploy the backend

```bash
cd backend/functions
npm install
cd ..

firebase login
firebase functions:secrets:set GEMINI_API_KEY
firebase deploy --only functions,firestore
```

The backend deploys:

- `parseFood` in `asia-south1`
- `weeklySummary` in `asia-south1`
- Firestore security rules

See [`backend/README.md`](backend/README.md) for deployment checks, emulator usage, quotas, and troubleshooting.

### Build the Android app

From the repository root:

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

The release APK is generated at:

```text
app/build/outputs/apk/release/app-release.apk
```

## Testing and quality checks

```bash
./gradlew test
./gradlew lint
./gradlew connectedAndroidTest
```

The unit tests focus on pure fitness and date logic, including weekly ranges, goal progress, and boundary cases. UI and navigation changes can be verified with instrumented tests on an emulator or device.

## Security and operational notes

- Gemini credentials stay in Firebase Secret Manager and are never embedded in the Android source.
- Client Firestore access is restricted; usage counters are maintained by the Admin SDK.
- AI request payloads and model output are bounded to control cost and response size.
- Per-user and global daily quotas protect the backend from accidental or abusive usage.
- The release APK uses the real Firebase Functions path; debug builds may use local emulator/mock behavior during development.
- Generated build output, local configuration, and Firebase client configuration are excluded from Git.

## Repository structure

```text
.
├── app/                  Android application module
├── backend/              Firebase Functions and Firestore configuration
├── docs/                 Design references and implementation notes
├── gradle/               Gradle wrapper and dependency configuration
├── build.gradle.kts      Root Gradle configuration
├── settings.gradle.kts   Gradle project settings
└── README.md             Project overview and setup guide
```

## Current scope and future improvements

Potential next steps include adding richer workout templates, sync across devices, deeper analytics, automated screenshot tests, Play Integrity App Check for Play-distributed builds, and a dedicated production signing configuration.

## License

No open-source license has been added yet. Treat this repository as a portfolio/project sample unless a license is provided by the owner.
