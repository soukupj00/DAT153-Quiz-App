package com.example.quiz_app.types

import androidx.annotation.DrawableRes

/**
 * Represents an entry in the gallery, with a name and an image.
 */
data class GalleryEntry(
    val name: String,
    @param:DrawableRes val image: Int,
)
