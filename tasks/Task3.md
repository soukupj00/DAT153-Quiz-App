# DAT153 - Android App Development: Task 3 (Peer Review)

In this oblig, you will compare another group’s solution to Oblig 2 to yours and submit a PDF with
your comments (both to the group which you are reviewing, and here in Canvas to Volker & Ulises).

In the table below, you will find the repository URLs that each group submitted. You will be
reviewing the repository shown in the right column in the row with your group number in the first
column.

If you/your group are missing in the table (in either column), please contact Volker!

## Instructions

0. **Identify the Target**: Make sure to include the URL (and if necessary the git hash of the
   commit for the version that you review) of the repository that you are reviewing at the top of
   your document.

1. **Initial Checklist**: Please check immediately the following things for the repo that you will
   review:
    - You have access to the repository.
    - You can clone & build the Android application without errors.
    - The application launches correctly on your phone/emulator.

   *If any of this fails, please **FILE AN ISSUE** on the corresponding git-repo when possible (
   e.g., GitHub, Codeberg, etc.) and also tag the instructors.*

   > **Note:** The other group’s repository may contain commits since they submitted the oblig
   and/or may use git-branches. In that case, it might make sense to use Android Studio’s Git tool
   window to browse the history and pick the commit that seems to correspond to the submission
   deadline.

2. **Comparative Analysis**: Please answer the following questions in about 2-3 paragraphs each.
   First describe in your own words the solution the other group chose, and then compare it with
   your own solution — what’s the difference?
    - Did you learn a new trick?
    - Does one solution have an advantage over the other?
    - Mention any issues in the code that you may have spotted.

   *You can copy-paste code fragments or take screenshots if this helps the explanation.*

### Questions to Address

- **2a) Gallery Interaction**: How does the code make the user select an image in the
  `GalleryActivity`?
    - Which `Intent` does it use?
    - Are there any additional parameters?
    - How is the return value from the intent handled?

- **2b) Quiz Logic & Testing**: Describe how the quiz chooses a new image for the next question and
  the two wrong choices to label the buttons.
    - How is the check if the answer was correct implemented?
    - Will the quiz work correctly for an empty gallery or a gallery with only two pictures?
    - Is the test case easy to generalize into a loop that answers a random number of questions,
      deciding for each round randomly if the correct or an incorrect answer should be given?

- **2c) ContentProvider Review**: Review the `ContentProvider`.
    - Do the returned URIs correspond to the URI of the content provider, or does it return URIs
      that do not begin with the authority of the provider?
    - Does the provider implement the mandatory columns? (
      See [OpenableColumns](https://developer.android.com/reference/android/provider/OpenableColumns))
    - Test the content provider from the command line with `adb` — make separate tests with
      `--projection` and `--where` arguments. If it fails, explain why.

## Comparison Format

Consider presenting your findings in a table:

| Our Solution | Their Solution |
|:-------------|:---------------|
| ...          | ...            |

*Your explanations should use the correct Android terminology and contain sufficient detail.*

> **Example of Good Detail:**
> “The group uses the `ActivityResultContract` for `OpenDocument()` which returns a URI... In
> addition to the URI, it is important to also request the permission to the file. They create a new
`QuizItem` object with the URI in...”

## Submission

3. Submit your answers as a **PDF document** attachment in a GitHub issue on the project that you
   have reviewed.
4. **ALSO** submit your answers as a single PDF document in **Canvas**. Include the link to the
   GitHub issue at the top of the document.
