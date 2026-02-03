package com.example.quiz_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.quiz_app.gallery.GalleryActivity
import com.example.quiz_app.quiz.QuizActivity
import com.example.quiz_app.ui.AnimatedGlowingButton
import com.example.quiz_app.ui.NorthernLightsBackground

/**
 * Used as main activity, needed to change it per task requirements for XML layout
 * This will hopefully replace the XML layout in the future
 */
class ComposeMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NorthernLightsBackground {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedGlowingButton(
            onClick = { context.startActivity(Intent(context, GalleryActivity::class.java)) },
            text = stringResource(id = R.string.gallery)
        )
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedGlowingButton(
            onClick = { context.startActivity(Intent(context, QuizActivity::class.java)) },
            text = stringResource(id = R.string.quiz)
        )
    }
}
