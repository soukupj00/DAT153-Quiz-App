package com.example.quiz_app.gallery

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quiz_app.types.GalleryEntry

/**
 * Represents a single item in the Gallery grid.
 * Displays an image from a URI and its associated name.
 *
 * @param entry The data model for the gallery item.
 * @param onLongPress Callback triggered when the user performs a long-press on the item.
 * @param modifier Modifier to be applied to the root Column.
 */
@Composable
fun GalleryItem(
    entry: GalleryEntry,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.pointerInput(entry) {
            // Detect long-press gestures to trigger deletion or other actions
            detectTapGestures(onLongPress = { onLongPress() })
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, entry.uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, entry.uri)
                ImageDecoder.decodeBitmap(source)
            }
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = entry.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Display the name of the image below it
        Text(
            text = entry.name,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
