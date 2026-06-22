package dev.arkhamd.wordletogether.wordle.domain

data class Guess(
    val value: String
) {
    val letters: List<Letter> = value.map { Letter(letter = it) }
}
