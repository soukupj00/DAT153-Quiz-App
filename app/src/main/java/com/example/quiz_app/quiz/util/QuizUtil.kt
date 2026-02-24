package com.example.quiz_app.quiz.util

import com.example.quiz_app.types.GalleryEntry
import com.example.quiz_app.types.QuizEntry

/**
 * Generates a quiz question from a correct gallery entry.
 * It creates a list of 4 options: 1 correct name and 3 incorrect names from the gallery.
 *
 * @param correctEntry The gallery entry that is the correct answer.
 * @param gallery The full list of gallery entries to pick wrong answers from.
 * @return A [QuizEntry] for the quiz.
 */
fun generateQuizEntry(correctEntry: GalleryEntry, gallery: List<GalleryEntry>): QuizEntry {
    val wrongNames =
        gallery.filter { it.name != correctEntry.name }.shuffled().take(3).map { it.name }
    val options = (wrongNames + correctEntry.name).shuffled()
    return QuizEntry(correctEntry.image, options, correctEntry.name)
}
