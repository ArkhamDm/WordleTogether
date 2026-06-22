package dev.arkhamd.wordletogether.wordle.ui

import dev.arkhamd.wordletogether.session.domain.MultiplayerSessionGateway
import dev.arkhamd.wordletogether.session.domain.ResultState
import dev.arkhamd.wordletogether.session.domain.SessionGatewayError
import dev.arkhamd.wordletogether.session.domain.SessionHandle
import dev.arkhamd.wordletogether.session.domain.toSessionGrid
import dev.arkhamd.wordletogether.wordle.domain.Letter
import dev.arkhamd.wordletogether.wordle.domain.WordDictionary
import dev.arkhamd.wordletogether.wordle.domain.WordValidator
import dev.arkhamd.wordletogether.wordle.domain.WordleDefaults
import dev.arkhamd.wordletogether.wordle.domain.WordleGameReducer
import dev.arkhamd.wordletogether.wordle.domain.WordleRoundState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WordleGameController(
    private val scope: CoroutineScope,
    private val gateway: MultiplayerSessionGateway,
    private val wordDictionary: WordDictionary
) {
    private val reducer = WordleGameReducer()
    private val wordValidator = WordValidator(wordDictionary::isValid)

    private val _wordleWords = MutableStateFlow(WordleDefaults.startGameState())
    val wordleWords = _wordleWords.asStateFlow()

    private val _keyboardLetters = MutableStateFlow(WordleDefaults.startKeyboard())
    val keyboardLetters: StateFlow<List<List<Letter>>> = _keyboardLetters.asStateFlow()

    private val _sessionHandle = MutableStateFlow<SessionHandle?>(null)
    val sessionHandle: StateFlow<SessionHandle?> = _sessionHandle.asStateFlow()

    private val _currentWord = MutableStateFlow("")
    val currentWord: StateFlow<String> = _currentWord.asStateFlow()

    private val _lastError = MutableStateFlow<WordleCommandError?>(null)
    val lastError: StateFlow<WordleCommandError?> = _lastError.asStateFlow()

    init {
        restartGame()
    }

    fun send(event: LetterEvent) {
        when (event) {
            is LetterEvent.AddLetter -> addLetter(event.letter, event.isHost)
            is LetterEvent.CheckAnswer -> checkAnswer(event.isHost)
            is LetterEvent.CloseLine -> closeLine()
            is LetterEvent.DeleteLetter -> deleteLetter(event.isHost)
            is LetterEvent.IsDoneSwitch -> clearLoseFlag()
            is LetterEvent.Restart -> restartGame(event.sessionHandle)
            is LetterEvent.SetCurrentWord -> setCurrentWord(event.word)
            is LetterEvent.SetSessionHandle -> setSessionHandle(event.handle)
        }
    }

    fun isValidWord(word: String): Boolean = wordValidator.isValid(word)

    private fun restartGame(handle: SessionHandle? = null) = scope.launch {
        _currentWord.value = wordDictionary.randomWordOrNull().orEmpty()
        if (handle != null) {
            _sessionHandle.value = handle
        }
        applyRoundState(reducer.startRound())
    }

    private fun addLetter(letter: Char, isHost: Boolean? = null) = scope.launch {
        if (isHost != null && !isOnlineRoundReady()) return@launch

        applyRoundState(reducer.addLetter(currentRoundState(), letter))

        if (isHost != null) {
            submitCurrentGrid(WordleCommandOperation.AddLetter)
        }
    }

    private fun deleteLetter(isHost: Boolean? = null) = scope.launch {
        if (isHost != null && !isOnlineRoundReady()) return@launch

        applyRoundState(reducer.deleteLetter(currentRoundState()))

        if (isHost != null) {
            submitCurrentGrid(WordleCommandOperation.DeleteLetter)
        }
    }

    private fun checkAnswer(isHost: Boolean? = null) = scope.launch {
        val result = reducer.submitGuess(
            roundState = currentRoundState(),
            targetWord = currentWord.value,
            isValidWord = wordValidator::isValid
        )
        if (!result.submitted) return@launch

        applyRoundState(result.roundState)

        if (isHost != null) {
            submitCurrentGrid(WordleCommandOperation.CheckAnswer)
        }
    }

    private fun closeLine() = scope.launch {
        applyRoundState(reducer.closeLine(currentRoundState()))
    }

    private fun clearLoseFlag() {
        applyRoundState(reducer.clearLose(currentRoundState()))
    }

    private fun setSessionHandle(handle: SessionHandle) {
        _sessionHandle.value = handle
    }

    private fun setCurrentWord(word: String) {
        _currentWord.value = word
    }

    private suspend fun submitCurrentGrid(operation: WordleCommandOperation) {
        gateway.submitGridIfSessionReady(
            sessionHandle = sessionHandle.value,
            grid = wordleWords.value.tryingWords
        )?.collect {
            when (it) {
                is ResultState.Success -> Unit
                is ResultState.Failure -> recordFailure(operation, it.error)
                ResultState.Loading -> Unit
            }
        }
    }

    private fun isOnlineRoundReady(): Boolean = sessionHandle.value != null && currentWord.value.isNotBlank()

    private fun currentRoundState(): WordleRoundState = WordleRoundState(
        game = wordleWords.value,
        keyboard = keyboardLetters.value
    )

    private fun applyRoundState(roundState: WordleRoundState) {
        _wordleWords.value = roundState.game
        _keyboardLetters.value = roundState.keyboard
    }

    private fun recordFailure(operation: WordleCommandOperation, error: SessionGatewayError) {
        _lastError.value = WordleCommandError(
            operation = operation,
            message = error.message
        )
    }
}

data class WordleCommandError(
    val operation: WordleCommandOperation,
    val message: String
)

enum class WordleCommandOperation {
    AddLetter,
    DeleteLetter,
    CheckAnswer
}

private fun MultiplayerSessionGateway.submitGridIfSessionReady(
    sessionHandle: SessionHandle?,
    grid: List<List<Letter>>
) = if (sessionHandle == null) {
    null
} else {
    submitGrid(
        handle = sessionHandle,
        grid = grid.toSessionGrid()
    )
}
