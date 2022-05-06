package com.remenyo.papertrader.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun BackButton(fn: () -> Unit) {
    IconButton(fn) {
        Icon(Icons.Default.ArrowBack, "Back")
    }
}

