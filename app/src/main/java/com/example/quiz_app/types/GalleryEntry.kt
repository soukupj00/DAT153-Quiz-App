package com.example.quiz_app.types

import android.net.Uri

/**
 * Represents an entry in the gallery, with a name and an image URI.
 */
data class GalleryEntry(
    val name: String,
    val uri: Uri
)
