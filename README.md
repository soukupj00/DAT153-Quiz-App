# DAT153-Quiz-App

An Android Quiz application for subject DAT153 Android App Development.

## Project Structure

- **`data`**: Handles data persistence and sharing.
    - `AppDatabase`: Room database setup with a thread-safe Singleton pattern.
    - `QuizItem`: The Entity representing a name/image pair in the database.
    - `QuizItemDao`: Defines the SQL operations for the database.
    - `QuizRepository`: An abstraction layer that provides a clean API for the rest of the app to
      access data.
    - `QuizProvider`: A **ContentProvider** that exposes the names and image URIs to external
      applications (read-only).
- **`quiz`**: Contains the logic and UI for the quiz game.
    - `QuizViewModel`: Manages the quiz state, scoring, and persistence across process death using
      `SavedStateHandle`.
    - `QuizScreen`: A responsive Compose UI that adapts to portrait and landscape orientations.
    - `FinalScoreScreen`: Displays the results and navigation options at the end of a quiz.
- **`gallery`**: Manages the collection of quiz items.
    - `GalleryActivity`: The host for the gallery UI, managing top-level sorting state.
    - `GalleryScreen`: Grid screen allowing users to add, view, and delete quiz items.
- **`types`**: Data classes used across the application.
- **`ui`**: Shared UI components and theme definitions (not needed for the course but makes it
  nicer)

## Testing

### Manual Testing (Content Provider)

To verify the `QuizProvider` and inspect the database externally, we can use the Android Debug
Bridge (adb):

1. Ensure the app is installed and running on a device or emulator.
2. Run the following command in your terminal:
   ```bash
   adb shell content query --uri content://com.example.quiz_app.provider/quiz_items
   ```

**Output:**

```shell
adb shell content query --uri content://com.example.quiz_app.provider/quiz_items
Row: 0 name=Jupiter, URI=android.resource://com.example.quiz_app/2131165280
Row: 1 name=Mars, URI=android.resource://com.example.quiz_app/2131165281
Row: 2 name=Venus, URI=android.resource://com.example.quiz_app/2131165299
Row: 3 name=Uranus, URI=android.resource://com.example.quiz_app/2131165298
```

### Future Automated Testing

**TODO**