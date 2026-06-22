package dev.arkhamd.wordletogether.wordle.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.arkhamd.wordletogether.session.domain.MultiplayerSessionGateway
import dev.arkhamd.wordletogether.wordle.domain.WordDictionary

class LettersViewModel(
    gateway: MultiplayerSessionGateway,
    wordDictionary: WordDictionary
) : ViewModel() {
    private val controller = WordleGameController(
        scope = viewModelScope,
        gateway = gateway,
        wordDictionary = wordDictionary
    )

    val wordleWords = controller.wordleWords
    val keyboardLetters = controller.keyboardLetters
    val sessionHandle = controller.sessionHandle
    val lastError = controller.lastError
    val currentWord = controller.currentWord

    fun send(event: LetterEvent) {
        controller.send(event)
    }

    fun isValidWord(word: String): Boolean = controller.isValidWord(word)

    companion object {
        class Factory(
            private val gameSessionGateway: MultiplayerSessionGateway,
            private val wordDictionary: WordDictionary
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LettersViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return LettersViewModel(
                        gateway = gameSessionGateway,
                        wordDictionary = wordDictionary
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
