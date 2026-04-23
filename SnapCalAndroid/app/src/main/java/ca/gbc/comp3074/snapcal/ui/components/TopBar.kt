@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ca.gbc.comp3074.snapcal.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController

@Composable
fun AppTopBar(
    title: String,
    nav: NavHostController? = null,
    showBack: Boolean = false
) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (showBack && nav != null) {
                IconButton(onClick = { nav.navigateUp() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        }
    )
}
