package dev.arkhamd.wordletogether.wordle.domain

data class WordleRoundState(
    val game: WordleGameState = WordleDefaults.startGameState(),
    val keyboard: List<List<Letter>> = WordleDefaults.startKeyboard()
)

data class SubmitGuessResult(
    val roundState: WordleRoundState,
    val submitted: Boolean
)

class WordleGameReducer(
    private val scoreGuess: ScoreGuessUseCase = ScoreGuessUseCase()
) {
    fun startRound(): WordleRoundState = WordleRoundState()

    fun addLetter(roundState: WordleRoundState, letter: Char): WordleRoundState {
        val game = roundState.game
        val newTryingWords = game.tryingWords.mutableGridCopy()
        val currentLine = newTryingWords.getOrNull(game.wordsInLine) ?: return roundState

        if (game.madeWordInLine) return roundState

        val indexOfSpace = currentLine.indexOf(Letter())
        if (indexOfSpace < 0) return roundState

        currentLine[indexOfSpace] = Letter(letter = letter)
        val madeFullWordInLine = indexOfSpace == currentLine.lastIndex

        return roundState.copy(
            game = game.copy(
                tryingWords = newTryingWords.immutableGridCopy(),
                madeWordInLine = madeFullWordInLine || game.madeWordInLine,
                wordsInLine = game.wordsInLine,
                isLose = game.isLose
            )
        )
    }

    fun deleteLetter(roundState: WordleRoundState): WordleRoundState {
        val game = roundState.game
        val newTryingWords = game.tryingWords.mutableGridCopy()
        val currentLine = newTryingWords.getOrNull(game.wordsInLine) ?: return roundState

        val indexOfChar = currentLine.indexOf(Letter()) - 1
        when {
            indexOfChar >= 0 -> currentLine[indexOfChar] = Letter()
            indexOfChar == -2 && currentLine.isNotEmpty() -> currentLine[currentLine.lastIndex] = Letter()
            else -> return roundState
        }

        return roundState.copy(
            game = game.copy(
                tryingWords = newTryingWords.immutableGridCopy(),
                madeWordInLine = false,
                wordsInLine = game.wordsInLine,
                isLose = game.isLose
            )
        )
    }

    fun submitGuess(
        roundState: WordleRoundState,
        targetWord: String,
        isValidWord: (String) -> Boolean
    ): SubmitGuessResult {
        val game = roundState.game
        val amountOfWords = game.tryingWords.size
        if (!game.madeWordInLine || game.wordsInLine >= amountOfWords) {
            return SubmitGuessResult(roundState = roundState, submitted = false)
        }

        val answerString = game.currentGuess()
        if (!isValidWord(answerString)) {
            return SubmitGuessResult(roundState = roundState, submitted = false)
        }

        if (targetWord.length != answerString.length || targetWord.isBlank()) {
            return SubmitGuessResult(roundState = roundState, submitted = false)
        }

        val scoredGuess = scoreGuess(answerString, targetWord)
        val newTryingWords = game.tryingWords.mutableGridCopy()
        val answer = game.tryingWords[game.wordsInLine]
            .map { it.copy() }
            .toMutableList()
        var keyboard = roundState.keyboard

        for (index in answer.indices) {
            val feedback = scoredGuess[index]
            answer[index] = Letter(answer[index].letter, feedback)
            keyboard = updateKeyboard(keyboard, answer[index].letter, feedback)
        }

        val isWin = answer.count { it.feedback == LetterFeedback.Right } == WordleDefaults.WORD_LENGTH
        val isLose = game.wordsInLine.inc() == amountOfWords

        newTryingWords[game.wordsInLine] = answer

        return SubmitGuessResult(
            roundState = roundState.copy(
                game = game.copy(
                    tryingWords = newTryingWords.immutableGridCopy(),
                    wordsInLine = if (isLose || isWin) game.wordsInLine else game.wordsInLine.inc(),
                    madeWordInLine = false,
                    isWin = isWin,
                    isLose = isLose
                ),
                keyboard = keyboard
            ),
            submitted = true
        )
    }

    fun closeLine(roundState: WordleRoundState): WordleRoundState {
        val game = roundState.game
        val amountOfWords = game.tryingWords.size
        val newTryingWords = game.tryingWords.mutableGridCopy()
        val currentLine = newTryingWords.getOrNull(game.wordsInLine) ?: return roundState
        val isLose = game.wordsInLine.inc() == amountOfWords

        newTryingWords[game.wordsInLine] = List(currentLine.size) {
            Letter(' ', LetterFeedback.Miss)
        }.toMutableList()

        return roundState.copy(
            game = game.copy(
                tryingWords = newTryingWords.immutableGridCopy(),
                wordsInLine = if (isLose) game.wordsInLine else game.wordsInLine.inc(),
                madeWordInLine = false,
                isWin = game.isWin,
                isLose = isLose
            )
        )
    }

    fun clearLose(roundState: WordleRoundState): WordleRoundState = roundState.copy(
        game = roundState.game.copy(isLose = false)
    )

    private fun updateKeyboard(
        keyboard: List<List<Letter>>,
        letter: Char,
        feedback: LetterFeedback
    ): List<List<Letter>> {
        val newKeyboard = keyboard.mutableGridCopy()
        for (line in newKeyboard.indices) {
            val indexOfChar = newKeyboard[line].indexOfFirst {
                it.letter == letter
            }

            if (indexOfChar >= 0) {
                val currentFeedback = newKeyboard[line][indexOfChar].feedback
                if (feedback.priority() > currentFeedback.priority()) {
                    newKeyboard[line][indexOfChar] = Letter(letter, feedback)
                }
                break
            }
        }

        return newKeyboard.immutableGridCopy()
    }
}

fun WordleGameState.currentGuess(): String = tryingWords
    .getOrNull(wordsInLine)
    ?.joinToString(separator = "") { it.letter.toString() }
    .orEmpty()

fun WordleGameState.canSubmitGuess(isValidWord: (String) -> Boolean): Boolean = madeWordInLine && currentGuess().let(isValidWord)

private fun LetterFeedback.priority(): Int = when (this) {
    LetterFeedback.Right -> 3
    LetterFeedback.Almost -> 2
    LetterFeedback.Miss -> 1
    LetterFeedback.None -> 0
}

private fun <T> List<List<T>>.mutableGridCopy(): MutableList<MutableList<T>> = map { it.toMutableList() }.toMutableList()

private fun <T> List<List<T>>.immutableGridCopy(): List<List<T>> = map { it.toList() }
