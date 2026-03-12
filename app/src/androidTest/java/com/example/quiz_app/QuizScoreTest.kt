package com.example.quiz_app

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quiz_app.quiz.QuizActivity
import com.example.quiz_app.quiz.QuizUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
     * It asserts against the ViewModel's state rather than matching hardcoded UI text strings.
     */
    @Test
    fun testScoreUpdatesCorrectly() {
        Log.d(TAG, "Starting test: testScoreUpdatesCorrectly")
        val viewModel = composeTestRule.activity.viewModel

        // --- Correct Answer Test ---

        Log.d(TAG, "Getting correct answer for the first question")
        val firstState = viewModel.quizState.value as? QuizUiState.Question
        val correctAnswer = firstState?.entry?.correctName

        Log.d(TAG, "Clicking correct answer: $correctAnswer")
        correctAnswer?.let {
            composeTestRule.onNodeWithText(it).performClick()
        }

        // Assert against internal values (Requirement 4)
        val stateAfterFirst = viewModel.quizState.value as QuizUiState.Question
        Log.d(TAG, "Verifying ViewModel values: score=${stateAfterFirst.score}, questionNumber=${stateAfterFirst.questionNumber}")
        
        assertEquals("Score should be 1 after correct answer", 1, stateAfterFirst.score)
        assertEquals("Question number should be 1", 1, stateAfterFirst.questionNumber)
        assertTrue("isAnswered should be true", stateAfterFirst.isAnswered)

        Log.d(TAG, "Clicking 'Next' button")
        composeTestRule.onNodeWithText("Next").performClick()

        // --- Incorrect Answer Test ---

        Log.d(TAG, "Getting incorrect answer for the second question")
        val secondState = viewModel.quizState.value as QuizUiState.Question
        val secondCorrectAnswer = secondState.entry.correctName
        val incorrectAnswer = secondState.entry.options.find { it != secondCorrectAnswer }

        Log.d(TAG, "Clicking incorrect answer: $incorrectAnswer")
        incorrectAnswer?.let {
            composeTestRule.onNodeWithText(it).performClick()
        }

        // Assert against internal values
        val stateAfterSecond = viewModel.quizState.value as QuizUiState.Question
        Log.d(TAG, "Verifying ViewModel values: score=${stateAfterSecond.score}, questionNumber=${stateAfterSecond.questionNumber}")
        
        assertEquals("Score should remain 1 after incorrect answer", 1, stateAfterSecond.score)
        assertEquals("Question number should be 2", 2, stateAfterSecond.questionNumber)
        assertTrue("isAnswered should be true", stateAfterSecond.isAnswered)

        Log.d(TAG, "Finished test: testScoreUpdatesCorrectly")
    }
}
