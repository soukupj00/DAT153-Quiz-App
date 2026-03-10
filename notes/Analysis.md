# Code Review: Quiz App

Let's start with the first screen the user sees: `MainActivity`.

## Part 1: `MainActivity.kt` - The Entrance to Your App

This file represents the main screen of your application. It's built using the traditional Android
View system, where the user interface is defined in an XML file (`activity_main.xml`).

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val galleryButton: Button = findViewById(R.id.gallery_button)
        galleryButton.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        val quizButton: Button = findViewById(R.id.quiz_button)
        quizButton.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }
    }
}
```

### Key Concepts Explained

* **`class MainActivity : AppCompatActivity()`**: Inherits from `AppCompatActivity` for backward
  compatibility.
* **`override fun onCreate(savedInstanceState: Bundle?)`**: The entry point method for the activity.
* **`setContentView(R.layout.activity_main)`**: Connects your Kotlin code to your XML layout file.
* **`findViewById(...)`**: Retrieves a reference to a UI element from XML.
* **`setOnClickListener { ... }`**: Attaches a click listener to a view.
* **`startActivity(Intent(...))`**: Navigates to another activity.

## Part 2: `GalleryActivity.kt` - A New Era with Jetpack Compose

This screen is built with Jetpack Compose, a modern, declarative UI toolkit. We describe *what* the
UI should look like, and Compose handles the updates.

### Key Concepts Explained

* **`setContent { ... }`**: The entry point for a Compose UI within an activity.
* **`@Composable` Functions**: Special functions that describe a piece of UI.
* **State and Recomposition**: The core of Compose. We use `remember { mutableStateOf(...) }` to
  create observable state that, when changed, causes the UI to update.
* **`var ... by remember { ... }`**: A Kotlin "delegated property" that simplifies state usage.
* **`LazyVerticalGrid`**: For efficiently displaying large, scrollable grids of items.
* **`rememberLauncherForActivityResult`**: The modern, composable-friendly way to start an activity
  for a result.
* **`pointerInput` and `detectTapGestures`**: A powerful way to handle complex touch events.

## Part 3: `QuizActivity.kt` - The Brains of the Operation

The `QuizActivity` is a masterclass in managing complex state and logic in a declarative UI.

### `QuizGame()` - The Stateful Controller

This top-level composable is **stateful**; it owns and manages the data that can change over time (
the current score, the current question, etc.).

#### `remember` with Keys: Smart Shuffling

`val galleryForQuiz = remember(allEntries, quizRestartTrigger) { allEntries.shuffled() }`

This is a brilliant use of `remember` with keys. The shuffling logic only re-runs if `allEntries` or
`quizRestartTrigger` changes, making it very efficient.

#### `LaunchedEffect` - Handling Side Effects

`LaunchedEffect(remainingEntries) { ... }`

This is the correct way to handle logic that isn't directly describing the UI, like preparing the
next quiz question. It runs whenever its key (`remainingEntries`) changes.

### `QuizScreen()` and `FinalScoreScreen()` - The Stateless Dummies

These composables are **stateless**. They receive all the data they need as parameters and don't
manage any state themselves. When a user clicks a button, they simply call a function that was
passed to them (e.g., `onAnswerSelected`). This pattern is called **State Hoisting** and it makes
your components reusable, testable, and simpler to reason about.

---

## Part 4: Data Structures - The `types` Package

Good software is built on good data structures. In your `types` package, you've defined the "nouns"
of your application. These are simple, clear, and serve as the single source of truth for what your
data looks like.

### `GalleryEntry.kt` - The Blueprint for an Image

```kotlin
data class GalleryEntry(
    val name: String,
    val uri: Uri,
    val isDrawable: Boolean = false,
    @DrawableRes val drawableId: Int = 0
)
```

This is a Kotlin `data class`. Data classes are designed specifically for holding data. The compiler
automatically generates useful methods like `equals()`, `hashCode()`, `toString()`, and `copy()` for
you.

* **What is it?** This class represents a single image in your gallery. It can be one of two things:
    1. An image that comes with the app (from a drawable resource).
    2. An image the user added from their device's storage.
* **`isDrawable` Flag**: This boolean is a simple and effective way to distinguish between the two
  types of images. When `isDrawable` is `true`, you know to use `drawableId` to load the image. When
  it's `false`, you use the `uri`.
* **Immutability**: The properties are declared with `val`, making them read-only. This is a good
  practice. It means that once a `GalleryEntry` is created, it cannot be changed. This prevents
  accidental modifications and makes your state more predictable.

### `QuizEntry.kt` - A Single Quiz Question

```kotlin
data class QuizEntry(
    val image: GalleryEntry,
    val options: List<String>,
    val correctName: String
)
```

* **What is it?** This data class perfectly represents a single question in your quiz. It holds the
  image to be displayed, a list of possible answers, and the correct answer.
* **Composition over Inheritance**: Notice that `QuizEntry` *contains* a `GalleryEntry`. This is a
  powerful concept called composition. A quiz question *has an* image; it doesn't need to be a
  special type of image. This keeps your data structures decoupled and easy to understand.

## Part 5: The Data Layer - `GalleryData.kt`

If the `types` are the blueprints, `GalleryData.kt` is the factory and the warehouse. It's
responsible for creating and storing all the `GalleryEntry` objects.

```kotlin
object GalleryData {
    private val _entries = mutableStateListOf<GalleryEntry>()
    val entries: List<GalleryEntry> = _entries

    fun addEntry(entry: GalleryEntry) {
        ...
    }
    fun removeEntry(entry: GalleryEntry) {
        ...
    }
}
```

### The `object` Singleton Pattern

As we discussed, `object` creates a singleton—a single, globally accessible instance. This is
perfect for managing a central repository of data like your image gallery. Anywhere in your app, you
can simply access `GalleryData.entries` to get the current list of images.

### `mutableStateListOf` - The Key to Reactivity

This is the magic that makes your UI update automatically when an image is added or removed.
`mutableStateListOf` creates a list that is observable by Jetpack Compose. When you call `addEntry`
or `removeEntry`, which modify this list, Compose detects the change and automatically triggers a
recomposition for any composable that is reading from `GalleryData.entries`. This is why your
gallery screen updates in real-time!

### The Add/Delete Flow

1. **Add**: The user clicks the FAB in `GalleryActivity`, which launches the system's image picker
   using `rememberLauncherForActivityResult`.
2. The user selects an image, and the result (a `Uri`) is returned.
3. The `AddNameDialog` is shown to get a name for the image.
4. `GalleryData.addEntry()` is called with a new `GalleryEntry` containing the name and `Uri`.
5. `_entries.add()` is called, which notifies Compose to redraw the `LazyVerticalGrid` with the new
   item.

6. **Delete**: The user long-presses an image in `GalleryActivity`.
7. A state variable (`showDeleteDialog`) is set to `true`.
8. The `DeleteConfirmationDialog` is shown.
9. If the user confirms, `GalleryData.removeEntry()` is called.
10. `_entries.remove()` is called, and Compose redraws the grid without the deleted item.

## Part 6: Reusable UI - The `ui` Package

A major advantage of Compose is the ability to easily create your own reusable UI components. Your
`ui` package is a great example of this.

### `StandardLayout.kt`

This composable likely provides a consistent frame for your screens, probably including a `Scaffold`
with a `TopAppBar`. This is excellent practice. It ensures that all your screens have a similar look
and feel (e.g., the same title bar style) without duplicating code. You define the wrapper once and
reuse it everywhere.

### `AnimatedGlowingButton.kt`

This is a fantastic example of going the extra mile to create a polished user experience. By
creating a custom button with a unique animation, you give your app a distinct personality. Because
it's a self-contained composable, you can now drop this `AnimatedGlowingButton` anywhere you want
with zero effort, and it will just work.

### `CustomProgressBar.kt`

This shows the power of composition in UI. You likely took a standard `ProgressBar` and combined it
with a `Text` composable inside a `Box` to create a new component that perfectly fits your needs.
This is something that was much harder to do with the old XML view system but is natural and simple
in Compose.

---

## Part 7: Project Setup & Advanced Topics

Now that we've reviewed the individual screens and data structures, let's look at the "scaffolding"
of your project. This includes the build system that compiles your app and the architectural
patterns that make your Compose code so clean and effective.

### Gradle and Dependencies - The Build System

Every Android project is powered by Gradle. It's a build tool that takes all your source code,
resources, and libraries, and packages them into the APK (Android Package) that runs on a device.
The main configuration file for this process is `build.gradle.kts` located in your `app` module.

The most important section for a developer is `dependencies`. This is where you declare which
external libraries your app needs.

```kotlin
// Inside app/build.gradle.kts

dependencies {
    // Core Android libraries
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Jetpack Compose Libraries
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ... other dependencies
}
```

* **What is this?** This block tells Gradle to download and include specific versions of libraries
  from online repositories (like Maven Central).
* **`compose-bom` (Bill of Materials)**: This is a special dependency. Instead of adding a library,
  it manages the versions of all your other Compose libraries. This ensures that all your Compose
  dependencies are compatible with each other, which is a huge help in preventing versioning
  conflicts.
* **`activity-compose`**: This library provides the `setContent { ... }` extension function, which
  is the bridge between a traditional `Activity` and your Jetpack Compose UI.

### A Deeper Look at Compose UI Patterns

You've used several powerful Compose patterns correctly. Let's highlight them with more specific
examples.

#### State Hoisting in Practice (`QuizActivity`)

As we discussed, **State Hoisting** is the key to a well-structured Compose app. The `QuizGame`
composable is stateful (it owns the state), while `QuizScreen` is stateless (it just displays the
state).

**Stateful "Controller" Composable (`QuizGame`)**

This composable creates and manages the state. It then passes the state *down* to the stateless
composable and event handlers *up* from it.

```kotlin
@Composable
fun QuizGame() {
    // ... (state declarations)
    var isAnswered by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    // ... (logic)

    QuizScreen(
        // ... (other state is passed down)
        isAnswered = isAnswered,
        selectedOption = selectedOption,
        onAnswerSelected = { option ->
            // Logic is handled here, in the stateful parent
            if (!isAnswered) {
                selectedOption = option
                // ... (update score, etc.)
                isAnswered = true
            }
        },
        onNextClicked = {
            // Logic is handled here
            isAnswered = false
            selectedOption = null
            remainingEntries = remainingEntries.drop(1)
        }
    )
}
```

**Stateless "Display" Composable (`QuizScreen`)**

This composable is simple. It receives state and displays it. It doesn't know *how* to handle a
click; it just knows to call the `onAnswerSelected` function when a button is clicked.

```kotlin
@Composable
fun QuizScreen(
    // ... (receives state)
    isAnswered: Boolean,
    selectedOption: String?,
    onAnswerSelected: (String) -> Unit, // Receives event handler
    onNextClicked: () -> Unit           // Receives event handler
) {
    // ...
    LazyVerticalGrid(...) {
        items(quizEntry.options) { option ->
            Button(
                onClick = { onAnswerSelected(option) }, // Calls the event handler
                // ...
            ) {
                Text(text = option)
            }
        }
    }
    //...
}
```

This pattern makes your code incredibly easy to test and reuse.

#### Reusable Components (`StandardLayout`)

You've created a `StandardLayout` composable to ensure a consistent look across screens. This is a
fundamental concept in building scalable UIs.

```kotlin
@Composable
fun StandardLayout(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                actions = actions,
                // ... colors and styling
            )
        }
    ) { innerPadding ->
        // The screen's content is passed in here
        content(innerPadding)
    }
}
```

* **`Scaffold`**: This is a core Material Design component that provides a standard layout
  structure (app bars, floating action buttons, etc.).
* **Slot APIs**: Notice how `actions` and `content` are not just values; they are themselves
  `@Composable` functions. This is called a "slot API." You are leaving "slots" in your layout that
  other composables can fill. This makes `StandardLayout` incredibly flexible. The `GalleryActivity`
  can slot in its sort button, while the `QuizActivity` can slot in nothing, all while using the
  same layout.
