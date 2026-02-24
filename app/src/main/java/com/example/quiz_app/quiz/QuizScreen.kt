package com.example.quiz_app.quiz

import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.quiz_app.R
import com.example.quiz_app.types.QuizEntry
import com.example.quiz_app.ui.AnimatedGlowingButton
import com.example.quiz_app.ui.CustomProgressBar
import com.example.quiz_app.ui.theme.CorrectAnswer
import com.example.quiz_app.ui.theme.IncorrectAnswer

/**
 * Main quiz screen responsible for displaying the question image and multiple choice options.
 * It also shows the current progress and score.
 *
 * @param quizEntry Data for the current question (image, options, correct answer).
 * @param score Current cumulative score.
 * @param questionNumber The index of the current question.
 * @param totalQuestions Total number of questions in the quiz session.
 * @param isAnswered Whether the current question has been answered.
 * @param selectedOption The option selected by the user, if any.
 * @param onAnswerSelected Callback when an option is tapped.
 * @param onNextClicked Callback for navigating to the next question or finishing.
 */
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
    val configuration = LocalConfiguration.current

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeQuizLayout(quizEntry, score, questionNumber, totalQuestions, isAnswered, selectedOption, onAnswerSelected, onNextClicked)
    } else {
        PortraitQuizLayout(quizEntry, score, questionNumber, totalQuestions, isAnswered, selectedOption, onAnswerSelected, onNextClicked)
    }
}

/**
 * Layout for portrait orientation.
 */
@Composable
private fun PortraitQuizLayout(
    quizEntry: QuizEntry, score: Int, questionNumber: Int, totalQuestions: Int,
    isAnswered: Boolean, selectedOption: String?, onAnswerSelected: (String) -> Unit, onNextClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ScoreAndProgress(score, questionNumber, totalQuestions)
        Spacer(modifier = Modifier.height(16.dp))

        // Image display handling for both drawables and URIs
        QuizImage(
            quizEntry,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 16.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnswerGrid(quizEntry, isAnswered, selectedOption, onAnswerSelected)
        
        NextButton(isAnswered, questionNumber, totalQuestions, onNextClicked)
    }
}

/**
 * Layout for landscape orientation.
 */
@Composable
private fun LandscapeQuizLayout(
    quizEntry: QuizEntry, score: Int, questionNumber: Int, totalQuestions: Int,
    isAnswered: Boolean, selectedOption: String?, onAnswerSelected: (String) -> Unit, onNextClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display score and progress bar at the top, spanning the whole width
        ScoreAndProgress(score, questionNumber, totalQuestions)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image takes up slightly more space than answers
            QuizImage(quizEntry, modifier = Modifier.weight(1.3f).fillMaxHeight(), contentScale = ContentScale.Fit)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnswerGrid(quizEntry, isAnswered, selectedOption, onAnswerSelected)
                NextButton(isAnswered, questionNumber, totalQuestions, onNextClicked)
            }
        }
    }
}

/**
 * Displays the current score and a progress bar.
 */
@Composable
private fun ScoreAndProgress(score: Int, questionNumber: Int, totalQuestions: Int) {
    // Display score
    Text(
        text = stringResource(id = R.string.score_format, score, questionNumber),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onPrimary
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Progress bar
    CustomProgressBar(
        progress = if (totalQuestions > 0) questionNumber.toFloat() / totalQuestions.toFloat() else 0f,
        text = stringResource(id = R.string.question_format, questionNumber, totalQuestions)
    )
}

/**
 * Displays the image for the quiz entry, handling both drawable resources and URIs.
 */
@Composable
private fun QuizImage(
    quizEntry: QuizEntry, 
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (quizEntry.isDrawable) {
            Image(
                painter = painterResource(id = quizEntry.drawableId),
                contentDescription = stringResource(id = R.string.quiz_image_content_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, quizEntry.uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, quizEntry.uri)
                ImageDecoder.decodeBitmap(source)
            }
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(id = R.string.quiz_image_content_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    }
}

/**
 * Displays a 2x2 grid of answer options.
 */
@Composable
private fun AnswerGrid(
    quizEntry: QuizEntry,
    isAnswered: Boolean,
    selectedOption: String?,
    onAnswerSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(quizEntry.options) { option ->
            Button(
                onClick = { onAnswerSelected(option) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.primary
                        option == quizEntry.correctName -> CorrectAnswer
                        option == selectedOption && option != quizEntry.correctName -> IncorrectAnswer
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    }
                )
            ) {
                Text(text = option)
            }
        }
    }
}

/**
 * Displays the navigation button shown after a question has been answered.
 */
@Composable
private fun NextButton(isAnswered: Boolean, questionNumber: Int, totalQuestions: Int, onNextClicked: () -> Unit) {
    Box(
        modifier = Modifier.height(80.dp).fillMaxWidth(),
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
