package dev.arkhamd.wordletogether.wordle.ui.model

import androidx.compose.ui.graphics.Color
import dev.arkhamd.wordletogether.wordle.domain.LetterFeedback

enum class ColorLetter(val color: Color) {
    Right(Color.Green),
    Almost(Color.Yellow),
    Miss(Color.Gray),
    None(Color(100, 100, 100, 200))
}

fun LetterFeedback.toUiColor(): Color = when (this) {
    LetterFeedback.Right -> ColorLetter.Right.color
    LetterFeedback.Almost -> ColorLetter.Almost.color
    LetterFeedback.Miss -> ColorLetter.Miss.color
    LetterFeedback.None -> ColorLetter.None.color
}
