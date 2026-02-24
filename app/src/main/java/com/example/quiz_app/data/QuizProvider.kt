package com.example.quiz_app.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

/**
 * A ContentProvider that exposes quiz entries to other applications.
 * This allows external tools (like adb) or other apps to query the names and image URIs.
 */
class QuizProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.quiz_app.provider"

        private const val ITEMS = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            // Matches 'content://com.example.quiz_app.provider/quiz_items'
            addURI(AUTHORITY, "quiz_items", ITEMS)
        }
    }

    private lateinit var database: AppDatabase

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(): Boolean {
        // Initialize the Room database. Using GlobalScope here as ContentProvider 
        // lifecycles are tied to the process.
        database = AppDatabase.getDatabase(context!!, GlobalScope)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        if (uriMatcher.match(uri) == ITEMS) {
            // Requirement specifies column names "name" and "URI".
            // We use SQL 'AS' to rename the internal 'uri' column to 'URI'.
            val cursor = database.query("SELECT name, uri AS URI FROM quiz_items", null)
            cursor.setNotificationUri(context?.contentResolver, uri)
            return cursor
        }
        return null
    }

    /**
     * Returns the MIME type of the data at the given URI.
     *
     * The format "vnd.android.cursor.dir/vnd.com.example.quiz_app.quiz_items" follows Android conventions:
     * - "vnd.android.cursor.dir": Indicates a directory/list of items - multiple rows.
     *   (If it was a single item, it would be "vnd.android.cursor.item").
     * - "vnd.com.example.quiz_app.quiz_items": A sub-type defined by our app.
     */
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            ITEMS -> "vnd.android.cursor.dir/vnd.com.example.quiz_app.quiz_items"
            else -> null
        }
    }

    // read-only provider means that external applications (like the adb tool) can only read (query)
    // the data we've exposed. They are restricted from adding new data, deleting existing records,
    // or modifying them through this specific interface.
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("This provider is read-only")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("This provider is read-only")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("This provider is read-only")
    }
}
