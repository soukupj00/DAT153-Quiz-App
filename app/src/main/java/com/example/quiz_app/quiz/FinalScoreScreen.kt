package com.example.quiz_app.quiz

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.quiz_app.R
import com.example.quiz_app.gallery.GalleryActivity
import com.example.quiz_app.ui.AnimatedGlowingButton

/**
 * Displays the final score and provides options to restart or navigate away.
 * It adapts its layout based on the device orientation.
 *
 * @param score The final score achieved.
 * @param total The total number of questions attempted.
 * @param onTryAgain Callback to restart the quiz.
 */
@Composable
fun FinalScoreScreen(score: Int, total: Int, onTryAgain: () -> Unit) {
    val configuration = LocalConfiguration.current

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeFinalScoreLayout(score, total, onTryAgain)
    } else {
        PortraitFinalScoreLayout(score, total, onTryAgain)
    }
}

/**
 * Layout for portrait orientation.
 */
@Composable
private fun PortraitFinalScoreLayout(score: Int, total: Int, onTryAgain: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ScoreText(score, total)
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedGlowingButton(
            onClick = onTryAgain,
            text = stringResource(id = R.string.try_again)
        )

        Spacer(modifier = Modifier.height(48.dp))

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

/**
 * Layout for landscape orientation.
 */
@Composable
private fun LandscapeFinalScoreLayout(score: Int, total: Int, onTryAgain: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ScoreText(score, total)
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedGlowingButton(
            onClick = onTryAgain,
            text = stringResource(id = R.string.try_again)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Home and Gallery buttons in a row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedGlowingButton(
                onClick = { (context as? Activity)?.finish() },
                text = stringResource(id = R.string.home)
            )
            AnimatedGlowingButton(
                onClick = { context.startActivity(Intent(context, GalleryActivity::class.java)) },
                text = stringResource(id = R.string.gallery)
            )
        }
    }
}

/**
 * Displays the final score text.
 */
@Composable
private fun ScoreText(score: Int, total: Int) {
    Text(
        text = stringResource(id = R.string.final_score),
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onPrimary
    )
    Text(
        text = stringResource(id = R.string.score_format, score, total),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onPrimary
    )
}
