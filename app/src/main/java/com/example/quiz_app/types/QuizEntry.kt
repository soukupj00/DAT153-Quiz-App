package com.example.quiz_app.types

import android.net.Uri

/**
 * Represents a single quiz question.
 * The image is now exclusively handled via Uri.
 */
data class QuizEntry(
    val uri: Uri,
    val options: List<String>,
    val correctName: String,
)
