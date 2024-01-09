package com.example.scrambletogether.data

import androidx.compose.ui.graphics.Color

data class LetterDataClass(
    var letter: Char = ' ',
    var color: Color = ColorLetter.None.color
)
