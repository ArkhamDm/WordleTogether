package com.example.scrambletogether.presentation.ui.multiplayer.dialogs.extras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(dotsCount: Int) {
    Row {
        repeat(dotsCount) {
            Dot()
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
fun Dot() {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color = Color.White, shape = CircleShape)
    )
}
