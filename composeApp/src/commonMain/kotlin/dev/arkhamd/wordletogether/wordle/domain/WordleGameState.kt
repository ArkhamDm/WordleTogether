package dev.arkhamd.wordletogether.wordle.domain

data class WordleGameState(
    val tryingWords: List<List<Letter>>,
    val madeWordInLine: Boolean = false,
    val wordsInLine: Int = 0,
    val isWin: Boolean = false,
    val isLose: Boolean = false
)

object WordleDefaults {
    const val MAX_GUESSES: Int = 6
    const val WORD_LENGTH: Int = 5

    fun startGameState(): WordleGameState = WordleGameState(
        tryingWords = List(MAX_GUESSES) {
            List(WORD_LENGTH) { Letter() }
        }
    )

    fun startKeyboard(): List<List<Letter>> = listOf(
        "ЙЦУКЕНГШЩЗХЪ".toKeyboardLine(),
        "ФЫВАПРОЛДЖЭ".toKeyboardLine(),
        "ЯЧСМИТЬБЮ".toKeyboardLine()
    )

    private fun String.toKeyboardLine(): List<Letter> = List(length) { index -> Letter(letter = this[index]) }
}
