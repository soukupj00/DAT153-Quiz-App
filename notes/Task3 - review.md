# DAT153 Task 3 - Review Report

**Target Repository:** [https://github.com/r33hAB/oblig1](https://github.com/r33hAB/oblig1)
**Commit Hash:** ...
**Date:** ...
**Reviewers:** Group 13: **Jan Soukup** and **Fabienne Failke**

---

## 1. Initial Checklistk

- [ ] Repository is accessible.
- [ ] Application clones and builds without errors.
- [ ] Application launches correctly on phone/emulator.

---

## 2. Comparative Analysis

### 2a) Gallery Interaction

**How does the code make the user select an image in the `GalleryActivity`?**

In our project, we use the modern `ActivityResultContracts.OpenDocument()` with
`rememberLauncherForActivityResult`. This approach is cleaner than the legacy
`startActivityForResult` as it is lifecycle-aware and specifically designed for Jetpack Compose.

**Code Example (Our Solution):**

```kotlin
// In GalleryScreen.kt
val pickImageLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri: Uri? ->
    uri?.let {
        newImageUri = it
        showNameDialog = true
    }
}

// Triggering the picker
FloatingActionButton(
    onClick = { pickImageLauncher.launch(arrayOf("image/*")) },
    // ...
)
```

We also handle persistable URI permissions to ensure we can access the image later:

```kotlin
val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
context.contentResolver.takePersistableUriPermission(uri, takeFlags)
```

### 2b) Quiz Logic & Testing

**How does the quiz choose a new image and wrong choices?**

Our quiz logic is encapsulated in a `ViewModel` and uses a utility function to generate questions.
We filter the gallery to exclude the correct answer, shuffle, and take three distractors.

**Code Example (Our Solution):**

```kotlin
// In QuizUtil.kt
fun generateQuizEntry(correctEntry: GalleryEntry, gallery: List<GalleryEntry>): QuizEntry {
    val wrongNames = gallery.filter { it.name != correctEntry.name }
        .shuffled()
        .take(3)
        .map { it.name }
    val options = (wrongNames + correctEntry.name).shuffled()
    return QuizEntry(correctEntry.uri, options, correctEntry.name)
}
```

**Testing & Edge Cases:**

- **Empty Gallery:** Our `GalleryScreen` handles this with an explicit check:
  `if (currentEntries.isEmpty()) { EmptyGalleryText() }`.
- **Generalized Tests:** Our Espresso tests access the `ViewModel` state directly, making it very
  easy to generalize into a loop:

```kotlin
// In FullQuizRunTest.kt
for (i in 1..totalQuestions) {
    answerQuestion(shouldBeCorrect = i % 2 != 0, expectedScore = ..., questionNumber = i)
}
```

### 2c) ContentProvider Review

**Review of the ContentProvider Implementation.**

Our `QuizProvider` maps internal database columns to the required public names using SQL aliasing.

**Code Example (Our Solution):**

```kotlin
// In QuizProvider.kt
override fun query(...): Cursor? {
    if (uriMatcher.match(uri) == ITEMS) {
        val cursor = database.query("SELECT name, uri AS URI FROM quiz_items", null)
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }
    return null
}
```

---

## 3. Comparison Summary

| Feature             | Our Solution                                      | Their Solution |
|:--------------------|:--------------------------------------------------|:---------------|
| **Image Selection** | `OpenDocument()` contract with Compose launcher.  | [To be filled] |
| **Question Gen**    | Pure function `generateQuizEntry` with shuffling. | [To be filled] |
| **Persistence**     | Room Database with Flow for reactive updates.     | [To be filled] |
| **Testing**         | Logic-based assertions via ViewModel state.       | [To be filled] |

---

## 4. Conclusion & Observations

[Insert your overall thoughts here. Did you learn any new tricks? Any bugs spotted in their code?]
