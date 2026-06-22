package dev.arkhamd.wordletogether.session.domain

import dev.arkhamd.wordletogether.wordle.domain.LetterFeedback

data class SessionLetter(
    val letter: Char = ' ',
    val feedback: LetterFeedback = LetterFeedback.None
)
