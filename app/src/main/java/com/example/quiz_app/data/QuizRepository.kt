package com.example.quiz_app.data

import kotlinx.coroutines.flow.Flow

/**
 * A repository class that abstracts access to the data source.
 * It provides a clean API for the rest of the app to interact with the data,
 * without having to know the implementation details (in this case, Room).
 */
class QuizRepository(private val quizItemDao: QuizItemDao) {


    // A Flow that emits a list of all quiz items from the database.
    // Using a Flow allows the UI to reactively update whenever the data changes.
    val allItems: Flow<List<QuizItem>> = quizItemDao.getAllItems()

    // suspend function - it must be called from a coroutine.
    suspend fun insert(item: QuizItem) {
        quizItemDao.insert(item)
    }

    suspend fun delete(item: QuizItem) {
        quizItemDao.delete(item)
    }
}
