# Productivity by Larson

A Tinder-style "things to do" productivity app for Android, built with Java and Gradle.

## Overview

Productivity by Larson reimagines task management as a swipe-based experience. Tasks are presented as profile cards (similar to Tinder), displayed in priority order. Users swipe right to commit to a task or swipe left to skip it for now.

## Current State

The app is a fully functional Android application written in Java (converted from an initial Kotlin/Jetpack Compose template). It uses traditional XML layouts with Material Design components, Room database for persistence, and WorkManager for background processing.

### Implemented Features

- **Swipe-based task interface** - Tasks appear as cards that can be swiped right ("Do it!") or left ("Skip"). Cards include animated swipe indicators and a stacked card visual effect.
- **Task creation with profile-style cards** - Each task includes:
  - Title and description
  - Task image (picked from gallery)
  - Category color (7 color options)
  - Four scoring attributes: Urgency, Importance, Desire, Creative (each 1-10)
  - Optional deadline with date and time picker
- **Priority scoring system** - Tasks are sorted by a weighted priority score:
  - `Score = (Urgency × 3) + (Importance × 3) + (Desire × 2) + (Creative × 2)`
  - Maximum score: 100, minimum: 10
- **Dynamic urgency scaling** - Tasks with deadlines automatically increase in urgency as the deadline approaches:
  - 90+ days away → urgency 1
  - 60-89 days → urgency 2
  - 30-59 days → urgency 3
  - 14-29 days → urgency 4
  - 7-13 days → urgency 5
  - 3-6 days → urgency 6
  - 2 days → urgency 7
  - 1 day → urgency 8
  - 2-24 hours → urgency 9
  - < 1 hour or overdue → urgency 10
- **Deadline notifications** - Alarms are scheduled at 24 hours, 1 hour, and 15 minutes before a task's deadline, sending push notifications to the user's phone.
- **Task timer** - When swiping right on a task, users are prompted to set a focus timer (5-120 minutes, default 25 minutes) to prevent getting lost in a task. A notification fires when the timer completes.
- **Wellbeing encouragement** - The app encourages users to add tasks without deadlines that have high Desire and Creative scores. A banner on the main screen and contextual encouragement text on the add task screen promote this behavior.
- **Completed tasks view** - A dedicated screen shows all completed tasks with their category colors.
- **Background urgency updates** - A WorkManager periodic job runs every hour to recalculate urgency scores for tasks with deadlines.
- **Room database** - All tasks are persisted locally using Room with LiveData observation for reactive UI updates.

### Architecture

```
com.larson.productivitybylarson/
├── model/
│   └── Task.java              # Room entity with all task fields
├── dao/
│   └── TaskDao.java           # Room DAO with priority-sorted queries
├── database/
│   └── TaskDatabase.java      # Room database singleton
├── adapter/
│   ├── SwipeCardView.java     # Custom swipe card UI with touch handling
│   └── CompletedTaskAdapter.java  # RecyclerView adapter for completed tasks
├── util/
│   ├── PriorityCalculator.java    # Priority score and dynamic urgency logic
│   └── NotificationHelper.java    # Notification channels, alarms, and timers
├── receiver/
│   └── DeadlineAlarmReceiver.java # BroadcastReceiver for deadline/timer alerts
├── worker/
│   └── UrgencyUpdateWorker.java   # Periodic background urgency recalculation
├── MainActivity.java              # Main swipe interface
├── AddTaskActivity.java           # Task creation form
└── CompletedTasksActivity.java    # Completed tasks list
```

### Tech Stack

- **Language:** Java 11
- **UI:** XML layouts with Material Design Components
- **Database:** Room (SQLite)
- **Background:** WorkManager
- **Image loading:** Glide
- **Notifications:** AlarmManager + NotificationCompat
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 36

### Build & Run

1. Open the project in IntelliJ IDEA or Android Studio
2. Sync Gradle dependencies
3. Connect an Android device or start an emulator (API 26+)
4. Run the `app` module

### Testing

Unit tests for the priority calculator are included:

```bash
./gradlew test
```
