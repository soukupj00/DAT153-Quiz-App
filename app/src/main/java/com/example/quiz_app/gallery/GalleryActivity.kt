package com.example.quiz_app.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.quiz_app.R
import com.example.quiz_app.ui.StandardLayout

/**
 * The GalleryActivity serves as the host for the GalleryScreen composable.
 * It is responsible for managing the top-level state that affects the entire screen,
 * such as the current sort order.
 */
class GalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // State for the sort order is kept at this top-level component.
            // This allows the state to be controlled here and passed down to the GalleryScreen
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
                        val (icon, description) = when (sortOrder) {
                            SortOrder.DESCENDING -> Icons.Default.KeyboardArrowUp to "Sort Ascending"
                            SortOrder.ASCENDING -> Icons.Default.KeyboardArrowDown to "Sort Descending"
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = description,
                            tint = Color.White
                        )
                    }
                }
            ) { innerPadding ->
                GalleryScreen(contentPadding = innerPadding, sortOrder = sortOrder)
            }
        }
    }
}
