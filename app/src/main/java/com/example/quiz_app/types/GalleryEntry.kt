package com.example.quiz_app.types

import android.net.Uri

/**
 * Represents an entry in the gallery, with a name and an image URI.
 */
data class GalleryEntry(
    val name: String,
    val image: Any // Can be Int for resource or Uri for user images
) {
    val isDrawable: Boolean
        get() = image is Int

    val drawableId: Int
        get() = image as Int

    val uri: Uri
        get() = image as Uri
}
