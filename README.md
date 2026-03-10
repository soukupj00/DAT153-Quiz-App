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
### Automated Testing

To verify the expected behaviour of our app we use the Espresso Framework to implement the following testcases. The basic stucture of these testcases is as follows:

```kotlin  
@Before
fun setUp() {
    Intents.init()
}

@After
fun tearDown() {
    Intents.release()
}

@Test
fun testSomething() {
    ...
}

```

#### Navigation Test

The Navigation Test verifies the navigation from MainActivity to GalleryActivity.
This is achived by using the functionallity of the Espresso Intents.
1. We use onView() to assert that the state of the gallery button equals isDisplayed().
2. Using onView() we perform a click action on the button.
3. Using intended() we assert that the correct activity is launched.

##### Output:
```shell
2026-03-09 15:50:03.726 27655-27770 NavigationTest          com.example.quiz_app                 D  Starting test: testNavigateToGallery
2026-03-09 15:50:03.727 27655-27770 NavigationTest          com.example.quiz_app                 D  Checking if gallery button is displayed
2026-03-09 15:50:36.461 27655-27770 NavigationTest          com.example.quiz_app                 D  Clicking gallery button
2026-03-09 15:50:37.559 27655-27770 NavigationTest          com.example.quiz_app                 D  Verifying start intent for GalleryActivity
2026-03-09 15:50:37.569 27655-27770 NavigationTest          com.example.quiz_app                 D  Finished test: testNavigateToGallery
```

#### QuizScore Test

The QuizScore Test verifies that the score updated in the expected manner.

##### Correct answer:
1. We get the correct answer:
   * create an instance of ViewModel
   * use this instance to get the first question
   * get the correct answer from the question
2. Click the node with the correct answer text
3. Verify that the score is 1/1
4. Click the next button

##### Incorrect answer:

1. We get the incorrect answer:
   * create an instance of ViewModel
   * use this instance to get the second question
   * get the correct answer from the question
   * get an answer where the text != correct answer
2. Click the node with the incorrect answer text
3. Verify that the score is 1/2

##### Output:
```shell
2026-03-09 16:25:18.443  7843-8039  QuizScoreTest           com.example.quiz_app                 D  Starting test: testScoreUpdatesCorrectly
2026-03-09 16:25:18.443  7843-8039  QuizScoreTest           com.example.quiz_app                 D  Getting correct answer for the first question
2026-03-09 16:25:18.444  7843-8039  QuizScoreTest           com.example.quiz_app                 D  Clicking correct answer: Mars
2026-03-09 16:25:18.772  7843-8039  QuizScoreTest           com.example.quiz_app                 D  Verifying score is 1 / 1
2026-03-09 16:25:18.842  7843-8039  QuizScoreTest           com.example.quiz_app                 D  Clicking 'Next' button
2026-03-09 16:25:18.860  7843-8039  QuizScoreTest           com.example.quiz_app                 D  Getting incorrect answer for the second question
2026-03-09 16:25:18.860  7843-8039  QuizScoreTest           com.example.quiz_app                 D  Clicking incorrect answer: Mars
2026-03-09 16:25:18.942  7843-8039  QuizScoreTest           com.example.quiz_app                 D  Verifying score is 1 / 2
2026-03-09 16:25:19.008  7843-8039  QuizScoreTest           com.example.quiz_app                 D  Finished test: testScoreUpdatesCorrectly
```
#### GalleryIntents Test
This test verifies adding and deleting an entry in the gallery.
It uses Intent Stubbing to simulate picking an image from the device.

##### Add:
1. Prepare a dummy image using an URI parsed from our apps ressources
2. Create a stub result for the image picker intent
3. Stub the ACTION_OPEN_DOCUMENT intent to return our result without UI
4. We click on the "Add Image" button
5. We enter the name "New Test Entry" for our image
6. We click on the "Add" button
7. We verify that the item "New Test Entry" exists

##### Delete:
1. We long-press the item "New Test Entry" to delete it
2. We confirm the deletion 
3. We verify that the item "New Test Entry" is deleted

##### Output:

```shell
2026-03-09 16:55:48.150 17243-17509 GalleryIntentsTest      com.example.quiz_app                 D  Starting test: testAddAndDeleteNewItem
2026-03-09 16:55:48.188 17243-17509 GalleryIntentsTest      com.example.quiz_app                 D  Intent stubbed for ACTION_OPEN_DOCUMENT
2026-03-09 16:55:48.188 17243-17509 GalleryIntentsTest      com.example.quiz_app                 D  Clicking 'Add Image' button
2026-03-09 16:55:48.529 17243-17509 GalleryIntentsTest      com.example.quiz_app                 D  Entering name: New Test Entry
2026-03-09 16:55:49.390 17243-17509 GalleryIntentsTest      com.example.quiz_app                 D  Verifying item 'New Test Entry' exists
2026-03-09 16:55:49.600 17243-17509 GalleryIntentsTest      com.example.quiz_app                 D  Long-pressing item 'New Test Entry' to delete
2026-03-09 16:55:49.671 17243-17509 GalleryIntentsTest      com.example.quiz_app                 D  Confirming deletion
2026-03-09 16:55:49.798 17243-17509 GalleryIntentsTest      com.example.quiz_app                 D  Verifying item 'New Test Entry' is deleted
2026-03-09 16:55:49.883 17243-17509 GalleryIntentsTest      com.example.quiz_app                 D  Finished test: testAddAndDeleteNewItem          com.example.quiz_app                 D  Finished test: testScoreUpdatesCorrectly
```

#### FullQuizRun Test
This test simulates a complete walkthrough of the quiz. 
It verifies the scoring locic with a mix of correct and incorect answers and checks that the final navigation leads back to the  home screen. 

1. Use createAndroidComposeRule to launch QuizActivity
2. Select the correct answer for the first question -> Verify that the score is 1/1
3. Select an incorect answer for the second question -> Verify that the score is 1/2
4. Select the correct answer for the third question -> Verify that the score is 2/3
5. Select an incorect answer for the fourth question -> Verify that the score is 2/4
6. Click the "Finish" button -> Verify that the final score is 2/4
7. Click the "Home" button
8. Verify that the intent for MainActivity is launched




##### Output:
```shell
2026-03-10 09:21:17.879 30422-30652 FullQuizRunTest         pid-30422                            D  Starting test: testFullQuizRun
2026-03-10 09:21:17.890 30422-30652 FullQuizRunTest         pid-30422                            D  Answering question 1. Correct: true. Clicking: Uranus
2026-03-10 09:21:18.137 30422-30652 FullQuizRunTest         pid-30422                            D  Verifying score is 'Score: 1 / 1'
2026-03-10 09:21:18.259 30422-30652 FullQuizRunTest         pid-30422                            D  Answering question 2. Correct: false. Clicking: Mars
2026-03-10 09:21:18.377 30422-30652 FullQuizRunTest         pid-30422                            D  Verifying score is 'Score: 1 / 2'
2026-03-10 09:21:18.466 30422-30652 FullQuizRunTest         pid-30422                            D  Answering question 3. Correct: true. Clicking: Mars
2026-03-10 09:21:18.665 30422-30652 FullQuizRunTest         pid-30422                            D  Verifying score is 'Score: 2 / 3'
2026-03-10 09:21:18.751 30422-30652 FullQuizRunTest         pid-30422                            D  Answering question 4. Correct: false. Clicking: Mars
2026-03-10 09:21:18.840 30422-30652 FullQuizRunTest         pid-30422                            D  Verifying score is 'Score: 2 / 4'
2026-03-10 09:21:18.927 30422-30652 FullQuizRunTest         pid-30422                            D  Quiz finished, navigating to final score screen
2026-03-10 09:21:18.927 30422-30652 FullQuizRunTest         pid-30422                            D  Verifying final score is 2 / 4
2026-03-10 09:21:19.011 30422-30652 FullQuizRunTest         pid-30422                            D  Clicking 'Home' button
2026-03-10 09:21:19.045 30422-30652 FullQuizRunTest         pid-30422                            D  Verifying intent for MainActivity
2026-03-10 09:21:19.233 30422-30652 FullQuizRunTest         pid-30422                            D  Finished test: testFullQuizRun
2026-03-10 09:22:49.897 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Starting test: testFullQuizRun
2026-03-10 09:22:49.902 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Answering question 1. Correct: true. Clicking: Uranus
2026-03-10 09:22:50.107 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Verifying score is 'Score: 1 / 1'
2026-03-10 09:22:50.205 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Answering question 2. Correct: false. Clicking: Jupiter
2026-03-10 09:22:50.415 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Verifying score is 'Score: 1 / 2'
2026-03-10 09:22:50.504 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Answering question 3. Correct: true. Clicking: Jupiter
2026-03-10 09:22:50.612 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Verifying score is 'Score: 2 / 3'
2026-03-10 09:22:50.717 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Answering question 4. Correct: false. Clicking: Mars
2026-03-10 09:22:50.809 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Verifying score is 'Score: 2 / 4'
2026-03-10 09:22:50.895 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Quiz finished, navigating to final score screen
2026-03-10 09:22:50.895 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Verifying final score is 2 / 4
2026-03-10 09:22:50.978 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Clicking 'Home' button
2026-03-10 09:22:51.019 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Verifying intent for MainActivity
2026-03-10 09:22:51.236 32272-32440 FullQuizRunTest         com.example.quiz_app                 D  Finished test: testFullQuizRun
```

