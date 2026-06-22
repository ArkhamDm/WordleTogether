package dev.arkhamd.wordletogether.wordle.domain

interface WordDictionary {
    fun isValid(word: String): Boolean
    fun randomWordOrNull(): String?
}

class StaticWordDictionary(
    words: List<String>,
    answerWords: List<String> = words
) : WordDictionary {
    private val words: List<String> = words
        .map { it.uppercase() }
        .filter { it.length == WordleDefaults.WORD_LENGTH }
        .distinct()

    private val answerWords: List<String> = answerWords
        .map { it.uppercase() }
        .filter { it.length == WordleDefaults.WORD_LENGTH }
        .distinct()

    private val wordSet: Set<String> = this.words.toSet()

    override fun isValid(word: String): Boolean = word.uppercase() in wordSet

    override fun randomWordOrNull(): String? = answerWords.randomOrNull()
}
