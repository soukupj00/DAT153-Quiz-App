# DAT153-Quiz-App

An Android Quiz application where users match planet names with their respective images. This project demonstrates modern Android development practices using Jetpack Compose, Room Database, and Content Providers.

## Project Structure

The application follows a clean architecture pattern, organized into the following packages:

- **`data`**: Handles data persistence and sharing.
    - `AppDatabase`: Room database setup with a thread-safe Singleton pattern.
    - `QuizItem`: The Entity representing a name/image pair in the database.
    - `QuizItemDao`: Defines the SQL operations for the database.
    - `QuizRepository`: An abstraction layer that provides a clean API for the rest of the app to access data.
    - `QuizProvider`: A **ContentProvider** that exposes the names and image URIs to external applications (read-only).
- **`quiz`**: Contains the logic and UI for the quiz game.
    - `QuizViewModel`: Manages the quiz state, scoring, and persistence across process death using `SavedStateHandle`.
    - `QuizScreen`: A responsive Compose UI that adapts to portrait and landscape orientations.
    - `FinalScoreScreen`: Displays the results and navigation options at the end of a quiz.
- **`gallery`**: Manages the collection of quiz items.
    - `GalleryActivity`: The host for the gallery UI, managing top-level sorting state.
    - `GalleryScreen`: A dynamic grid that allows users to view, add (via `OpenDocument` intent), and delete quiz items.
- **`types`**: Common data classes used across the application.
- **`ui`**: Shared UI components and theme definitions.

## Features

- **Responsive Design**: Custom layouts for both portrait and landscape modes.
- **Data Persistence**: Uses Room to store built-in planets and user-added photos.
- **Modern UI**: Built entirely with Jetpack Compose (except for the XML-based Main Menu as per requirements).
- **Content Sharing**: External apps can query the quiz database via a published Content Provider.

## Testing

### Manual Testing (Content Provider)

To verify the `QuizProvider` and inspect the database externally, you can use the Android Debug Bridge (adb):

1. Ensure the app is installed and running on a device or emulator.
2. Run the following command in your terminal:
   ```bash
   adb shell content query --uri content://com.example.quiz_app.provider/quiz_items
   ```

**Expected Output:**
```shell
Row: 0 name=Jupiter, URI=android.resource://com.example.quiz_app/2131165280
Row: 1 name=Mars, URI=android.resource://com.example.quiz_app/2131165281
Row: 2 name=Venus, URI=android.resource://com.example.quiz_app/2131165299
Row: 3 name=Uranus, URI=android.resource://com.example.quiz_app/2131165298
```

### Future Automated Testing

The project is structured to support automated testing in future iterations:

- **Unit Tests (`test` artifact)**: 
    - Will be used to test `QuizViewModel` logic (scoring, question rotation) and `generateQuizEntry` utility functions.
    - Focus on verifying business logic in isolation without Android dependencies.
- **Instrumented Tests (`androidTest` artifact)**:
    - Will be used for UI testing with **Compose Test Rule**.
    - Will verify database integrations and end-to-end quiz flows on actual devices.

## Installation

1. Clone the repository.
2. Open the project in **Android Studio Ladybug (2024.2.1)** or newer.
3. Sync Gradle and run the `:app` module.

---
*Developed as part of DAT153 - Western Norway University of Applied Sciences.*
