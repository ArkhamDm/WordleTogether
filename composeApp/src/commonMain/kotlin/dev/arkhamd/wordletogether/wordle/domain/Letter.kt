package dev.arkhamd.wordletogether.wordle.domain

data class Letter(
    val letter: Char = ' ',
    val feedback: LetterFeedback = LetterFeedback.None
) {
    val value: Char
        get() = letter
}
