package com.remenyo.papertrader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.remenyo.papertrader.ui.theme.AppTypography

@Composable
fun TopAppBar(
    text: String,
    leftButton: @Composable () -> Unit = { Text("") },
    rightButton: @Composable () -> Unit = { Text("") }
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.Start)
            ) { leftButton() }

            Text(
                text,
                style = AppTypography.titleLarge,
                modifier = Modifier.weight(2f),
                textAlign = TextAlign.Center
            )

            Box(
                Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.End)
            ) { rightButton() }
        }
    }
}