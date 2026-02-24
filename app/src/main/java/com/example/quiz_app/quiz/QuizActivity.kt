package com.example.quiz_app.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.quiz_app.R
import com.example.quiz_app.ui.StandardLayout

class QuizActivity : ComponentActivity() {
    val viewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizGame(viewModel)
        }
    }
}

@Composable
fun QuizGame(viewModel: QuizViewModel) {
    val uiState by viewModel.quizState.collectAsState()

    StandardLayout(title = stringResource(id = R.string.quiz)) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is QuizUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is QuizUiState.Question -> {
                    QuizScreen(
                        quizEntry = state.entry,
                        score = state.score,
                        questionNumber = state.questionNumber,
                        totalQuestions = state.totalQuestions,
                        isAnswered = state.isAnswered,
                        selectedOption = state.selectedOption,
                        onAnswerSelected = { viewModel.submitAnswer(it) },
                        onNextClicked = { viewModel.nextQuestion() }
                    )
                }

                is QuizUiState.Finished -> {
                    FinalScoreScreen(
                        score = state.score,
                        total = state.total,
                        onTryAgain = { viewModel.restartQuiz() }
                    )
                }
            }
        }
    }
}
