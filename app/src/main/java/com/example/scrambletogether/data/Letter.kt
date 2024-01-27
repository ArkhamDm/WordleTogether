package com.example.scrambletogether.data

import androidx.compose.ui.graphics.Color

data class Letter(
    var letter: Char = ' ',
    var color: Color = ColorLetter.None.color
)
