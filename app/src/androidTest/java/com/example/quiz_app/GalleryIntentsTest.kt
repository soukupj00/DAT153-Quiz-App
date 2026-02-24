package com.example.quiz_app

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quiz_app.gallery.GalleryActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GalleryIntentsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<GalleryActivity>()

    companion object {
        private const val TAG = "GalleryIntentsTest"
    }

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    /**
     * This test verifies adding and deleting an entry in the gallery.
     * It uses Intent Stubbing to simulate picking an image from the device.
     */
    @Test
    fun testAddAndDeleteNewItem() {
        Log.d(TAG, "Starting test: testAddAndDeleteNewItem")
        val newImageName = "New Test Entry"

        // Prepare a dummy image URI from resources
        val imageUri =
            Uri.parse("android.resource://${composeTestRule.activity.packageName}/${R.drawable.jupiter}")

        // Create a stub result for the image picker intent
        val resultData = Intent().apply { data = imageUri }
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // Stub the ACTION_OPEN_DOCUMENT intent to return our result without opening the system UI
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(result)
        Log.d(TAG, "Intent stubbed for ACTION_OPEN_DOCUMENT")

        // First we add the image
        Log.d(TAG, "Clicking 'Add Image' button")
        composeTestRule.onNodeWithContentDescription("Add Image").performClick()

        Log.d(TAG, "Entering name: $newImageName")
        composeTestRule.onNodeWithText("Image Name").performTextInput(newImageName)
        composeTestRule.onNodeWithText("Add").performClick()

        Log.d(TAG, "Verifying item '$newImageName' exists")
        composeTestRule.onNodeWithText(newImageName).assertExists()

        // Now we delete the image
        Log.d(TAG, "Long-pressing item '$newImageName' to delete")
        composeTestRule.onNodeWithText(newImageName).performTouchInput { longClick() }

        Log.d(TAG, "Confirming deletion")
        composeTestRule.onNodeWithText("Delete").performClick()

        Log.d(TAG, "Verifying item '$newImageName' is deleted")
        composeTestRule.onNodeWithText(newImageName).assertDoesNotExist()
        Log.d(TAG, "Finished test: testAddAndDeleteNewItem")
    }
}
