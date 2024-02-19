package com.example.scrambletogether.ui.viewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrambletogether.data.ColorLetter
import com.example.scrambletogether.data.Letter
import com.example.scrambletogether.data.LettersViewModelDataClass
import com.example.scrambletogether.data.startKeyboard
import com.example.scrambletogether.data.startWordleWords
import com.example.scrambletogether.data.words
import com.example.scrambletogether.firestore.repository.FirestoreRepository
import com.example.scrambletogether.firestore.repository.FirestoreRepositoryImpl
import com.example.scrambletogether.utils.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LettersViewModel : ViewModel() {
    private val _wordleWords = MutableStateFlow(startWordleWords)
    private val _keyboardLetters = MutableStateFlow(startKeyboard)

    var wordleWords: StateFlow<LettersViewModelDataClass> = _wordleWords.asStateFlow()
    var keyboardLetters: StateFlow<Array<Array<Letter>>> = _keyboardLetters.asStateFlow()

    private val repo: FirestoreRepository = FirestoreRepositoryImpl()

    lateinit var firebaseId: String
    var currentWord: String? = null

    fun restartGame(id: String? = null) = viewModelScope.launch {
        currentWord = words.subList(0, 819).random()
        if (id != null) firebaseId = id

        _wordleWords.update {
            startWordleWords
        }
        _keyboardLetters.update {
            startKeyboard
        }

    }


    init {
        restartGame()
    }

    fun addLetter(letter: Char, isHost: Boolean? = null) = viewModelScope.launch {
        _wordleWords.update { currentState ->
            val newTryingWords = currentState.tryingWords.map { it.copyOf() }.toTypedArray()
            var madeFullWordInLine = false

            val amountOfWords = currentState.tryingWords.size

            if (!currentState.madeWordInLine && currentState.wordsInLine < amountOfWords) {

                val indexOfSpace = newTryingWords[currentState.wordsInLine].indexOf(
                    Letter()
                )
                if (indexOfSpace >= 0) {
                    newTryingWords[currentState.wordsInLine][indexOfSpace] =
                        Letter(letter = letter)
                    if (indexOfSpace == 4) {
                        madeFullWordInLine = true
                    }
                }
            }

            currentState.copy(
                tryingWords = newTryingWords,
                madeWordInLine =
                if (madeFullWordInLine) madeFullWordInLine else currentState.madeWordInLine,
                wordsInLine = currentState.wordsInLine,
                isLose = currentState.isLose
            )
        }

        if (isHost != null) {
            repo.updateGrid(firebaseId, wordleWords.value.tryingWords, isHost).collect {
                when (it) {
                    is ResultState.Success -> {
                        Log.d(TAG, it.data)
                    }

                    is ResultState.Failure -> {
                        Log.e(TAG, "fail addLetter $firebaseId", it.msg)
                    }

                    ResultState.Loading -> {}
                }
            }
        }
    }

    fun deleteLetter(isHost: Boolean? = null) = viewModelScope.launch {
        _wordleWords.update { currentState ->
            val newTryingWords = currentState.tryingWords.map { it.copyOf() }.toTypedArray()

            val indexOfChar = newTryingWords[currentState.wordsInLine].indexOf(
                Letter()
            ) - 1
            if (indexOfChar >= 0) {
                newTryingWords[currentState.wordsInLine][indexOfChar] = Letter()
            } else if (indexOfChar == -2) {
                newTryingWords[currentState.wordsInLine][4] = Letter()
            }

            currentState.copy(
                tryingWords = newTryingWords,
                madeWordInLine = false,
                wordsInLine = currentState.wordsInLine,
                isLose = currentState.isLose
            )
        }

        if (isHost != null) {
            repo.updateGrid(firebaseId, wordleWords.value.tryingWords, isHost).collect {
                when (it) {
                    is ResultState.Success -> {
                        Log.d(TAG, it.data)
                    }

                    is ResultState.Failure -> {
                        Log.e(TAG, "fail deleteLetter $firebaseId", it.msg)
                    }

                    ResultState.Loading -> {}
                }
            }
        }
    }

    fun checkAnswer(isHost: Boolean? = null) = viewModelScope.launch {
        val amountOfWords = _wordleWords.value.tryingWords.size
        if (_wordleWords.value.madeWordInLine && _wordleWords.value.wordsInLine < amountOfWords) {
            var answerString = ""
            for (letter in _wordleWords.value.tryingWords[_wordleWords.value.wordsInLine]) {
                answerString = answerString.plus(letter.letter)
            }

            if (answerString in words) {
                _wordleWords.update { currentState ->
                    val newTryingWords =
                        currentState.tryingWords.map { it.copyOf() }.toTypedArray()
                    val answer = currentState.tryingWords[currentState.wordsInLine]

                    for (i in answer.indices) {
                        val color = when (answer[i].letter) {
                            currentWord!![i] -> {
                                ColorLetter.Right.color
                            }

                            in currentWord!! -> {
                                ColorLetter.Almost.color
                            }

                            else -> {
                                ColorLetter.Miss.color
                            }
                        }
                        answer[i] = Letter(answer[i].letter, color)
                        updateKeyboard(answer[i].letter, color)
                    }

                    val isWin = (answer.count { it.color == ColorLetter.Right.color } == 5)
                    val isLose = (currentState.wordsInLine.inc() == amountOfWords)

                    newTryingWords[currentState.wordsInLine] = answer

                    currentState.copy(
                        tryingWords = newTryingWords,
                        wordsInLine =
                        if (isLose or isWin)
                            currentState.wordsInLine
                        else currentState.wordsInLine.inc(),
                        madeWordInLine = false,
                        isWin = isWin,
                        isLose = isLose
                    )
                }

                if (isHost != null) {
                    repo.updateGrid(firebaseId, wordleWords.value.tryingWords, isHost).collect {
                        when (it) {
                            is ResultState.Success -> {
                                Log.d(TAG, it.data)
                            }

                            is ResultState.Failure -> {
                                Log.e(TAG, "fail checkAnswer $firebaseId", it.msg)
                            }

                            ResultState.Loading -> {}
                        }
                    }
                }
            }
        }
    }

    fun closeLine() = viewModelScope.launch {
        val amountOfWords = _wordleWords.value.tryingWords.size
        _wordleWords.update { currentState ->
            val newTryingWords =
                currentState.tryingWords.map { it.copyOf() }.toTypedArray()
            newTryingWords[currentState.wordsInLine] = arrayOf(
                Letter(' ', ColorLetter.Miss.color),
                Letter(' ', ColorLetter.Miss.color),
                Letter(' ', ColorLetter.Miss.color),
                Letter(' ', ColorLetter.Miss.color),
                Letter(' ', ColorLetter.Miss.color)
            )
            val isLose = (currentState.wordsInLine.inc() == amountOfWords)

            currentState.copy(
                tryingWords = newTryingWords,
                wordsInLine =
                if (isLose)
                    currentState.wordsInLine
                else currentState.wordsInLine.inc(),
                madeWordInLine = false,
                isWin = currentState.isWin,
                isLose = isLose
            )
        }
    }

    fun isDoneSwitch() {
        _wordleWords.update {
            it.copy(
                isLose = false
            )
        }
    }


    private fun updateKeyboard(letter: Char, color: Color) = viewModelScope.launch {
        _keyboardLetters.update { currentState ->
            val newKeyboardLetter = currentState.map { it.copyOf() }.toTypedArray()
            for (line in newKeyboardLetter.indices) {
                var indexOfChar = newKeyboardLetter[line].indexOf(Letter(letter = letter))

                //сhange from yellow to green
                if (indexOfChar == -1) {
                    indexOfChar = newKeyboardLetter[line].indexOf(
                        Letter(letter = letter, color = ColorLetter.Almost.color)
                    )
                }

                if (indexOfChar >= 0) {
                    newKeyboardLetter[line][indexOfChar] = Letter(letter, color)
                    break
                }
            }

            newKeyboardLetter
        }
    }

}