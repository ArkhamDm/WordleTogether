package com.example.scrambletogether.ui.viewModels

import android.content.ContentValues
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrambletogether.data.LetterDataClass
import com.example.scrambletogether.data.LettersViewModelDataClass
import com.example.scrambletogether.data.startKeyboard
import com.example.scrambletogether.data.startWordleWords
import com.example.scrambletogether.data.words
import com.example.scrambletogether.utils.FirebaseUtils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LettersViewModel : ViewModel() {
    private val _wordleWords = MutableStateFlow(startWordleWords)
    private val _keyboardLetters = MutableStateFlow(startKeyboard)
    private val _wordFromEnemy = MutableStateFlow(String())

    var wordleWords: StateFlow<LettersViewModelDataClass> = _wordleWords.asStateFlow()
    var keyboardLetters: StateFlow<Array<Array<LetterDataClass>>> = _keyboardLetters.asStateFlow()
    var wordFromEnemy: StateFlow<String> = _wordFromEnemy.asStateFlow()

    var firebaseId: String = ""
    lateinit var currentWord: String
    lateinit var enemyFirebaseId: String
    //TODO lateinit or bylazy

    fun restartGame(firebase: String = "", isMultiplayer: Boolean = false) {
        currentWord = words.subList(0, 819).random()
        firebaseId = firebase 

        _wordleWords.update {
            startWordleWords
        }
        _keyboardLetters.update {
            startKeyboard
        }
        _wordFromEnemy.update {
            ""
        }

        if (isMultiplayer) FirebaseUtils.update(startWordleWords.tryingWords, firebase)
    }

    init {
        restartGame()
    }

    fun addLetter(letter: Char, isMultiplayer: Boolean = false) {
        viewModelScope.launch {
            _wordleWords.update { currentState ->
                val newTryingWords = currentState.tryingWords.map { it.copyOf() }.toTypedArray()
                var madeFullWordInLine = false

                val amountOfWords = currentState.tryingWords.size

                if (!currentState.madeWordInLine && currentState.wordsInLine < amountOfWords) {

                    val indexOfSpace = newTryingWords[currentState.wordsInLine].indexOf(
                        LetterDataClass()
                    )
                    if (indexOfSpace >= 0) {
                        newTryingWords[currentState.wordsInLine][indexOfSpace] =
                            LetterDataClass(letter = letter)
                        if (indexOfSpace == 4) {
                            madeFullWordInLine = true
                        }
                    }
                }

                if (isMultiplayer) FirebaseUtils.update(newTryingWords, firebaseId)

                currentState.copy(
                    tryingWords = newTryingWords,
                    madeWordInLine =
                        if (madeFullWordInLine) madeFullWordInLine else currentState.madeWordInLine,
                    wordsInLine = currentState.wordsInLine,
                    isDone = currentState.isDone
                )
            }
        }
    }

    fun deleteLetter(isMultiplayer: Boolean = false) {
        viewModelScope.launch {
            _wordleWords.update { currentState ->
                val newTryingWords = currentState.tryingWords.map { it.copyOf() }.toTypedArray()

                val indexOfChar = newTryingWords[currentState.wordsInLine].indexOf(
                    LetterDataClass()
                ) - 1
                if (indexOfChar >= 0) {
                    newTryingWords[currentState.wordsInLine][indexOfChar] = LetterDataClass()
                } else if (indexOfChar == -2) {
                    newTryingWords[currentState.wordsInLine][4] = LetterDataClass()
                }

                if (isMultiplayer) FirebaseUtils.update(newTryingWords, firebaseId)

                currentState.copy(
                    tryingWords = newTryingWords,
                    madeWordInLine = false,
                    wordsInLine = currentState.wordsInLine,
                    isDone = currentState.isDone
                )
            }
        }
    }

    fun checkAnswer(isMultiplayer: Boolean) {
        viewModelScope.launch {
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
                            when (answer[i].letter) {
                                currentWord[i] -> {
                                    answer[i] = LetterDataClass(answer[i].letter, Color.Green)
                                    updateKeyboard(answer[i].letter, Color.Green)
                                }
                                in currentWord -> {
                                    answer[i] = LetterDataClass(answer[i].letter, Color.Yellow)
                                    updateKeyboard(answer[i].letter, Color.Yellow)
                                }
                                else -> {
                                    answer[i] = LetterDataClass(answer[i].letter, Color.Gray)
                                    updateKeyboard(answer[i].letter, Color.Gray)
                                }
                            }
                        }

                        val isDone =
                            (answer.count { it.color == Color.Green } == 5) or
                                    (currentState.wordsInLine.inc() == amountOfWords)

                        newTryingWords[currentState.wordsInLine] = answer

                        if (isMultiplayer) FirebaseUtils.update(newTryingWords, firebaseId)

                        currentState.copy(
                            tryingWords = newTryingWords,
                            wordsInLine =
                                if (isDone)
                                    currentState.wordsInLine
                                else currentState.wordsInLine.inc(),
                            madeWordInLine = false,
                            isDone = isDone
                        )
                    }
                }
            }
        }
    }


    private fun updateKeyboard(letter: Char, color: Color) {
        _keyboardLetters.update { currentState ->
            val newKeyboardLetter = currentState.map { it.copyOf() }.toTypedArray()
            for (line in newKeyboardLetter.indices) {
                var indexOfChar = newKeyboardLetter[line].indexOf(LetterDataClass(letter = letter))

                //сhange from yellow to green
                if (indexOfChar == -1) {
                    indexOfChar = newKeyboardLetter[line].indexOf(LetterDataClass(letter = letter, color = Color.Yellow))
                }

                if (indexOfChar >= 0) {
                    newKeyboardLetter[line][indexOfChar] = LetterDataClass(letter, color)
                    break
                }
            }

            newKeyboardLetter
        }
    }

    fun waitWord() {
        Firebase.firestore.collectionGroup(firebaseId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(ContentValues.TAG, "Listen failed Word", e)
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    Log.d(ContentValues.TAG, "No such document")
                } else {
                    val data = snapshot.documents.find {
                        firebaseId in it.reference.path
                    }
                    val wordInBase = data?.data?.get("currentWord").toString()
                    if (wordInBase.length == 5) {
                        _wordFromEnemy.update {
                            wordInBase
                        }
                        currentWord = wordInBase
                    }
                    return@addSnapshotListener
                }
            }
    }
}