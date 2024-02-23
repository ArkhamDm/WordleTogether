package com.example.scrambletogether.domain.model

import androidx.compose.ui.graphics.Color

data class Letter(
    var letter: Char = ' ',
    var color: Color = ColorLetter.None.color
)
