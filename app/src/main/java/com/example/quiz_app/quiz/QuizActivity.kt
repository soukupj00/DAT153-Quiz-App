package com.example.quiz_app.quiz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.quiz_app.R
import com.example.quiz_app.data.GalleryData
import com.example.quiz_app.gallery.GalleryActivity
import com.example.quiz_app.types.GalleryEntry
import com.example.quiz_app.types.QuizEntry
import com.example.quiz_app.ui.AnimatedGlowingButton
import com.example.quiz_app.ui.CustomProgressBar
import com.example.quiz_app.ui.StandardLayout
import com.example.quiz_app.ui.theme.CorrectAnswer
import com.example.quiz_app.ui.theme.IncorrectAnswer

class QuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizGame()
        }
    }
}

@Composable
fun QuizGame() {
    LocalContext.current
    // Read from the shared data source
    val allEntries = GalleryData.entries
    var quizRestartTrigger by remember { mutableIntStateOf(0) }

    // Re-shuffle the quiz if the gallery data or the restart trigger changes
    val galleryForQuiz = remember(allEntries, quizRestartTrigger) { allEntries.shuffled() }

    var remainingEntries by remember(galleryForQuiz) { mutableStateOf(galleryForQuiz) }
    var currentQuizEntry by remember { mutableStateOf<QuizEntry?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var attempts by remember { mutableIntStateOf(0) }
    var isAnswered by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    // This effect will run when remainingEntries changes, setting up the next question
    LaunchedEffect(remainingEntries) {
        if (remainingEntries.isNotEmpty()) {
            val nextEntry = remainingEntries.first()
            currentQuizEntry = generateQuizEntry(nextEntry, allEntries)
        }
    }

    StandardLayout(title = stringResource(id = R.string.quiz)) {
        if (remainingEntries.isEmpty()) {
            // Show final score screen when all questions are answered.
            FinalScoreScreen(
                score = score,
                total = attempts,
                onTryAgain = {
                    // Reset all the states to restart the quiz
                    score = 0
                    attempts = 0
                    isAnswered = false
                    selectedOption = null
                    quizRestartTrigger++ // Trigger recomposition to get a new shuffled list
                }
            )
        } else {
            // Show the quiz screen if there are questions left
            currentQuizEntry?.let { entry ->
                val totalQuestions = galleryForQuiz.size
                val questionNumber = totalQuestions - remainingEntries.size + 1
                QuizScreen(
                    quizEntry = entry,
                    score = score,
                    questionNumber = questionNumber,
                    totalQuestions = totalQuestions,
                    isAnswered = isAnswered,
                    selectedOption = selectedOption,
                    onAnswerSelected = { option ->
                        if (!isAnswered) {
                            selectedOption = option
                            attempts++
                            if (option == entry.correctName) {
                                score++
                            }
                            isAnswered = true
                        }
                    },
                    onNextClicked = {
                        isAnswered = false
                        selectedOption = null
                        // Move to the next question by dropping the current one
                        remainingEntries = remainingEntries.drop(1)
                    }
                )
            }
        }
    }
}

@Composable
fun QuizScreen(
    quizEntry: QuizEntry,
    score: Int,
    questionNumber: Int,
    totalQuestions: Int,
    isAnswered: Boolean,
    selectedOption: String?,
    onAnswerSelected: (String) -> Unit,
    onNextClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.score_format, score, questionNumber),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Custom progress bar with text overlay
        CustomProgressBar(
            progress = if (totalQuestions > 0) questionNumber.toFloat() / totalQuestions.toFloat() else 0f,
            text = stringResource(id = R.string.question_format, questionNumber, totalQuestions),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Image with larger size
        Image(
            painter = painterResource(id = quizEntry.image),
            contentDescription = stringResource(id = R.string.quiz_image_content_description),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Makes the image square
                .padding(horizontal = 16.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 2x2 Grid of answer buttons
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quizEntry.options) { option ->
                Button(
                    onClick = { onAnswerSelected(option) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.primary // Default color
                            option == quizEntry.correctName -> CorrectAnswer
                            option == selectedOption && option != quizEntry.correctName -> IncorrectAnswer
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) // Faded for other options
                        }
                    )
                ) {
                    Text(text = option)
                }
            }
        }

        // Reserve space for the "Next" button to prevent UI jerk
        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isAnswered) {
                AnimatedGlowingButton(
                    onClick = onNextClicked,
                    text = if (questionNumber < totalQuestions) stringResource(id = R.string.next) else stringResource(
                        id = R.string.finish
                    )
                )
            }
        }
    }
}

@Composable
fun FinalScoreScreen(score: Int, total: Int, onTryAgain: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.final_score),
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )
        Text(
            text = stringResource(id = R.string.score_format, score, total),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedGlowingButton(
            onClick = onTryAgain,
            text = stringResource(id = R.string.try_again)
        )

        Spacer(modifier = Modifier.height(48.dp)) // Added more spacing

        AnimatedGlowingButton(
            onClick = { (context as? Activity)?.finish() },
            text = stringResource(id = R.string.home)
        )
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedGlowingButton(
            onClick = { context.startActivity(Intent(context, GalleryActivity::class.java)) },
            text = stringResource(id = R.string.gallery)
        )
    }
}

// Updated to generate 4 options (1 correct, 3 incorrect)
private fun generateQuizEntry(correctEntry: GalleryEntry, gallery: List<GalleryEntry>): QuizEntry {
    val wrongNames =
        gallery.filter { it.name != correctEntry.name }.shuffled().take(3).map { it.name }
    val options = (wrongNames + correctEntry.name).shuffled()
    return QuizEntry(correctEntry.image, options, correctEntry.name)
}
