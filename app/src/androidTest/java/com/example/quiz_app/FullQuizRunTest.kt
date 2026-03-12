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
import org.junit.Assert.assertEquals
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
     * This test assumes the default 4 seeded items are the only ones in the gallery.
     */
    @Test
    fun testFullQuizRun() {
        Log.d(TAG, "Starting test: testFullQuizRun")
        val viewModel = composeTestRule.activity.viewModel

        // The built-in questions are 4, so we do 2 correct and 2 incorrect
        answerQuestion(shouldBeCorrect = true, expectedScore = 1, questionNumber = 1)
        answerQuestion(shouldBeCorrect = false, expectedScore = 1, questionNumber = 2)
        answerQuestion(shouldBeCorrect = true, expectedScore = 2, questionNumber = 3)
        answerQuestion(shouldBeCorrect = false, expectedScore = 2, questionNumber = 4)

        // Verify state before finishing
        val finalQuestionState = viewModel.quizState.value as QuizUiState.Question
        assertEquals("Final score in ViewModel should be 2", 2, finalQuestionState.score)

        composeTestRule.onNodeWithText("Finish").performClick()
        Log.d(TAG, "Quiz finished, navigating to final score screen")

        // Verify Finished state in ViewModel
        val finishedState = viewModel.quizState.value as QuizUiState.Finished
        assertEquals("Finished state score should be 2", 2, finishedState.score)
        assertEquals("Finished state total should be 4", 4, finishedState.total)

        Log.d(TAG, "Verifying final score UI text")
        composeTestRule.onNodeWithText("Score: 2 / 4").assertExists()

        // Click the home button and verify we navigate back to MainActivity
        Log.d(TAG, "Clicking 'Home' button")
        composeTestRule.onNodeWithText("Home").performClick()

        Log.d(TAG, "Verifying intent for MainActivity")
        intended(hasComponent(MainActivity::class.java.name))
        Log.d(TAG, "Finished test: testFullQuizRun")
    }

    /**
     * Helper function to answer a single question and verify the score via ViewModel state.
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

        // Robust Assertion: Check ViewModel state
        val stateAfterClick = viewModel.quizState.value as QuizUiState.Question
        assertEquals("Score mismatch at question $questionNumber", expectedScore, stateAfterClick.score)
        assertEquals("Question number mismatch", questionNumber, stateAfterClick.questionNumber)

        if (questionNumber < 4) {
            composeTestRule.onNodeWithText("Next").performClick()
        }
    }
}
