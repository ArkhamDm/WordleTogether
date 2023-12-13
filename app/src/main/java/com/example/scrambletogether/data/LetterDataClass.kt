package com.example.scrambletogether.data

import androidx.compose.ui.graphics.Color
import com.example.scrambletogether.ui.theme.invisibleGray

data class LetterDataClass(
    var letter: Char = ' ',
    var color: Color = invisibleGray
)
