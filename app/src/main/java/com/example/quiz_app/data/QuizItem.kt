package com.example.quiz_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single quiz item in the database.
 * The @Entity annotation tells Room that this class represents a table in the database.
 */
@Entity(tableName = "quiz_items")
data class QuizItem(
    /**
     * primary key for the table.
     * `autoGenerate = true` - Room will automatically generate a unique ID for each item.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    // The URI of the image associated with the item.
    val uri: String,

    // A flag to indicate if the image is a drawable resource or a file URI.
    val isDrawable: Boolean = false
)
