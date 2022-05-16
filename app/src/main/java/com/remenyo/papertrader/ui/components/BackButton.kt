package com.remenyo.papertrader.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun BackButton(fn: () -> Unit) {
    IconButton(fn, modifier= Modifier.testTag("back")) {
        Icon(Icons.Default.ArrowBack, "Back")
    }
}

