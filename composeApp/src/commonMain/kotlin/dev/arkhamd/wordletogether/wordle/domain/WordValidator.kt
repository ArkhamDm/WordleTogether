package dev.arkhamd.wordletogether.wordle.domain

fun interface WordValidator {
    fun isValid(word: String): Boolean
}
