package dev.arkhamd.wordletogether.wordle.domain

data class GameBoard(
    val guesses: List<Guess> = emptyList(),
    val maxGuesses: Int = 6,
    val wordLength: Int = 5
)
