# DAT153 - Android App Development: Task 1

**Link to actual requirements:** [https://hvl.instructure.com/courses/31988/assignments/98499](https://hvl.instructure.com/courses/31988/assignments/98499)

This is the first oblig, deadline Monday, 9th February. Please submit your solution as a link to a public Git repository (e.g., on Codeberg, Gitlab, BitBucket, GitHub) in groups of 2 or 3 students. We will review the app and your code with your group in one of the lab sessions after the deadline, and you will have the chance to revise your app once if anything doesn't work. Please do ask for help on time if you get stuck somewhere! This assignment will be marked as passed when you've passed the review in the lab between the 9th and 16th.

-Volker

## The Quiz App

This little app should implement an easy game where you have to match names and photos (or a random selection of cute animal pictures). There are two core activities, which the user should be able to choose from when the application starts:

- **The "gallery"**: It shows all names & pictures, if necessary, letting the user scroll through the list. There should be buttons for:
    - Adding a new entry.
    - Removing an existing entry (e.g., by clicking an image).
    - Sorting all existing entries in alphabetical or reverse order (A-Z or Z-A).
- **The "quiz"**: When users click on this activity, the app will randomly select a photo from the gallery and show it on the screen. The app should present the right name for the photo and two wrong names in random order. The user has to select the one they think is correct. After submission, there should be an indication by the app if the name was correct or not. If not, the app should show the correct name. After that, the whole process repeats until the user decides to leave this activity. The app should keep track of the score (number of correct answers vs. all attempts) and show it on the screen during the quiz.
- **"Add entry"-functionality**: Here the user can add a new entry (a pair of a photo and the associated name). Please allow the user to choose an existing photo from their phone or enable the user to take a photo using their camera (using the camera is an optional feature, but you must at least let the user choose from the media gallery). The name/photo pair should then be available to the "gallery" and the "quiz".

## Other Remarks

- You must design the **main menu** using an XML-based layout. Use Compose for all the other UIs.
- Don't immediately try to use one of the fancy databases such as SQLite or Rooms! Use a simple data structure from the `Collections` interface to maintain photos & names. Use the `Application` class to share this data structure throughout the app.
- Add **three photos** (at least) and names to the app through the resource folder and use it to initialize your database when the app starts. That is, load the image data and put it into your data structure. (Make sure that the images are not too large, because they will also be in Git -- you can also use a cat-pic instead of your real face.)
- Do not worry about persistently storing new entries (or the score) on the phone. We will add this functionality in the next oblig. For now, it is okay if your app "forgets" everything except for the built-in photos when we restart the app.
- Make sure navigating back from an activity works correctly (common mistakes: internal data structure not updating correctly when adding/removing, gallery not updating after adding/deleting, memory leak when dealing with image files).
- Document your code!
- Please use Git "properly," that is, only store the Android Studio project, not generated files like JARs and class-files.
- In future obligs, we will work on storing the data (with new entries) on the phone, writing tests, and integration with other services on the phone.

## Some Useful Links

- Use e.g the `ACTION_GET_CONTENT`/`ACTION_OPEN_DOCUMENT`-intent to let the user choose an existing image:
    - [Document Provider](https://developer.android.com/guide/topics/providers/document-provider)
    - [Bitmap](https://developer.android.com/training/data-storage/shared/documents-files#bitmap)
- Sharing state throughout an application (one of many possible ways):
    - [Application Class](https://developer.android.com/reference/kotlin/android/app/Application)
- An alternative in Kotlin to RecyclerView:
    - [Lazy list](https://developer.android.com/jetpack/compose/lists)
