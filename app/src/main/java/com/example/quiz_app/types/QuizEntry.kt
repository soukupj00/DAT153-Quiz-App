package com.example.quiz_app.types

import androidx.annotation.DrawableRes

/**
 * Represents a single quiz question, including the image, a list of name options, and the correct name.
 */
data class QuizEntry(
    @param:DrawableRes val image: Int,
    val options: List<String>,
    val correctName: String,
)
