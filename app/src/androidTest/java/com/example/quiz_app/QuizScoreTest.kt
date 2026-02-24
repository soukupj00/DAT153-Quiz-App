package com.example.quiz_app

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quiz_app.quiz.QuizActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuizScoreTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<QuizActivity>()

    companion object {
        private const val TAG = "QuizScoreTest"
    }

    /**
     * This test verifies that the score is updated correctly after answering a question.
     * It answers one question correctly and one incorrectly, checking the score each time.
     */
    @Test
    fun testScoreUpdatesCorrectly() {
        Log.d(TAG, "Starting test: testScoreUpdatesCorrectly")
        // Correct Answer Test

        Log.d(TAG, "Getting correct answer for the first question")
        val viewModel = composeTestRule.activity.viewModel
        val firstQuestion =
            (viewModel.quizState.value as? com.example.quiz_app.quiz.QuizUiState.Question)?.entry
        val correctAnswer = firstQuestion?.correctName

        Log.d(TAG, "Clicking correct answer: $correctAnswer")
        correctAnswer?.let {
            composeTestRule.onNodeWithText(it).performClick()
        }

        Log.d(TAG, "Verifying score is 1 / 1")
        composeTestRule.onNodeWithText("Score: 1 / 1").assertExists()

        Log.d(TAG, "Clicking 'Next' button")
        composeTestRule.onNodeWithText("Next").performClick()

        // Incorrect Answer Test

        // Get the options for the second question
        Log.d(TAG, "Getting incorrect answer for the second question")
        val secondQuestion =
            (viewModel.quizState.value as? com.example.quiz_app.quiz.QuizUiState.Question)?.entry
        val secondCorrectAnswer = secondQuestion?.correctName
        val incorrectAnswer = secondQuestion?.options?.find { it != secondCorrectAnswer }

        Log.d(TAG, "Clicking incorrect answer: $incorrectAnswer")
        incorrectAnswer?.let {
            composeTestRule.onNodeWithText(it).performClick()
        }

        Log.d(TAG, "Verifying score is 1 / 2")
        composeTestRule.onNodeWithText("Score: 1 / 2").assertExists()
        Log.d(TAG, "Finished test: testScoreUpdatesCorrectly")
    }
}
