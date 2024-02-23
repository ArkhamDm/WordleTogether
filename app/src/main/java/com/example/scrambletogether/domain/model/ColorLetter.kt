package com.example.scrambletogether.domain.model

import androidx.compose.ui.graphics.Color
import com.example.scrambletogether.presentation.ui.theme.invisibleGray

enum class ColorLetter(val color: Color) {
    Right(Color.Green),
    Almost(Color.Yellow),
    Miss(Color.Gray),
    None(invisibleGray)
}