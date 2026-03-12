# Task 2: Advanced Android Features & Testing

This document provides a technical overview of the improvements implemented in Task 2 of the DAT153
Quiz App. We transitioned from simple in-memory data to a persistent architecture using Room,
introduced cross-process data sharing with a ContentProvider, and implemented a robust automated
testing suite.

---

## 1. Data Persistence with Room

To meet the requirement of saving and loading data, we implemented **Android Room**, which provides
an abstraction layer over SQLite.

### Entity: `QuizItem`

The data model is defined as a Kotlin data class annotated with `@Entity`.

- **`@PrimaryKey(autoGenerate = true)`**: Ensures each entry has a unique ID handled by the
  database.
- **Unified URI Storage**: Instead of storing heavy Bitmaps, we store the `String` representation of
  the image URI (e.g., `android.resource://...` or `content://...`). This allows us to treat both
  built-in resources and user-added files through a single, consistent code path.

### Data Access Object (DAO): `QuizItemDao`

The DAO defines the SQL operations using Kotlin Coroutines:

- **`Flow<List<QuizItem>>`**: The `getAllItems()` method returns a `Flow`. This is a reactive stream
  that automatically emits new data whenever the database table changes.
- **`suspend` functions**: Insert and delete operations are marked as `suspend` to ensure they are
  called from a background thread, preventing UI freezes.

### Thread-Safe Database: `AppDatabase`

- Uses the **Singleton pattern** to prevent multiple instances of the database from being opened
  simultaneously.
- **`@Volatile`**: Ensures the database instance is always up-to-date across different threads.
- **Pre-population**: Uses a `RoomDatabase.Callback` to insert default planet data the very first
  time the app is run.

---

## 2. State Management with ViewModel

The `QuizViewModel` is responsible for the business logic and ensuring state is preserved during
configuration changes (like screen rotation).

### `SavedStateHandle`

To prevent losing the current question index or the score during rotation, we use
`SavedStateHandle`.

- **Delegates**: We implemented a custom property delegate `savedStateHandle.delegate(defaultValue)`
  which allows us to treat saved state variables like regular properties while ensuring they are
  persisted across process death.
- **Flow-based UI State**: The ViewModel exposes a `StateFlow<QuizUiState>`. The UI (Compose)
  collects this state, ensuring that the view is always a "dumb" reflection of the current data.

---

## 3. Data Sharing with `ContentProvider`

The `QuizProvider` allows external applications to read the app's data.

- **`UriMatcher`**: Used to identify incoming URI requests (e.g.,
  `content://com.example.quiz_app.provider/quiz_items`).
- **SQL Aliasing**: The requirement specified column names `"name"` and `"URI"`. Since our internal
  database uses lowercase `uri`, we used SQL aliasing in the provider:
  `SELECT name, uri AS URI FROM quiz_items`.
- **Read-Only**: The `insert`, `update`, and `delete` methods throw `UnsupportedOperationException`
  to ensure data integrity from external callers.

---

## 4. Automated Testing (Espresso & Compose)

We implemented four major test classes to ensure the app's reliability.

### Navigation Testing (`NavigationTest`)

Uses **Espresso Intents** to verify that clicking a button on the `MainActivity` actually triggers
an `Intent` to the correct target Activity (`GalleryActivity`). This tests the "contract" between
screens without needing to inspect the UI of the second screen.

### Logic Testing (`QuizScoreTest` & `FullQuizRunTest`)

- These tests use `createAndroidComposeRule<QuizActivity>()` to interact with the Compose-based UI.
- **Internal State Access**: The tests access the `viewModel` property of the `QuizActivity` to
  programmatically determine which answer is correct, then use
  `onNodeWithText(correctAnswer).performClick()` to simulate a perfect user.
- **Verification**: They assert that the UI text (e.g., `"Score: 1 / 1"`) matches the expected
  logical state.

### Intent Stubbing (`GalleryIntentsTest`)

One of the most complex requirements was adding an image without user interaction.

- **`Intents.intending(...)`**: We "intercept" the system intent `ACTION_OPEN_DOCUMENT` (the file
  picker).
- **`respondWith(result)`**: We tell the test to immediately return a predefined URI (the Jupiter
  image from resources) as if the user had picked it.
- This allows us to test the "Add to Gallery" flow in a completely automated, headless manner.

---

## 5. Utility Functions (`QuizUtil.kt`)

To keep the ViewModel clean, the logic for generating a question is extracted into a pure function:

- **`generateQuizEntry`**: Takes the correct entry and the full list of available items. It filters
  out the correct answer, shuffles the remaining items, takes 3 "distractors," and returns a
  shuffled list of 4 options. This ensures the correct answer isn't always in the same position.

---

## 6. Professional Performance & UI Robustness

### Professional Image Rendering
To ensure a jank-free UI experience, we implemented an asynchronous bitmap decoding strategy:
- **`produceState` & `Dispatchers.IO`**: Heavy decoding tasks are offloaded from the main thread to a dedicated background thread. `produceState` manages the lifecycle, ensuring that loading is cancelled if the user navigates away.
- **Cross-Version Compatibility**: The system automatically switches between the modern `ImageDecoder` (API 28+) and the legacy `MediaStore` APIs to ensure the app works smoothly on all supported Android versions.

### Robust Testing Strategy
We shifted from fragile UI text-based assertions to robust logic-based assertions:
- **ViewModel State Verification**: Tests now directly verify the `ViewModel` state (e.g., `score`, `isAnswered`) after user interactions. This makes the tests resilient to changes in UI strings or formatting.
- **Dynamic Question Handling**: Tests programmatically identify the correct answer from the current state, allowing them to remain valid even if the underlying database changes.

### Enhanced UX Logic (Loading States)
We improved the Gallery's entry-point to provide better visual feedback:
- **Three-State Handling**: By distinguishing between "Loading", "Empty", and "Data Ready" states, we eliminated the jarring flash of "The gallery is empty" text.
- **Visual Feedback**: A `CircularProgressIndicator` is now shown while the database is being queried or images are being decoded.
