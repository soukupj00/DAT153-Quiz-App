package com.example.quiz_app.gallery

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.example.quiz_app.R
import com.example.quiz_app.data.GalleryData
import com.example.quiz_app.types.GalleryEntry
import com.example.quiz_app.ui.StandardLayout
import java.io.File

/**
 * The GalleryActivity is responsible for displaying the collection of images
 * and allowing the user to add new images either from the device's gallery
 * or by taking a new photo with the camera.
 *
 * Why We chose Coil package - first Google answer and from the documentation it seemed like a
 * well maintained and used package for handling images in Android apps
 * It can intelligently handle different image sources. In our case, `entry.image` can be
 * either an Int (a drawable resource ID for our built-in images) or a Uri
 * (for user-added images). Coil automatically determines the source type and loads
 * the image asynchronously, handling caching and optimization for us.
 * https://coil-kt.github.io/coil/compose/
 */
class GalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StandardLayout(title = stringResource(id = R.string.gallery)) { innerPadding ->
                GalleryScreen(contentPadding = innerPadding)
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
fun GalleryScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val entries = GalleryData.entries

    // State management for the dialogs
    var showSourceDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    /**
     * Creates a temporary, secure URI for storing a photo taken with the camera
     * Camera needs a location to save the image it captures. We must provide this location as a URI
     * By creating a temporary file, we a location where to store it and know how to access it
     *
     * Modern Android versions have strict security policies that prevent apps from sharing file URIs (file://) directly
     * A FileProvider creates secure, shareable content URI (content://) that grants temporary access
     * to a specific file or directory, sort of a workaround for this issue
     * This function's logic is linked to the provider path defined in `res/xml/file_paths.xml`
     * https://developer.android.com/reference/androidx/core/content/FileProvider
     */
    fun getTempUri(): Uri {
        val imagePath = File(context.cacheDir, "images")
        imagePath.mkdirs() // Ensure the 'images' subdirectory exists.
        val tmpFile = File.createTempFile("temp_image_file", ".png", imagePath).apply {
            createNewFile()
            deleteOnExit() // Ensures the temp file is cleaned up when the app's VM terminates.
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", tmpFile)
    }

    // Launcher for picking an image from the gallery.
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { // If the user successfully selected an image, images URI is returned
            newImageUri = it
            showNameDialog = true
        }
    }

    // Launcher handles the result from the camera app.
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            newImageUri?.let {
                showNameDialog = true
            }
        }
    }

    // Launcher handles the runtime camera permission request.
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // If permission is granted, create a temp URI and launch the camera.
            val tempUri = getTempUri()
            newImageUri = tempUri
            takePictureLauncher.launch(tempUri)
        } else {
            // I don't know if we should show some sort of alert when permission is denied?
            // Most likely not as it's users choice
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            // composable function from Coil, handles different image sources
                            // entry.image can be an Int - a drawable resource ID for our built-in images
                            // or a Uri - for user-added images
                            painter = rememberAsyncImagePainter(entry.image),
                            contentDescription = entry.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
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
            onClick = { showSourceDialog = true },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Image")
        }
    }

    if (showSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showSourceDialog = false },
            onGalleryClick = {
                showSourceDialog = false
                pickImageLauncher.launch("image/*")
            },
            onCameraClick = {
                showSourceDialog = false
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }

    // Ask user for name of image, if successful, add to GalleryData object
    if (showNameDialog) {
        AddNameDialog(
            onDismiss = { showNameDialog = false },
            onConfirm = { name ->
                newImageUri?.let {
                    GalleryData.addEntry(GalleryEntry(name, it))
                }
                showNameDialog = false
            }
        )
    }
}

/**
 * Dialog that asks the user to choose between the Gallery and Camera as an image source.
 */
@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Image") },
        text = { Text("Choose a source for your new image.") },
        confirmButton = {
            Button(onClick = onGalleryClick) {
                Text("Gallery")
            }
        },
        dismissButton = {
            Button(onClick = onCameraClick) {
                Text("Camera")
            }
        }
    )
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
