package com.example.quiz_app

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quiz_app.gallery.GalleryActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    companion object {
        private const val TAG = "NavigationTest"
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
     * Test for navigation from MainActivity to GalleryActivity.
     * Verified using Espresso Intents to ensure the correct activity is launched.
     */
    @Test
    fun testNavigateToGallery() {
        Log.d(TAG, "Starting test: testNavigateToGallery")

        Log.d(TAG, "Checking if gallery button is displayed")
        onView(withId(R.id.gallery_button)).check(matches(isDisplayed()))

        Log.d(TAG, "Clicking gallery button")
        onView(withId(R.id.gallery_button)).perform(click())

        Log.d(TAG, "Verifying start intent for GalleryActivity")
        intended(hasComponent(GalleryActivity::class.java.name))

        Log.d(TAG, "Finished test: testNavigateToGallery")
    }
}
