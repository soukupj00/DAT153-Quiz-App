package com.example.quiz_app.gallery

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quiz_app.R
import com.example.quiz_app.data.GalleryData
import com.example.quiz_app.types.GalleryEntry
import com.example.quiz_app.ui.StandardLayout

enum class SortOrder {
    ASCENDING, DESCENDING
}

/**
 * The GalleryActivity is responsible for displaying the collection of images,
 * allowing the user to add new images from the device's gallery,
 * removing images and sorting them
 */
class GalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var sortOrder by remember { mutableStateOf(SortOrder.ASCENDING) }
            StandardLayout(
                title = stringResource(id = R.string.gallery),
                actions = {
                    IconButton(onClick = {
                        sortOrder = when (sortOrder) {
                            SortOrder.ASCENDING -> SortOrder.DESCENDING
                            SortOrder.DESCENDING -> SortOrder.ASCENDING
                        }
                    }) {
                        when (sortOrder) {
                            SortOrder.DESCENDING -> Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = "Sort Ascending",
                                tint = Color.White
                            )

                            SortOrder.ASCENDING -> Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Sort Descending",
                                tint = Color.White
                            )
                        }
                    }
                }
            ) { innerPadding ->
                GalleryScreen(contentPadding = innerPadding, sortOrder = sortOrder)
            }
        }
    }
}

/**
 * The main composable for the gallery screen. It manages the UI and state for displaying images,
 * handling user input for adding new images
 * and coordinating the permission and activity result launchers
 */
@Composable
fun GalleryScreen(contentPadding: PaddingValues, sortOrder: SortOrder) {
    val context = LocalContext.current
    val entries = when (sortOrder) {
        SortOrder.ASCENDING -> GalleryData.entries.sortedBy { it.name.lowercase() }
        SortOrder.DESCENDING -> GalleryData.entries.sortedByDescending { it.name.lowercase() }
    }
    var showNameDialog by remember { mutableStateOf(false) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<GalleryEntry?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
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
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.gallery_empty),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        } else {
            // LazyVerticalGrid is used for displaying a grid of items,
            // it only composes and lays out visible items.
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(entries) { entry ->

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.pointerInput(entry) {
                            detectTapGestures(onLongPress = {
                                showDeleteDialog = true
                                entryToDelete = entry
                            })
                        }
                    ) {
                        if (entry.isDrawable) {
                            Image(
                                painter = painterResource(id = entry.drawableId),
                                contentDescription = entry.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )
                            // Handles rendering of images that are not from drawable resources,
                            // - images that have been added by the user from the gallery
                        } else {
                            // To display an image from a Uri, we first need to decode it into a Bitmap
                            // The method for doing this differs based on the Android API level
                            // IDK why, but google and AndroidStudio says so
                            // - guess that's why Coil exists, is being used and recommended
                            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                                // For android versions older then API 28, we use the getBitmap()
                                // It takes a ContentResolver and the image's Uri to retrieve the bitmap
                                MediaStore.Images.Media.getBitmap(
                                    context.contentResolver,
                                    entry.uri
                                )
                            } else {
                                // For android API 28 and newer, we use the modern ImageDecoder API
                                // which is recommended, should be safer and more efficient
                                // Create a source from the Uri using the contentResolver
                                val source =
                                    ImageDecoder.createSource(context.contentResolver, entry.uri)
                                // Decode the source into a Bitmap
                                ImageDecoder.decodeBitmap(source)
                            }
                            // The standard Image composable requires ImageBitmap - Compose-specific
                            // container for bitmap images. We convert our platform Bitmap to an ImageBitmap
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = entry.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                // Crop scales the image to fill the bounds of the container,
                                // maintaining the aspect ratio but cropping any parts of the image
                                // that extend beyond the container's dimensions
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = entry.name,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { pickImageLauncher.launch("image/*") },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Image")
        }
    }

    // Ask user for name of image, if successful, add to GalleryData object
    if (showNameDialog) {
        AddNameDialog(
            onDismiss = { showNameDialog = false },
            onConfirm = { name ->
                newImageUri?.let { uri ->
                    try {
                        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                        GalleryData.addEntry(GalleryEntry(name, uri))
                    } catch (e: SecurityException) {
                        e.printStackTrace()
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
                entryToDelete?.let { GalleryData.removeEntry(it) }
                showDeleteDialog = false
            }
        )
    }
}

/**
 * Dialog that asks the user to enter a name for the new image they have selected or captured.
 */
@Composable
fun AddNameDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Name Your Image") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Image Name") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text("Add")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Image") },
        text = { Text("Are you sure you want to delete this image?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
