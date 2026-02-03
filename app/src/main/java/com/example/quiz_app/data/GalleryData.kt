package com.example.quiz_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.quiz_app.types.GalleryEntry
import com.example.quiz_app.R

/**
 * A singleton object to hold the shared gallery data.
 * Using a mutableStateListOf ensures that changes to this list will automatically
 * trigger recomposition in any Composable that reads from it.
 *
 * Later this will probably be replaced with storage on mobile device
 */
object GalleryData {

    private val _entries = mutableStateListOf(
        GalleryEntry("Jupiter", R.drawable.jupiter),
        GalleryEntry("Mars", R.drawable.mars),
        GalleryEntry("Uranus", R.drawable.uranus),
        GalleryEntry("Venus", R.drawable.venus)
    )

    val entries: List<GalleryEntry>
        get() = _entries

    /**
     * Adds a new entry to the gallery.
     * Right now this does not persist the change to the device storage.
     */
    fun addEntry(entry: GalleryEntry) {
        _entries.add(entry)
    }
}