package dev.arkhamd.wordletogether.wordle.data

import dev.arkhamd.wordletogether.wordle.domain.StaticWordDictionary
import dev.arkhamd.wordletogether.wordle.domain.WordDictionary

private const val DEFAULT_ANSWER_WORD_COUNT = 819
private val nonCyrillicLowercaseLetters = Regex("[^а-я]")

fun buildBundledWordDictionary(lines: Sequence<String>): WordDictionary {
    val words = parseBundledWordList(lines)
    return StaticWordDictionary(
        words = words,
        answerWords = words.take(DEFAULT_ANSWER_WORD_COUNT)
    )
}

fun parseBundledWordList(lines: Sequence<String>): List<String> = lines.map { line ->
    line.replace(nonCyrillicLowercaseLetters, "").uppercase()
}
    .filter { it.isNotEmpty() }
    .toList()
