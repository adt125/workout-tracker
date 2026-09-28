# Fitness App --- Product Improvement & Phased Implementation Plan

## Purpose

This document is the execution plan for improving the existing
fitness/health tracking application.

The coding agent should implement the application **phase by phase**,
not all at once.

The developer will explicitly tell the agent which phase to execute, for
example:

> "Execute Phase 1 from FITNESS_APP_ROADMAP.md"

After each phase, the developer will test the application manually and
run the automated test suite. The agent **must not begin the next phase
unless explicitly instructed**.

------------------------------------------------------------------------

# 0. Product Vision

The application should evolve from a simple fitness data logger into a
personal fitness companion.

The core product loop is:

> **Log → Understand → Get Insight → Take Action → Track Progress**

The application should help a user answer:

1.  How am I doing today?
2.  Am I progressing toward my goals?
3.  What should I focus on today?
4.  How has my fitness changed over time?
5.  What patterns can I learn from my data?

The product should remain simple and fast for everyday logging.

Avoid adding features merely because they are technically interesting.
Every feature should either:

-   reduce logging friction,
-   improve understanding of progress,
-   help the user take action,
-   improve consistency, or
-   make the application meaningfully more useful.

------------------------------------------------------------------------

# 1. Important Instructions for the Coding Agent

## 1.1 Work incrementally

Implement only the requested phase.

Do not silently implement future-phase features.

If a future feature requires a database/API abstraction, create only the
minimal foundation needed for the current phase.

------------------------------------------------------------------------

## 1.2 Preserve existing functionality

Before changing anything:

-   inspect the current project structure,
-   identify the existing frontend/backend architecture,
-   understand current routes,
-   inspect database models/schema,
-   inspect existing API contracts,
-   inspect authentication if present,
-   inspect current tests,
-   understand how data is currently stored.

Do not rewrite working functionality without a clear reason.

Prefer incremental refactoring.

------------------------------------------------------------------------

## 1.3 Follow the existing tech stack

Do not replace the current framework or architecture unless explicitly
instructed.

For example, if the application currently uses:

-   React
-   Spring Boot
-   PostgreSQL
-   AWS
-   existing authentication

continue using them.

Reuse existing components, utilities, services, API conventions, and
styling primitives whenever possible.

------------------------------------------------------------------------

## 1.4 UX first

The application should feel like a polished consumer fitness
application, not an admin dashboard.

Prioritize:

-   clear information hierarchy,
-   large readable numbers,
-   meaningful visual feedback,
-   minimal typing,
-   consistent spacing,
-   consistent iconography,
-   clear empty states,
-   responsive layouts,
-   accessible controls,
-   obvious primary actions.

Avoid:

-   unnecessary cards,
-   excessive borders,
-   excessive gradients,
-   excessive animations,
-   excessive emoji,
-   dense dashboards,
-   modal overload.

------------------------------------------------------------------------

## 1.5 Data integrity

Fitness data is personal historical data.

Never silently overwrite historical records.

Prefer:

-   immutable logs,
-   timestamps,
-   edit/delete actions where appropriate,
-   validation,
-   explicit confirmation for destructive actions.

If a value is corrected, preserve a sensible audit/history model where
the existing architecture supports it.

------------------------------------------------------------------------

## 1.6 Backward compatibility

Existing data should continue to work after migrations.

Database migrations must:

-   be explicit,
-   be reversible where practical,
-   provide sensible defaults,
-   not destroy existing records.

------------------------------------------------------------------------

## 1.7 Testing is mandatory

Every phase must include appropriate tests.

At minimum:

### Frontend

-   component tests for important new components,
-   interaction tests for important flows,
-   validation tests,
-   empty/loading/error state tests.

### Backend

-   unit tests,
-   service-layer tests,
-   controller/API tests where appropriate,
-   validation tests,
-   repository/database tests where appropriate.

### End-to-end/manual testing

The agent must provide a manual test checklist after each phase.

Do not claim a phase is complete merely because the application
compiles.

------------------------------------------------------------------------

## 1.8 No fake functionality

Do not implement fake AI, fake analytics, fake charts, or placeholder
calculations that look real.

If a feature is not implemented yet:

-   clearly mark it as unavailable, or
-   leave it for the appropriate future phase.

Do not fabricate health insights.

------------------------------------------------------------------------

# 2. Product Information Architecture

The target structure is approximately:

``` text
HOME
├── Today's progress
├── Weight
├── Protein
├── Water
├── Today's workout
├── Quick Add
├── Today's timeline
└── Daily insight

PROGRESS
├── Weight
├── Protein
├── Water
├── Workout volume
├── Goals
└── Trends

WORKOUT
├── Start workout
├── Active workout
├── Workout history
├── Exercises
└── Personal records

HISTORY
├── Calendar
└── Daily details

REPORTS
├── Weekly report
├── Monthly report
└── AI review

SETTINGS
├── Profile
├── Goals
├── Preferences
├── Notifications
└── Data
```

The exact navigation can be adapted to the existing application.

Do not force five bottom-navigation tabs if the current application can
achieve the same usability with fewer.

------------------------------------------------------------------------

# 3. Design Direction

## 3.1 Visual hierarchy

Important values should be visually dominant.

Example:

``` text
72.4
kg

↓ 0.6 kg vs 4-week average
```

instead of:

``` text
Weight: 72.4 kg
```

------------------------------------------------------------------------

## 3.2 Cards

Not every piece of information should be a card.

Use:

-   large primary cards for important metrics,
-   smaller supporting surfaces,
-   flat sections for secondary information,
-   buttons for actions.

Avoid making the entire application look like a collection of identical
rounded rectangles.

------------------------------------------------------------------------

## 3.3 Icons

Use one consistent icon family.

Prefer an icon system already present in the project.

If no icon system exists, introduce one consistent library rather than
using arbitrary icons.

Emoji may be used sparingly for personality but should not become the
primary icon system.

------------------------------------------------------------------------

## 3.4 Colors

Maintain the application's existing dark visual identity unless there is
a strong reason to change it.

Use color semantically:

-   primary/accent,
-   success,
-   warning,
-   error,
-   neutral.

Do not use color as the only way to communicate information.

------------------------------------------------------------------------

## 3.5 Accessibility

All new components should consider:

-   keyboard navigation,
-   readable contrast,
-   focus states,
-   labels,
-   touch target size,
-   screen-reader-friendly controls,
-   form validation messages.

------------------------------------------------------------------------

# 4. PHASE 1 --- Core Product Experience

## Goal

Make the current application significantly better without introducing
advanced AI.

The application should become a useful daily fitness tracker.

## Scope

### 4.1 Goals and Targets

Allow the user to configure:

#### Body

-   current weight
-   target weight
-   fitness goal:
    -   build muscle
    -   lose fat
    -   maintain weight
    -   general fitness
-   optional target date

#### Nutrition

-   daily protein target
-   daily water target
-   optional calorie target

#### Workout

-   workouts per week
-   optional target workout duration

The existing application should use these targets throughout the UI.

------------------------------------------------------------------------

### 4.2 Onboarding / Goal Setup

Create an onboarding or setup flow if the application does not already
have one.

Example:

``` text
What's your goal?

○ Build muscle
○ Lose fat
○ Maintain weight
○ General fitness

↓

Current weight
72.4 kg

↓

Target weight
68 kg

↓

Daily protein target
120 g

↓

Daily water target
3 L

↓

Workout goal
4 days/week
```

The implementation should allow the user to edit these values later.

------------------------------------------------------------------------

### 4.3 Home Dashboard

The home screen should answer:

> "How am I doing today?"

Suggested structure:

``` text
TODAY
Saturday, 26 September

72.4 kg
↓ 0.3 kg this week

Protein
58 / 120 g
██████░░░░░░

Water
1.8 / 3.0 L
████████░░░░

Today's Workout
Chest + Triceps
4 exercises · 52 min

QUICK LOG

+250 ml   +500 ml   +1 L
Water

+20 g     +25 g     +30 g
Protein

+ Weight  + Workout
```

Do not copy these exact numbers; use real user data.

------------------------------------------------------------------------

### 4.4 Quick Add

Make common logging extremely fast.

#### Water

Provide common increments:

-   +250 ml
-   +500 ml
-   +750 ml
-   +1 L
-   custom

#### Protein

Provide:

-   +20 g
-   +25 g
-   +30 g
-   custom

#### Weight

Open a minimal weight-entry interaction.

#### Workout

Open workout flow.

The user should not need to navigate through several screens for common
actions.

------------------------------------------------------------------------

### 4.5 Progress Indicators

Protein and water should display:

-   current amount,
-   target,
-   percentage,
-   progress bar/ring,
-   remaining amount.

Example:

``` text
Protein

72 / 120 g

████████░░░░
60%

48 g remaining
```

Avoid misleading progress if a target is not configured.

------------------------------------------------------------------------

### 4.6 Weight Trends

Introduce a weight trend visualization.

Time ranges:

-   7D
-   30D
-   90D
-   1Y

Show:

-   current weight,
-   change over selected period,
-   trend line,
-   optional moving average.

Example:

``` text
72.4 kg

30-day change
↓ 1.2 kg
```

The graph must handle:

-   no data,
-   one data point,
-   sparse data,
-   multiple data points,
-   future timestamps,
-   duplicate entries.

------------------------------------------------------------------------

### 4.7 History Improvements

The calendar should show useful indicators.

Example:

``` text
       M  T  W  T  F  S  S

       1  2  3  4  5  6  7
          ·     🏋    💧

       21 22 23 24 25 26 27
       🏋    🥩   🏋   💧
```

Do not depend on emoji specifically; use the application's icon system.

Tapping a date should open a daily summary:

``` text
September 26

Weight
72.4 kg

Water
2.3 L

Protein
104 g

Workout
Chest + Triceps
57 min

Notes
...
```

------------------------------------------------------------------------

### 4.8 Daily Timeline

Add a chronological activity feed where useful.

Example:

``` text
7:45 AM
Weight — 72.4 kg

9:50 AM
Workout — Chest + Triceps

10:30 AM
Protein — 30 g

12:40 PM
Water — 500 ml

2:00 PM
Lunch logged
```

This should use real events from the application.

------------------------------------------------------------------------

### 4.9 Phase 1 Data Model

Adapt to the existing schema, but likely entities include:

``` text
User
Goal / UserGoal
WeightLog
WaterLog
ProteinLog
Workout
WorkoutExercise
DailyNote
```

Avoid creating duplicate concepts if the existing application already
has them.

Prefer normalized storage for historical logs.

------------------------------------------------------------------------

## Phase 1 Acceptance Criteria

The phase is complete only when:

-   [ ] User can configure fitness goals.
-   [ ] Goals persist after refresh/restart.
-   [ ] Home dashboard reflects configured goals.
-   [ ] User can quickly log water.
-   [ ] User can quickly log protein.
-   [ ] User can log weight.
-   [ ] Progress indicators use real data.
-   [ ] Weight trend works across multiple time ranges.
-   [ ] History calendar shows logged activity.
-   [ ] Tapping a date shows the correct daily data.
-   [ ] Today's timeline uses actual logged events.
-   [ ] Existing functionality still works.
-   [ ] Automated tests pass.
-   [ ] No console errors.
-   [ ] No backend errors.
-   [ ] Responsive layout works on mobile-sized screens and desktop.

## Phase 1 Manual QA

Test at least:

1.  New user with no data.
2.  User with only weight data.
3.  User with only water data.
4.  User with only protein data.
5.  User with all data.
6.  User with no goals configured.
7.  User editing goals.
8.  User logging multiple water entries.
9.  User logging multiple protein entries.
10. User correcting/deleting a log if supported.
11. User navigating between dates.
12. User changing chart time ranges.
13. Refreshing the application.
14. Restarting backend/frontend.
15. Mobile viewport.
16. Desktop viewport.

STOP HERE.

Do not implement Phase 2 until explicitly instructed.

------------------------------------------------------------------------

# 5. PHASE 2 --- Workout Tracking & Progression

## Goal

Turn workout logging from a basic activity log into a real
strength-training tracker.

------------------------------------------------------------------------

## 5.1 Start Workout

Create a dedicated workout flow.

Example:

``` text
Start Workout

Chest + Triceps

[ Add Exercise ]

Bench Press
Incline DB Press
Cable Fly
Triceps Pushdown

[ Start ]
```

Allow users to:

-   select workout type,
-   select exercises,
-   add custom exercises,
-   reorder exercises,
-   remove exercises.

------------------------------------------------------------------------

## 5.2 Sets and Reps

Each strength exercise should support:

``` text
Exercise: Bench Press

Set   Weight   Reps

1     60 kg    10
2     60 kg     9
3     55 kg    10

+ Add Set
```

Support where appropriate:

-   weight,
-   reps,
-   duration,
-   distance,
-   rest time,
-   RPE/effort as an optional future-ready field.

Do not force strength-specific fields onto cardio exercises.

------------------------------------------------------------------------

## 5.3 Previous Performance

When performing an exercise, show the previous session.

Example:

``` text
Last workout

22.5 kg × 8

Suggestion:
Try 22.5 kg × 9
```

The system should not pretend that a recommendation is medically
authoritative.

Phrase it as a training suggestion.

------------------------------------------------------------------------

## 5.4 Personal Records

Automatically detect:

-   heaviest weight,
-   most reps at a given weight,
-   estimated 1RM where appropriate,
-   highest total volume,
-   longest duration where relevant.

Show:

``` text
🎉 New PR

Incline DB Press
22.5 kg × 10

Previous best
22.5 kg × 8
```

------------------------------------------------------------------------

## 5.5 Workout Summary

After completion:

``` text
Workout Complete

Chest + Triceps

57 min

8 exercises
24 sets

Total volume
4,820 kg

New PRs
2

[ View Workout ]
```

------------------------------------------------------------------------

## 5.6 Workout History

Allow:

-   date filtering,
-   workout filtering,
-   exercise filtering.

Exercise detail:

``` text
Bench Press

30D

Weight
60 → 65 kg

Volume
↑ 18%

Best
65 × 8
```

------------------------------------------------------------------------

## Phase 2 Acceptance Criteria

-   [ ] User can start a workout.
-   [ ] User can add exercises.
-   [ ] User can add sets.
-   [ ] Weight/reps persist correctly.
-   [ ] Previous performance is displayed.
-   [ ] Workout can be completed.
-   [ ] Workout summary is generated from real data.
-   [ ] PR detection works.
-   [ ] Workout history works.
-   [ ] Existing logs remain intact.
-   [ ] Automated tests pass.
-   [ ] Manual QA passes.

STOP HERE.

Do not implement Phase 3 until explicitly instructed.

------------------------------------------------------------------------

# 6. PHASE 3 --- Analytics & Insights

## Goal

Make the application explain the user's data.

This phase should initially be **deterministic analytics**, not an LLM.

------------------------------------------------------------------------

## 6.1 Progress Dashboard

Add:

### Weight

-   current value
-   7D trend
-   30D trend
-   90D trend
-   1Y trend

### Protein

-   daily average
-   target achievement rate
-   weekly average
-   best day
-   lowest day

### Water

-   daily average
-   target achievement rate
-   weekly average

### Workout

-   workouts/week
-   average duration
-   total volume
-   training frequency
-   PR count

------------------------------------------------------------------------

## 6.2 Weekly Report

Example:

``` text
THIS WEEK

4 workouts
2.3 L avg water
91 g avg protein
-0.3 kg weight change

Protein target
4 / 7 days achieved

Workout consistency
4 / 4 planned
```

------------------------------------------------------------------------

## 6.3 Insights

Generate deterministic observations.

Examples:

``` text
Your average protein intake increased by 12% compared with last week.

You completed 4 workouts this week, matching your target.

Your weight stayed within a 0.7 kg range over the last 14 days.

You hit your water target on 5 of the last 7 days.
```

Rules should be transparent and testable.

Avoid health diagnoses or medical claims.

------------------------------------------------------------------------

## 6.4 Monthly Report

Include:

-   weight trend,
-   protein consistency,
-   water consistency,
-   workout frequency,
-   total workouts,
-   PRs,
-   strongest exercise improvements,
-   logging consistency.

------------------------------------------------------------------------

## 6.5 Charts

Charts should be:

-   readable,
-   responsive,
-   accessible,
-   interactive where useful,
-   consistent with the design system.

Handle:

-   empty state,
-   insufficient data,
-   long date ranges,
-   extreme values,
-   missing days.

------------------------------------------------------------------------

## Phase 3 Acceptance Criteria

-   [ ] Progress dashboard exists.
-   [ ] Weekly report works.
-   [ ] Monthly report works.
-   [ ] Insights are generated from actual data.
-   [ ] No fabricated metrics.
-   [ ] Charts handle empty/sparse data.
-   [ ] Calculations have automated tests.
-   [ ] Report calculations match raw logs.
-   [ ] Performance remains acceptable with larger datasets.

STOP HERE.

Do not implement Phase 4 until explicitly instructed.

------------------------------------------------------------------------

# 7. PHASE 4 --- AI Fitness Companion

## Goal

Introduce AI only after reliable structured data and deterministic
analytics exist.

AI should explain the user's existing data rather than inventing facts.

------------------------------------------------------------------------

# 7.1 Daily AI Summary

Example:

``` text
TODAY'S AI SUMMARY

You're making steady progress today.

Protein
62 / 120 g

Water
1.8 / 3 L

Workout
Completed

Your main gap today is protein.

You have approximately 58 g remaining.
```

The AI should receive structured application data rather than scraping
UI text.

------------------------------------------------------------------------

# 7.2 Weekly AI Review

Example:

``` text
YOUR WEEK

You trained 4 times this week.

Your average protein intake was 91 g,
up from 82 g last week.

Your weight decreased by 0.3 kg.

FOCUS NEXT WEEK

Try reaching your protein target on
at least 5 days.
```

The application should provide the factual metrics.

The model should primarily generate the narrative.

------------------------------------------------------------------------

# 7.3 AI Action Suggestions

Examples:

-   next meal protein suggestion,
-   hydration reminder,
-   workout consistency suggestion,
-   recovery/logging reminder.

Do not make medical recommendations.

Avoid:

-   diagnosing conditions,
-   medication recommendations,
-   disease claims,
-   unsafe weight-loss advice.

------------------------------------------------------------------------

# 7.4 AI Chat

Only introduce a general chat experience if there is a clear product
reason.

Potential questions:

> "How has my weight changed this month?"

> "Which exercises improved the most?"

> "Why did my protein average fall this week?"

> "Summarize my last 30 days."

The AI should have access to structured user metrics through safe
backend tools/functions.

Do not expose raw database credentials or unrestricted database access
to the model.

------------------------------------------------------------------------

# 7.5 AI Architecture

Prefer:

``` text
React
  ↓
Spring Boot
  ↓
Fitness Analytics Service
  ↓
Structured User Metrics
  ↓
AI Service
  ↓
LLM
```

Rather than:

``` text
React → LLM → Database
```

The backend should control what information the model can access.

------------------------------------------------------------------------

## Phase 4 Acceptance Criteria

-   [ ] Daily summary uses actual data.
-   [ ] Weekly review uses actual analytics.
-   [ ] AI cannot invent missing metrics.
-   [ ] AI responses respect user privacy.
-   [ ] AI errors have graceful fallbacks.
-   [ ] API keys remain server-side.
-   [ ] Prompt injection risks are considered.
-   [ ] AI calls have timeout/error handling.
-   [ ] Usage/cost can be monitored.
-   [ ] Automated tests cover important AI-service behavior.

STOP HERE.

Do not implement Phase 5 until explicitly instructed.

------------------------------------------------------------------------

# 8. PHASE 5 --- Smart Reminders & Consistency

## Goal

Help the user act on their goals without becoming annoying.

------------------------------------------------------------------------

## 8.1 Reminders

Potential reminders:

### Water

> You're at 1.4L today. Your target is 3L.

### Protein

> You have approximately 40g protein remaining today.

### Workout

> You haven't logged today's workout yet.

Reminders should be configurable.

------------------------------------------------------------------------

## 8.2 Smart Timing

Do not send reminders at arbitrary times.

Use:

-   user's configured schedule,
-   historical logging patterns,
-   quiet hours,
-   workout schedule.

------------------------------------------------------------------------

## 8.3 Streaks

Track:

-   logging streak,
-   workout consistency,
-   protein target consistency,
-   water target consistency.

Avoid punitive language.

Prefer:

``` text
5-day consistency streak
```

rather than:

``` text
You failed your streak!
```

------------------------------------------------------------------------

## Phase 5 Acceptance Criteria

-   [ ] Notifications can be configured.
-   [ ] Quiet hours work.
-   [ ] User can disable categories.
-   [ ] Reminders are based on actual data.
-   [ ] No duplicate notification spam.
-   [ ] Streaks calculate correctly.
-   [ ] Notification scheduling is reliable.
-   [ ] Tests cover scheduling/business logic.

STOP HERE.

Do not implement Phase 6 until explicitly instructed.

------------------------------------------------------------------------

# 9. PHASE 6 --- Nutrition Intelligence

## Goal

Make nutrition tracking faster and more useful.

------------------------------------------------------------------------

## 9.1 Food Database

Allow:

``` text
Add Food

Search:
Paneer

100g
Protein: 18g
Calories: ...
```

Allow users to create custom foods.

------------------------------------------------------------------------

## 9.2 Meal Logging

Example:

``` text
Breakfast

Banana
Whey
Oats

Protein
32g
```

------------------------------------------------------------------------

## 9.3 AI Meal Logging

Potential future flow:

``` text
Take / upload meal photo

↓

AI estimates likely foods

↓

User reviews

↓

User confirms

↓

Meal gets logged
```

The AI estimate must clearly be presented as an estimate.

Never imply image-based nutrition estimates are exact.

------------------------------------------------------------------------

## 9.4 Nutrition Dashboard

Track:

-   protein,
-   calories if enabled,
-   meals,
-   daily averages,
-   target adherence.

Do not force calorie tracking on users who only want
protein/water/workout tracking.

------------------------------------------------------------------------

## Phase 6 Acceptance Criteria

-   [ ] Food search works.
-   [ ] Food entries can be logged.
-   [ ] Custom foods work.
-   [ ] Daily nutrition totals are correct.
-   [ ] Existing protein logs remain compatible.
-   [ ] AI estimates are clearly labeled.
-   [ ] User confirms AI-generated food entries before saving.

STOP HERE.

Do not implement Phase 7 until explicitly instructed.

------------------------------------------------------------------------

# 10. PHASE 7 --- Advanced Fitness Features

## Goal

Add features that make the application competitive with serious fitness
trackers.

Potential features:

### Exercise Library

For each exercise:

-   name,
-   muscle group,
-   equipment,
-   instructions,
-   alternatives,
-   personal history.

### Progressive Overload

Suggest:

``` text
Last time
60 kg × 8

Today
Try 60 kg × 9
```

Only when enough historical data exists.

### Workout Templates

Examples:

-   Push
-   Pull
-   Legs
-   Upper
-   Lower
-   Full Body

Users can customize templates.

### Personal Records

Dedicated PR dashboard.

### Exercise Analytics

Track:

-   strength trend,
-   volume,
-   frequency,
-   estimated 1RM.

### Workout Calendar

Visualize:

``` text
Mon  Tue  Wed  Thu  Fri  Sat  Sun

 🏋       🏋       🏋       🏋
```

------------------------------------------------------------------------

# 11. PHASE 8 --- Integrations

Only after the core product is stable.

Potential integrations:

-   Apple Health / Health Connect where technically appropriate,
-   wearable devices,
-   smartwatch workout data,
-   notification platforms,
-   calendar integration.

The integration layer should normalize external data rather than
allowing vendor-specific logic to spread throughout the application.

Example:

``` text
External Provider
       ↓
Integration Adapter
       ↓
Normalized Fitness Event
       ↓
Application
```

------------------------------------------------------------------------

# 12. PHASE 9 --- Production Hardening

Before considering the application production-ready:

## Security

-   authentication,
-   authorization,
-   secure secrets,
-   API validation,
-   rate limiting,
-   secure AI endpoints,
-   safe file uploads if introduced,
-   privacy controls.

## Performance

Test:

-   large history,
-   many workouts,
-   many exercises,
-   analytics queries,
-   concurrent requests.

## Observability

Add:

-   structured logs,
-   request tracing where appropriate,
-   error monitoring,
-   application metrics,
-   AI latency/cost monitoring.

## Infrastructure

Potential stack:

``` text
React
   ↓
API Gateway / Load Balancer
   ↓
Spring Boot
   ↓
PostgreSQL

Async / scheduled workloads
   ↓
AWS services

AI
   ↓
AI provider

Infrastructure
   ↓
Terraform
```

Adapt to the actual existing architecture.

------------------------------------------------------------------------

# 13. Database Design Principles

The application will grow significantly, so avoid putting all fitness
information into one generic table.

Prefer domain-oriented entities.

Potential model:

``` text
users

user_goals

weight_logs

water_logs

protein_logs

workouts

workout_exercises

exercise_sets

exercises

personal_records

daily_notes

food_items

meals

meal_items

notifications

ai_insights
```

Exact schema should be determined from the current codebase.

Do not duplicate data unnecessarily.

For example, a workout's total volume should preferably be calculated
from sets rather than manually stored unless there is a performance
reason.

------------------------------------------------------------------------

# 14. API Design Principles

Use clear resource-oriented APIs.

Examples:

``` text
GET    /api/goals
PUT    /api/goals

GET    /api/weight
POST   /api/weight

GET    /api/water
POST   /api/water

GET    /api/protein
POST   /api/protein

GET    /api/workouts
POST   /api/workouts

GET    /api/workouts/{id}
PUT    /api/workouts/{id}
DELETE /api/workouts/{id}

GET    /api/progress/weight
GET    /api/progress/protein
GET    /api/progress/water

GET    /api/reports/weekly
GET    /api/reports/monthly

GET    /api/insights
```

These are examples, not mandatory endpoints.

Follow the application's existing API conventions if they differ.

------------------------------------------------------------------------

# 15. Error & Empty States

Every major screen must handle:

## Loading

Show appropriate skeleton/loading state.

## Empty

Example:

``` text
No workouts yet.

Start your first workout to begin
tracking your progress.

[ Start Workout ]
```

## Error

Example:

``` text
Couldn't load your progress.

[ Retry ]
```

## Offline / network failure

Where practical, clearly communicate whether the action succeeded
locally or failed.

Do not show fake successful saves.

------------------------------------------------------------------------

# 16. UX Quality Checklist

Before marking any phase complete, inspect:

### Navigation

-   Is the next action obvious?
-   Can the user return easily?
-   Are navigation labels clear?

### Forms

-   Are defaults sensible?
-   Is typing minimized?
-   Are units visible?
-   Are errors understandable?

### Dashboard

-   Can the user understand today's status within 3--5 seconds?
-   Are the most important metrics visually dominant?
-   Does every metric have useful context?

### Charts

-   Is the chart understandable without explanation?
-   Are units visible?
-   Is the selected time range obvious?
-   Are empty states handled?

### Mobile

-   Are touch targets large enough?
-   Is scrolling natural?
-   Is text readable?
-   Are buttons reachable?

### Accessibility

-   Keyboard navigation
-   Focus states
-   Labels
-   Contrast
-   Screen readers

------------------------------------------------------------------------

# 17. Performance Checklist

Do not optimize prematurely, but watch for:

-   unnecessary React re-renders,
-   repeated API calls,
-   N+1 database queries,
-   expensive analytics calculated on every request,
-   large history payloads,
-   unnecessary chart data,
-   unbounded queries.

For historical data, use pagination or time-range queries.

------------------------------------------------------------------------

# 18. Testing Strategy

## Unit Tests

Test:

-   goal calculations,
-   percentage calculations,
-   trend calculations,
-   PR detection,
-   workout volume,
-   weekly/monthly aggregations,
-   streak calculations.

Example:

``` text
Protein = 90
Target = 120

Expected = 75%
```

------------------------------------------------------------------------

## API Tests

Test:

-   valid requests,
-   invalid requests,
-   missing values,
-   boundary values,
-   unauthorized access,
-   nonexistent resources,
-   date filtering.

------------------------------------------------------------------------

## Frontend Tests

Test:

-   Quick Add,
-   goal editing,
-   progress display,
-   date selection,
-   workout creation,
-   set entry,
-   loading state,
-   empty state,
-   error state.

------------------------------------------------------------------------

## Regression Tests

Every phase should verify that previous phases still work.

------------------------------------------------------------------------

# 19. Phase Execution Protocol

When the developer says:

> Execute Phase X

the agent should follow this exact process.

### Step 1 --- Inspect

Understand the current codebase and identify relevant files.

### Step 2 --- Plan

Briefly describe:

-   files/components likely to change,
-   backend changes,
-   database changes,
-   API changes,
-   tests required.

Do not implement unrelated improvements.

### Step 3 --- Implement

Implement the phase incrementally.

### Step 4 --- Test

Run:

-   existing tests,
-   new tests,
-   linting,
-   type checks/build,
-   relevant integration tests.

### Step 5 --- Review

Check:

-   regressions,
-   UX,
-   accessibility,
-   error states,
-   responsive behavior,
-   data integrity.

### Step 6 --- Report

Provide:

``` text
Phase X completed.

Implemented:
- ...
- ...
- ...

Files changed:
- ...

Database changes:
- ...

API changes:
- ...

Tests:
- ...

Manual QA checklist:
- ...

Known limitations:
- ...
```

### Step 7 --- STOP

Do not continue to the next phase.

Wait for the developer to explicitly request it.

------------------------------------------------------------------------

# 20. Definition of Done

A feature is not done merely because it works in the happy path.

A phase is done when:

-   functionality works,
-   UI is polished,
-   data persists correctly,
-   loading states exist,
-   empty states exist,
-   error states exist,
-   validation exists,
-   automated tests pass,
-   existing functionality still works,
-   responsive behavior is acceptable,
-   no obvious console/backend errors remain.

------------------------------------------------------------------------

# 21. Feature Priority Summary

## Phase 1 --- Core Product Experience

**Highest priority**

-   Goals
-   Targets
-   Onboarding
-   Home dashboard
-   Quick Add
-   Progress bars
-   Weight trends
-   History improvements
-   Daily timeline

## Phase 2 --- Workout Tracking

-   Sets/reps
-   Exercise tracking
-   Previous performance
-   Workout summaries
-   PR detection
-   Workout history

## Phase 3 --- Analytics

-   Progress dashboard
-   Weekly report
-   Monthly report
-   Deterministic insights
-   Advanced charts

## Phase 4 --- AI

-   Daily AI summary
-   Weekly AI review
-   Personalized suggestions
-   AI chat

## Phase 5 --- Consistency

-   Smart reminders
-   Quiet hours
-   Streaks
-   Adaptive notification timing

## Phase 6 --- Nutrition

-   Food database
-   Meal tracking
-   Custom foods
-   AI meal estimation
-   Nutrition analytics

## Phase 7 --- Advanced Fitness

-   Exercise library
-   Progressive overload
-   Workout templates
-   Advanced PRs
-   Exercise analytics

## Phase 8 --- Integrations

-   Health platforms
-   Wearables
-   Smartwatch data
-   Calendar/notification integrations

## Phase 9 --- Production

-   Security
-   Performance
-   Observability
-   Infrastructure
-   Reliability
-   Cost controls

------------------------------------------------------------------------

# 22. Final Product Principle

Do not turn the application into a feature dump.

The best version of this product should make a user's daily interaction
extremely simple:

``` text
OPEN APP
   ↓
SEE HOW I'M DOING
   ↓
LOG SOMETHING IN SECONDS
   ↓
UNDERSTAND MY PROGRESS
   ↓
GET ONE USEFUL NEXT ACTION
   ↓
CLOSE APP
```

The application should become progressively smarter as more data
accumulates.

**Simple first. Intelligent second.**

The priority is not the number of features.

The priority is building a tight feedback loop between:

**data → insight → action → progress.**
