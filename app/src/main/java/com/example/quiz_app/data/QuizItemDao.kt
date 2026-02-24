package com.example.quiz_app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the quiz_items table.
 * The @Dao annotation tells Room that this is a DAO interface.
 */
@Dao
interface QuizItemDao {

    /**
     * This function returns a Flow - UI can observe it for changes.
     * Whenever the data in the quiz_items table changes, the Flow will emit the new list.
     */
    @Query("SELECT * FROM quiz_items")
    fun getAllItems(): Flow<List<QuizItem>>

    /**
     * `onConflict = OnConflictStrategy.REPLACE` tells Room to replace the existing item
     * if a new item with the same primary key is inserted.
     * `suspend` marks this as a function that must be called from a coroutine.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: QuizItem)

    @Delete
    suspend fun delete(item: QuizItem)

    /**
     * A delete all items from the table.
     */
    @Query("DELETE FROM quiz_items")
    suspend fun deleteAll()
}
