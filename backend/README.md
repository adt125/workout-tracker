# WorkoutTracker AI Backend

This folder contains the Firebase Cloud Functions and Firestore rules used by the WorkoutTracker AI features.

The backend uses Google Gemini through the Generative Language API. The Gemini API key must remain a Firebase Functions secret and must never be placed in the Android app, `local.properties`, source code, or logs.

## Prerequisites

- Node.js 22 or newer
- Firebase CLI:

  ```bash
  npm install -g firebase-tools
  firebase login
  ```

- A Firebase project on the Blaze plan
- Gemini API access enabled in Google AI Studio / Google Cloud

## Create and configure the Firebase project

1. Create a Firebase project without Analytics.
2. Add the Android app with package name `com.example.workouttracker`.
3. Download `google-services.json` into the repository's `app/` directory.
4. Enable Authentication → Sign-in method → Anonymous.
5. Create Firestore in production mode in region `asia-south1`.
6. Set the Firebase project ID in `backend/.firebaserc`:

   ```json
   {
     "projects": {
       "default": "your-firebase-project-id"
     }
   }
   ```

Do not commit `google-services.json` if this repository is public. Review `.gitignore` before committing.

## Install backend dependencies

From the repository root:

```bash
cd backend/functions
npm install
cd ..
```

## Configure the Gemini secret

Set the key directly in Firebase. Do not write it into a file or pass it as a source-controlled environment variable:

```bash
firebase functions:secrets:set GEMINI_API_KEY
```

The command prompts for the value. Confirm that the key is not present in the repository:

```bash
rg -n --hidden --glob '!functions/node_modules/**' --glob '!../app/build/**' 'AIza|GEMINI_API_KEY\s*[:=]' .
```

The name must remain exactly `GEMINI_API_KEY`; it is referenced by `functions/index.js`.

## Run the emulators

From `backend/`:

```bash
firebase emulators:start
```

The functions emulator validates the JavaScript configuration and callable function loading. Android callable-function testing against the emulator requires additional emulator host configuration and is optional for the first deployment.

## Deploy

From `backend/`:

```bash
firebase deploy --only functions,firestore
```

Cloud Functions deploy with the Node.js 22 runtime, configured in `firebase.json` and `functions/package.json`.

This deploys:

- `parseFood` in `asia-south1`
- `weeklySummary` in `asia-south1`
- The deny-all Firestore security rules

Both functions use a maximum of three instances and a 40-second timeout. Usage counters are stored by the Admin SDK, so client Firestore access remains denied.

## Verify the deployment

1. In Firebase Console, confirm both functions are deployed in `asia-south1`.
2. Confirm the `GEMINI_API_KEY` secret is attached to both functions.
3. Launch the Android app with the matching `google-services.json`.
4. Tap Meal and accept the first-use disclosure.
5. Test a text meal such as `2 rotis, dal and a bowl of curd`.
6. Confirm the editable preview appears, then save it to Food log.
7. Test a photo after granting camera or gallery access.
8. Open Trends and generate a weekly summary.

The app signs in anonymously only immediately before the first AI request. AI results are not saved locally until the user confirms them.

## Operational controls

Before sharing the app:

- Set a Google Cloud budget alert of approximately INR 200.
- Set a monthly spend limit in the Gemini/API provider console where available.
- Keep the per-install limit at 20 calls per day and the global limit at 150 calls per day unless the owner approves a change.
- Do not enable App Check enforcement for APKs distributed directly. Enable Play Integrity App Check only when the app is distributed through Google Play.
- Consider configuring a Firestore TTL policy for the `expireAt` field on usage documents.

## Troubleshooting

- `unauthenticated / sign_in`: Anonymous Authentication is not enabled, or the request happened before sign-in completed.
- `resource-exhausted / user_cap`: The install has used its daily quota.
- `resource-exhausted / global_cap`: The global daily quota has been reached.
- `internal / ai_failed`: Check the Gemini API, model availability, billing, and secret configuration.
- `internal / ai_bad_json`: Gemini returned a response that did not match the required JSON contract.
- Deployment reports that Node.js 20 is decommissioned: confirm the Node.js 22 runtime is set in `firebase.json` and `functions/package.json`, then retry the deployment.
- Deployment fails with a project error: replace the placeholder project ID in `.firebaserc` and authenticate with the correct Firebase account.
