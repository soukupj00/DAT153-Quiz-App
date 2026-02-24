package com.example.quiz_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.quiz_app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The Room database for the app. It uses the Singleton pattern to ensure only
 * one instance of the database is created, which is expensive.
 */
@Database(entities = [QuizItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun quizItemDao(): QuizItemDao

    companion object {
        //Marks the JVM backing field of the annotated var property as volatile,
        // meaning that reads and writes to this field are atomic and writes are always
        // made visible to other threads. If another thread reads the value of this field
        // (e.g. through its accessor), it sees not only that value,
        // but all side effects that led to writing that value.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the database instance. If it doesn't exist, it creates it in a thread-safe way.
         */
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            // synchronized(this) prevents multiple threads from creating the database at once.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiz_database"
                )
                    .addCallback(AppDatabaseCallback(scope, context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * A callback that triggers when the database is created for the first time.
     * We use this to pre-populate the database with default items.
     */
    private class AppDatabaseCallback(
        private val scope: CoroutineScope,
        private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                // scope.launch starts a coroutine to do work in the background without
                // blocking the UI thread.
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.quizItemDao(), context)
                }
            }
        }

        /**
         * Inserts the default set of planets into the database.
         */
        suspend fun populateDatabase(quizItemDao: QuizItemDao, context: Context) {
            val packageName = context.packageName
            val planets = listOf(
                "Jupiter" to R.drawable.jupiter,
                "Mars" to R.drawable.mars,
                "Venus" to R.drawable.venus,
                "Uranus" to R.drawable.uranus
            )

            planets.forEach { (name, resId) ->
                quizItemDao.insert(
                    QuizItem(
                        name = name,
                        uri = "android.resource://$packageName/$resId",
                        isDrawable = true
                    )
                )
            }
        }
    }
}
