package com.example.quiz_app.gallery

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.quiz_app.R
import com.example.quiz_app.data.AppDatabase
import com.example.quiz_app.data.QuizItem
import com.example.quiz_app.data.QuizRepository
import com.example.quiz_app.types.GalleryEntry
import kotlinx.coroutines.launch

/**
 * Enumeration representing the possible sort orders for the gallery.
 */
enum class SortOrder {
    ASCENDING, DESCENDING
}

/**
 * The main UI content for the gallery screen.
 * It manages the display of images in a grid, sorting logic, and adding/deleting entries.
 *
 * @param contentPadding Padding applied to the root container, typically from a Scaffold.
 * @param sortOrder The current order in which entries should be displayed.
 */
@Composable
fun GalleryScreen(contentPadding: PaddingValues, sortOrder: SortOrder) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current

    // Initialize repository. `remember` ensures this is done only once.
    val repository = remember {
        val dao = AppDatabase.getDatabase(context.applicationContext, scope).quizItemDao()
        QuizRepository(dao)
    }

    var entries by remember { mutableStateOf<List<GalleryEntry>>(emptyList()) }
    var rawItems by remember { mutableStateOf<List<QuizItem>>(emptyList()) }

    // `LaunchedEffect(Unit)` runs this block once when the composable enters the composition.
    // It collects items from the database and updates the local state.
    LaunchedEffect(Unit) {
        repository.allItems.collect { items ->
            rawItems = items
            entries = items.map { item ->
                GalleryEntry(
                    item.name,
                    if (item.isDrawable) item.uri.substringAfterLast("/").toInt() else Uri.parse(
                        item.uri
                    )
                )
            }
        }
    }

    // Sort entries based on the selected order
    val sortedEntries = when (sortOrder) {
        SortOrder.ASCENDING -> entries.sortedBy { it.name.lowercase() }
        SortOrder.DESCENDING -> entries.sortedByDescending { it.name.lowercase() }
    }

    var showNameDialog by remember { mutableStateOf(false) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<GalleryEntry?>(null) }

    // `rememberLauncherForActivityResult` is a modern way to handle Activity results (like picking an image).
    // It safely manages the lifecycle of the launcher.
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            newImageUri = it
            showNameDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        if (sortedEntries.isEmpty()) {
            EmptyGalleryText()
        } else {
            val columns =
                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2
            ImageGrid(columns = columns, entries = sortedEntries) {
                showDeleteDialog = true
                entryToDelete = it
            }
        }

        // Add image button
        FloatingActionButton(
            onClick = { pickImageLauncher.launch(arrayOf("image/*")) },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Image")
        }
    }

    if (showNameDialog) {
        AddNameDialog(
            onDismiss = { showNameDialog = false },
            onConfirm = { name ->
                newImageUri?.let { uri ->
                    scope.launch {
                        try {
                            // We must take persistable URI permissions to access the image
                            // after the initial selection.
                            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            context.contentResolver.takePersistableUriPermission(uri, takeFlags)

                            repository.insert(
                                QuizItem(
                                    name = name,
                                    uri = uri.toString(),
                                    isDrawable = false
                                )
                            )
                        } catch (e: SecurityException) {
                            // This may fail on some devices/older Android versions, but we still try to save it.
                            repository.insert(
                                QuizItem(
                                    name = name,
                                    uri = uri.toString(),
                                    isDrawable = false
                                )
                            )
                        }
                    }
                }
                showNameDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                entryToDelete?.let { entry ->
                    scope.launch {
                        val itemToDelete = rawItems.find { it.name == entry.name }
                        itemToDelete?.let { repository.delete(it) }
                    }
                }
                showDeleteDialog = false
            }
        )
    }
}

@Composable
private fun EmptyGalleryText() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(id = R.string.gallery_empty),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
    }
}

@Composable
private fun ImageGrid(
    columns: Int,
    entries: List<GalleryEntry>,
    onLongPress: (GalleryEntry) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(entries) { entry ->
            GalleryItem(
                entry = entry,
                onLongPress = { onLongPress(entry) }
            )
        }
    }
}
