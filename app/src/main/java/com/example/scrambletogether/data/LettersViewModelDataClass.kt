package com.example.scrambletogether.data

data class LettersViewModelDataClass(
    val tryingWords: Array<Array<LetterDataClass>>,
    var madeWordInLine: Boolean = false,
    var wordsInLine: Int = 0,
    var isDone: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LettersViewModelDataClass

        if (!tryingWords.contentDeepEquals(other.tryingWords)) return false
        if (madeWordInLine != other.madeWordInLine) return false
        if (wordsInLine != other.wordsInLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tryingWords.contentDeepHashCode()
        result = 31 * result + madeWordInLine.hashCode()
        result = 31 * result + wordsInLine
        return result
    }
}
