# DAT153 - Android App Development: Task 2

**Link to actual requirements:** [https://hvl.instructure.com/courses/31988/assignments/99420](https://hvl.instructure.com/courses/31988/assignments/99420)

We are going to make some improvements to our application! **Deadline: Thursday, 12th March.** Again, submit a link to your Git repository as a group here in Canvas. You can start a new project from scratch if you like; it does not have to build on your Oblig 1. Note that it is okay if you decide to throw away your old version and quickly re-do your app in a different style -- I would even recommend it! For several of the problems you encountered in the first coding exercise, you should now know how to solve them better.

## Requirements

1.  **Save your data** (and load it when the app starts again later)! Decide on how you want to store the data (names & pictures) that we add from the app. We'll use **Android Room DAOs**. Encapsulate the data necessary for the quiz (i.e., all images, the current image and correct answer, and alternative wrong choices for the current question) in a subclass of a `ViewModel`. Make sure that when rotating your phone during a quiz:
    - The current question is not lost.
    - The score is not lost.
    
    Don't forget to load the three or more built-in questions at the right stage in your code. You can simplify your code by addressing both images on the phone and those in the resource folder through URIs. I do not recommend trying to store the images as bitmaps!

2.  **Publish a `ContentProvider`** in your application manifest that publishes the entries in your app (pairs of name/image URI). Use the following column names: `"name"`, `"URI"`. Show in your `README` in the repository how you tested your `ContentProvider` with `adb`.

3.  **Write test-cases** using the **Espresso framework** for your app. At least have the following test cases:
    - Clicking a button in the main menu takes you to the right sub-activity (i.e., to the Quiz or the Gallery; testing one button is enough).
    - Is the score updated correctly in the quiz (the test submits at least one right/wrong answer each and you check if the score is correct afterwards).
    - A test that checks that the number of registered pictures/persons is correct after adding/deleting an entry. For adding, use **Intent Stubbing** to return some image data (e.g., from the resource folder) without any user interaction.

4.  **Write your Espresso test classes** so that they directly address each activity under test. In other words, if your code has multiple activities, don't write all tests for the main activity and then have your test case click the main menu buttons to reach an activity. Note that you may have to change the internal structure of your app quite a bit so that your test cases actually have access to the internal state of your app (e.g., the current score and where the right answer is; or starting the Quiz directly without going through the main menu) from the unit test.

5.  **Document your test cases** with results in the `README` in your repo (HTML or Markdown). That means there should be:
    - A detailed description in natural language of what the test should... test (use the same "language" we would use to describe e.g., a use case), the expected result, and which class/method implements the test.
    - Whether the test passed or failed. If you haven't been able to get a test to pass, describe your explanation of what is going wrong and what you would have to do to fix it.

    Note that the main goal is to have proper test cases, so it is okay if in the end you have some test cases that still fail, where you haven't been able to make them "green." In this case, a failing test case can still serve as documentation.

## Time Estimates (Guideline)

- **LiveData/DAO** --- **3 hours**:
    - 1 hour thinking and designing the data structures.
    - 1 hour connecting the new data structures to the UI.
    - 1 hour troubleshooting (e.g., rotation).
- **ContentProvider** --- **1 hour**:
    - 30 minutes developing the provider after you have the previous task (Room) finished.
    - 30 minutes testing, troubleshooting, and documenting the test in the `README`.
- **Writing tests** --- **4 hours**:
    - 30 minutes designing and writing test-description.
    - 2 hours implementing the tests and adapting the internal structure where necessary.
    - 90 minutes troubleshooting.

Use the lab hours to get input on your solution from Ulises, Jobjørn, and me. If you get stuck on a sub-task, switch to another for a while; ask for help.

Like for Oblig 1, all group members will have to present their solution to Ulises or Volker in the lab for final approval. Please contact us beforehand if one or more group members will not be able to present.

Lykke til!
