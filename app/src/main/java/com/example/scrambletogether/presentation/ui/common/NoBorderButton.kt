package com.example.scrambletogether.presentation.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@Composable
fun NoBorderButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    shape: Shape = ButtonDefaults.shape,
    content: @Composable (RowScope.() -> Unit)
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        border = null,
        shape = shape,
        content = content
    )
}