package com.example.quiz_app.types

import android.net.Uri

/**
 * Represents an entry in the gallery, with a name and an image.
 * The image can be either a drawable resource (for pre-existing images)
 * or a URI (for user-added images).
 */
data class GalleryEntry(
    val name: String,
    val image: Any,
) {
    /**
     * Property to check if the image is from a drawable resource.
     * @return `true` if the image is an `Int`, `false` otherwise.
     */
    val isDrawable: Boolean
        get() = image is Int

    /**
     * @return The resource ID as an `Int`.
     * @throws ClassCastException if the image is not an Int.
     */
    val drawableId: Int
        get() = image as Int

    /**
     * @return The image's `Uri`.
     * @throws ClassCastException if the image is not a Uri.
     */
    val uri: Uri
        get() = image as Uri
}
