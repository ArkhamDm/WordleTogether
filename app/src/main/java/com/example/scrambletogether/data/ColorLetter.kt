package com.example.scrambletogether.data

import androidx.compose.ui.graphics.Color
import com.example.scrambletogether.ui.theme.invisibleGray

enum class ColorLetter(val color: Color) {
    Right(Color.Green),
    Almost(Color.Yellow),
    Miss(Color.Gray),
    None(invisibleGray)
}