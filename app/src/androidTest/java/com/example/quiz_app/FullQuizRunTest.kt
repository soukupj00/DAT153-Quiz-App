package com.example.quiz_app

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quiz_app.quiz.QuizActivity
import com.example.quiz_app.quiz.QuizUiState
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullQuizRunTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<QuizActivity>()

    companion object {
        private const val TAG = "FullQuizRunTest"
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
     * Simulates a full run through the quiz, with mixed answers,
     * and verifies the final score and navigation back to the home screen.
     */
    @Test
    fun testFullQuizRun() {
        Log.d(TAG, "Starting test: testFullQuizRun")

        // The built-in questions are 4, so we do 2 correct and 2 incorrect
        answerQuestion(shouldBeCorrect = true, expectedScore = 1, questionNumber = 1)
        answerQuestion(shouldBeCorrect = false, expectedScore = 1, questionNumber = 2)
        answerQuestion(shouldBeCorrect = true, expectedScore = 2, questionNumber = 3)
        answerQuestion(shouldBeCorrect = false, expectedScore = 2, questionNumber = 4)

        composeTestRule.onNodeWithText("Finish").performClick()
        Log.d(TAG, "Quiz finished, navigating to final score screen")

        Log.d(TAG, "Verifying final score is 2 / 4")
        composeTestRule.onNodeWithText("Score: 2 / 4").assertExists()

        // Click the home button and verify we navigate back to MainActivity
        Log.d(TAG, "Clicking 'Home' button")
        composeTestRule.onNodeWithText("Home").performClick()

        Log.d(TAG, "Verifying intent for MainActivity")
        intended(hasComponent(MainActivity::class.java.name))
        Log.d(TAG, "Finished test: testFullQuizRun")
    }

    /**
     * Helper function to answer a single question and verify the score.
     */
    private fun answerQuestion(shouldBeCorrect: Boolean, expectedScore: Int, questionNumber: Int) {
        val viewModel = composeTestRule.activity.viewModel
        val uiState = viewModel.quizState.value as? QuizUiState.Question
        val question = uiState?.entry
        val correctAnswer = question?.correctName
        val answerToClick = if (shouldBeCorrect) {
            correctAnswer
        } else {
            question?.options?.find { it != correctAnswer }
        }

        Log.d(
            TAG,
            "Answering question $questionNumber. Correct: $shouldBeCorrect. Clicking: $answerToClick"
        )
        answerToClick?.let {
            composeTestRule.onNodeWithText(it).performClick()
        }

        val expectedScoreText = "Score: $expectedScore / $questionNumber"
        Log.d(TAG, "Verifying score is '$expectedScoreText'")
        composeTestRule.onNodeWithText(expectedScoreText).assertExists()

        if (questionNumber < 4) {
            composeTestRule.onNodeWithText("Next").performClick()
        }
    }
}
