# DAT153 Task 3 - Review Report

**Target Repository:** [https://github.com/r33hAB/oblig1](https://github.com/r33hAB/oblig1)

**Commit Hash:** `7bae2ec9744d44414e1ed74e88b58cce093f919d`

**Commit text:** `test og oppdatert redame`

**Date:** 28. 3. 2025

**Reviewers:** Group 13: **Jan Soukup** and **Fabienne Failke**

---

## 1. Initial Checklist

- [x] Repository is accessible.
- [x] Application clones and builds without errors.
- [x] Application launches correctly on phone/emulator.

---

## 2. Comparative Analysis

### 2a) Gallery Interaction

#### **How does the code make the user select an image in the `GalleryActivity`?**

The process of selecting an image is handled through the **Storage Access Framework (SAF)**. 
Same as in our Project the **Intent** is not manually created but instead uses the `OpenDocument` contract. This uses the `ACTION_OPEN_DOCUMENT` intent.
This Project also uses **persistable URI permission** to ensure the image can be accessed later. It allows the user to browse through the system's file picker to select a specific file.
The return value is a **Uri** (Uniform Resource Identifier), which represents the location of the selected image. 
The handling happens inside the trailing lambda of `velgBildeLauncher`.

```kotlin
 private val velgBildeLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        // Hvis brukeren avbryter, er uri null
        // hvis null, avslutt callbacken
        uri ?: return@registerForActivityResult
        // Ber Android om å "huske" at appen har lesetilgang til denne bilde-urien
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // For resource-URIer eller i test er persistent tilgang ikke mulig
        }
        // Lagrer den valgte Uri i state.
        ventedeUri = uri
        // Setter state slik at "gi navn"-dialogen vises
        visLeggTilDialog = true
    }

```
The Parameter passed to the **Intent** is: `arrayOf("image/*")`.
It acts as a filter for the file picker, ensuring that the user can only select image files (JPEG, PNG, WebP, etc.).
Any non-image files will be greyed out in the picker. We also use this in our Project.

```kotlin
floatingActionButton = {
    FloatingActionButton(
        onClick = { velgBildeLauncher.launch(arrayOf("image/*")) }
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = stringResource(R.string.legg_til)
        )
    }
}
```

The **Uri** isn't actually "saved" to the app's permanent storage until the user clicks "Confirm" in the naming dialog.
This function handles the final conversion:

```kotlin

if (visLeggTilDialog && ventedeUri != null) {
    LeggTilNavnDialog(
        onBekreft = { navn ->
            val uri = ventedeUri ?: return@LeggTilNavnDialog
            viewModel.addImage(navn, uri.toString())
            ventedeUri = null
            visLeggTilDialog = false
        },
        onAvbryt = {
            ventedeUri = null
            visLeggTilDialog = false
        }
    )
}
```

**Our Solution:**

In our project, we use the modern `ActivityResultContracts.OpenDocument()` with **`rememberLauncherForActivityResult`**. This approach is cleaner than the legacy `startActivityForResult` as it is lifecycle-aware and specifically designed for **Jetpack Compose**.

**Our image launcher:**

```kotlin
// In GalleryScreen.kt
val pickImageLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri: Uri? ->
    uri?.let {
        // Request persistable permission immediately
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(it, takeFlags)
        newImageUri = it
        showNameDialog = true
    }
}

// Triggering the picker in our Compose UI
FloatingActionButton(
    onClick = { pickImageLauncher.launch(arrayOf("image/*")) },
    // ...
)
```

We also handle persistable URI permissions to ensure we can access the image later, similar to their solution, but encapsulated within the Compose state flow.

```kotlin
val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
context.contentResolver.takePersistableUriPermission(uri, takeFlags)
```

### 2b) Quiz Logic & Testing: Describe how the quiz chooses a new image for the next question and the two wrong choices to label the buttons.

#### **How is the check if the answer was correct implemented?**

**Their Solution:**
The check is implemented in the `QuizViewModel.submitAnswer(chosenAnswer: String)` function. 
It retrieves the correct name from the current state and performs a direct **string comparison**. 
The result is then used to update the `QuizUiState` with a new `QuizPhase.SvarGitt` object, which stores the boolean result.

Code Example of `submitAnswer()`:
```kotlin
// From QuizViewModel.kt (Other Group)
fun submitAnswer(chosenAnswer: String) {
  val phase = _uiState.value.phase
  if (phase !is QuizPhase.VenterPaaSvar) return

  val riktigSvar = phase.gjeldendeBilder.navn // Get correct name from state
  val erRiktig = chosenAnswer == riktigSvar    // Compare with user choice

  _uiState.update { state ->
    state.copy(
      phase = QuizPhase.SvarGitt(..., erRiktig = erRiktig),
    scoreRiktige = state.scoreRiktige + if (erRiktig) 1 else 0,
    scoreTotalt = state.scoreTotalt + 1
    )
  }
}
```

**Our Solution:**

Our implementation also uses **string comparison** but manages state via boolean flags and a dedicated `QuizUiState` sealed class. When an answer is submitted, we check it against the `correctName` property of our `QuizEntry`.

```kotlin
// In QuizViewModel.kt (Our Solution)
fun submitAnswer(option: String) {
    if (!isAnswered) {
        selectedOption = option
        isAnswered = true
        attempts++
        val state = _quizState.value
        if (state is QuizUiState.Question) {
            if (option == state.entry.correctName) {
                score++
            }
            updateCurrentQuestion()
        }
    }
}
```
  
#### **Will the quiz work correctly for an empty gallery or a gallery with only two pictures?**

**Their Solution:**

The quiz handles this edge case by enforcing a **minimum requirement of 3 images**. 
This is checked in the `loadNextQuestion()` function using a constant `MINIMUM_BILDER = 3`. 
If the gallery has less than 3 images, the quiz transitions to the `QuizPhase.ForFaaBilder` state, and the UI displays a dedicated error message (`ForFaaBilderMelding`) instead of starting the game.

Edge case error handling code:
```kotlin
// From QuizViewModel.kt (Other Group)
private const val MINIMUM_BILDER = 3

private fun loadNextQuestion() {
  viewModelScope.launch {
    // ... (loading list from repository)
    val list = repository.getAllAsc().first()

    if (list.size < MINIMUM_BILDER) { // Explicit check for 0, 1, or 2 images
      _uiState.update { it.copy(phase = QuizPhase.ForFaaBilder) }
      return@launch // Stop execution
    }
    // ... (rest of quiz generation logic)
  }
}
```

**Our Solution:**

In our project, we handle this through data observation. If the gallery is empty, the `QuizViewModel` remains in the **`Loading` state** because our `loadQuiz` function only proceeds to `updateCurrentQuestion()` if the items list is not empty. 
For small galleries (1 or 2 images), the quiz **will still run**, but the `generateQuizEntry` logic will simply provide fewer distractor options (resulting in 1 or 2 buttons instead of 4).

```kotlin
// In QuizViewModel.kt (Our Solution)
repository.allItems.collect { items ->
    if (items.isNotEmpty()) {
        // ... mapping and filtering logic ...
        updateCurrentQuestion()
    }
}
```

Our question generation logic:
```kotlin
// In QuizUtil.kt (Our Solution)
fun generateQuizEntry(correctEntry: GalleryEntry, gallery: List<GalleryEntry>): QuizEntry {
    // Filters out the correct answer and takes up to 3 distractors
    val wrongNames = gallery.filter { it.name != correctEntry.name }
        .shuffled().take(3).map { it.name }
    val options = (wrongNames + correctEntry.name).shuffled()
    return QuizEntry(correctEntry.uri, options, correctEntry.name)
}
```

#### **Is the test case easy to generalize into a loop?**

**Their Solution:**

Yes, their solution is highly testable because they use a **ViewModel** with a single `uiState` (`StateFlow`). 
To create a generalized loop that answers a random number of questions correctly or incorrectly, a test only needs to inspect the current state's available options.

How could a loop look like in their tests:
```kotlin
@Test
fun randomized_long_run_test() {
val viewModel = composeTestRule.activity.viewModel
val totalRounds = 10

    repeat(totalRounds) {
        // 1. Wait for question state
        composeTestRule.waitUntil { viewModel.uiState.value.phase is QuizPhase.VenterPaaSvar }
        
        val phase = viewModel.uiState.value.phase as QuizPhase.VenterPaaSvar
        val isCorrectRound = Random.nextBoolean()
        
        // 2. Decide answer based on random boolean
        val answerText = if (isCorrectRound) {
            phase.gjeldendeBilder.navn 
        } else {
            phase.svaralternativer.first { it != phase.gjeldendeBilder.navn }
        }

        // 3. Perform click and verify UI
        composeTestRule.onNodeWithText(answerText).performClick()
        val expectedStatus = if (isCorrectRound) "Riktig!" else "Feil!"
        composeTestRule.onNodeWithText(expectedStatus).assertExists()

        // 4. Move to next question
        composeTestRule.onNodeWithText("Neste sp\u00F8rsm\u00E5l").performClick()
    }
}
```

**Our Solution:**

Our solution is also built for easy generalization. In `FullQuizRunTest.kt`, we implemented a helper function `answerQuestion` that dynamically finds the correct (or an incorrect) answer text by inspecting the `ViewModel` state before performing the click. This allows us to run a full quiz with any distribution of correct/incorrect answers.

```kotlin
// In FullQuizRunTest.kt (Our Solution)
private fun answerQuestion(shouldBeCorrect: Boolean, expectedScore: Int, questionNumber: Int) {
    val uiState = viewModel.quizState.value as? QuizUiState.Question
    val correctAnswer = uiState?.entry?.correctName
    val answerToClick = if (shouldBeCorrect) {
        correctAnswer
    } else {
        uiState?.entry?.options?.find { it != correctAnswer }
    }

    answerToClick?.let {
        composeTestRule.onNodeWithText(it).performClick()
    }
    // ... assertions ...
    if (questionNumber < totalQuestions) {
        composeTestRule.onNodeWithText("Next").performClick()
    }
}
```

---

### 2c) ContentProvider Review

#### **Do the returned URIs correspond to the URI of the content provider?**

**Their Solution:**

No. The returned URIs in the URI column do not begin with the provider's authority (`com.example.quizapp.provider.gallery`). Instead, they return the **direct, underlying URI** of the resource.
As seen in the `adb` output, the provider returns values like `android.resource://com.example.quizapp/2131165320`. 

**Our Solution:**

Our **`QuizProvider`** follows a similar pattern. While the provider itself is accessed via `content://com.example.quiz_app.provider/quiz_items`, the URIs returned in the "URI" column are the original URIs stored in our database (which can be `content://` URIs from SAF or other sources).

#### **Does the provider implement the mandatory columns?**

**Their Solution:**

No. The provider does not implement the mandatory columns defined in the **`OpenableColumns`** interface.
 - It uses `name` instead of `DISPLAY_NAME` (`_display_name`).
 - It completely lacks the `SIZE` (`_size`) column.
 - It uses a custom column name `URI` instead of standard Android content constants.

**Our Solution:**

Our provider also prioritizes the task's requested column names (`name` and `URI`). We use **SQL aliasing** to map our internal Room database columns to these specific names required by the task, rather than implementing `OpenableColumns`.

```kotlin
// In QuizProvider.kt (Our Solution)
override fun query(...): Cursor? {
    if (uriMatcher.match(uri) == ITEMS) {
        // Requirement specifies column names "name" and "URI".
        // We use SQL 'AS' to rename the internal 'uri' column to 'URI'.
        val cursor = database.query("SELECT name, uri AS URI FROM quiz_items", null)
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }
    return null
}
```

#### **Test the content provider from the command line with `adb` — make separate tests with `--projection` and `--where` arguments. If it fails, explain why.**

**Their Solution:**

- **Projection Test (--projection):** 
  - Command: `adb shell content query --uri content://com.example.quizapp.provider.gallery/gallery_items --projection name`
  - Result: **Crashed** with the following output:
  ```text
Error while accessing provider:com.example.quizapp.provider.gallery
java.lang.IllegalArgumentException: columnNames.length = 1, columnValues.length = 3
        at android.database.DatabaseUtils.readExceptionFromParcel(DatabaseUtils.java:183)
        at android.database.DatabaseUtils.readExceptionFromParcel(DatabaseUtils.java:153)
        at android.content.ContentProviderProxy.query(ContentProviderNative.java:495)
        at com.android.commands.content.Content$QueryCommand.onExecute(Content.java:661)
        at com.android.commands.content.Content$Command.execute(Content.java:522)
        at com.android.commands.content.Content.main(Content.java:735)
        at com.android.internal.os.RuntimeInit.nativeFinishInit(Native Method)
        at com.android.internal.os.RuntimeInit.main(RuntimeInit.java:410)
  ```
  - Reason: This is a coding error in their query method. When you provide a projection (e.g., just "name"), the `MatrixCursor` is initialized with 1 column. However, inside their for loop, they hardcode `cursor.addRow(arrayOf(entity.id, entity.name, entity.uri))`, which attempts to insert 3 values. This mismatch causes the provider to crash when any projection is used.

- **Where Test (--where):** 
  - Command: `adb shell content query --uri content://com.example.quizapp.provider.gallery/gallery_items --where "name='Hund'"`
  - Result: The command ran, but the `selection` parameters were **completely ignored**, returning all rows:
  ```text
Row: 0 _id=2, name=Hund, URI=android.resource://com.example.quizapp/2131165320
Row: 1 _id=3, name=Kanin, URI=android.resource://com.example.quizapp/2131165321
  ```
  - Reason: Looking at the code, the `selection` and `selectionArgs` parameters are passed into the query function but are never used in the DAO call.

**Our Solution:**

- **Projection Test:** 
  - Command: `adb shell content query --uri content://com.example.quiz_app.provider/quiz_items --projection name`
  - Result: Our `QuizProvider` returns all rows and all columns, ignoring the projection but **not crashing**:
  ```text
Row: 0 name=Jupiter, URI=android.resource://com.example.quiz_app/2131165280
Row: 1 name=Mars, URI=android.resource://com.example.quiz_app/2131165281
Row: 2 name=Venus, URI=android.resource://com.example.quiz_app/2131165299
Row: 3 name=Uranus, URI=android.resource://com.example.quiz_app/2131165298
Row: 4 name=hdrhhe, URI=content://com.android.providers.media.documents/document/image%3A1000018582
  ```
  - Reason: We use **Room's `database.query()`** with a hardcoded SQL string `SELECT name, uri AS URI FROM quiz_items`. This returns a cursor directly from SQLite. It doesn't crash because the cursor structure is managed by SQLite, but it ignores the `projection` argument.

- **Where Test:** 
  - Command: `adb shell content query --uri content://com.example.quiz_app.provider/quiz_items --where "name=Mars"`
  - Result: Similar to the other group, our current implementation **ignores filtering** and returns all rows:
  ```text
Row: 0 name=Jupiter, URI=android.resource://com.example.quiz_app/2131165280
Row: 1 name=Mars, URI=android.resource://com.example.quiz_app/2131165281
Row: 2 name=Venus, URI=android.resource://com.example.quiz_app/2131165299
Row: 3 name=Uranus, URI=android.resource://com.example.quiz_app/2131165298
Row: 4 name=hdrhhe, URI=content://com.android.providers.media.documents/document/image%3A1000018582
  ```
  - Reason: Our current implementation passes `null` for the selection arguments in the SQL query. Thus, the selection logic is not yet implemented in the provider's `query` method.

---

## 3. Comparison Summary

| Feature                | Our Solution | Their Solution |
|:-----------------------|:--|:--|
| **Quiz Logic**         | Logic in `QuizUtil` (pure functions). | Logic in `QuizViewModel` using a state machine. |
| **Gallery Interaction**| `OpenDocument()` via Compose launcher. | `OpenDocument()` via `registerForActivityResult`. |
| **Small Gallery**      | Runs with available distractors (1-3 options). | Shows a specific **"Too Few Images"** error screen. |
| **ADB Stability**      | Stable but ignores filtering. | **Crashes** on custom projections. |
| **Naming**             | English naming convention. | Norwegian naming convention (e.g., `svaralternativer`). |

---

## 4. Conclusion & Observations

Reviewing Group 4's project was an insightful experience. 

**What we learned:**
- Their approach to **edge-case handling** (the `MINIMUM_BILDER` check) is very robust and provides a better user experience than allowing a "broken" quiz to start.
- The **projection crash** in their `ContentProvider` highlighted the importance of dynamically handling column counts when manually building a `MatrixCursor`.
- Both groups chose to prioritize the task's custom naming requirements (`name`/`URI`) over the standard `OpenableColumns`, which is understandable given the project specifications.

Overall, their solution is well-structured and functional, with clear separation of states in the `ViewModel`, though it could benefit from more dynamic SQL handling in the `ContentProvider`.
