package com.example.scrambletogether.ui.viewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.scrambletogether.data.ColorLetter
import com.example.scrambletogether.data.LetterDataClass
import com.example.scrambletogether.data.startWordleWords
import com.google.firebase.firestore.getField
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CurrentState(
    val currentWord: String = "",
    val isWin: Boolean = false
)

class LettersEnemyViewModel(firebaseId: String): ViewModel() {
    private val _wordleWords = MutableStateFlow(startWordleWords.tryingWords)
    var wordleWords: StateFlow<Array<Array<LetterDataClass>>> = _wordleWords.asStateFlow()

    private val _currentState = MutableStateFlow(CurrentState())
    var currentState: StateFlow<CurrentState> = _currentState.asStateFlow()

    fun restartGame() {
        _wordleWords.update {
            startWordleWords.tryingWords
        }
        _currentState.update {
            CurrentState()
        }
    }

    init {
        Firebase.firestore.collectionGroup(firebaseId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed Documents", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val data = snapshot.documents.find {
                        "aboutWordle" in it.reference.path
                    }
                    if (data != null) {
                        if (data.exists()) {
                            _wordleWords.update {
                                // from
                                // 6x5 wordleGrid (L - Letter, C - color)
                                // LCLCLCLCLC|
                                // LCLCLCLCLC|
                                // LCLCLCLCLC|
                                // LCLCLCLCLC|
                                // LCLCLCLCLC|
                                // LCLCLCLCLC|
                                // to
                                // [LC, LC, LC, LC, LC] x6
                                val wordleGrid = data.getField<String>("grid")
                                    ?.split("|")!!.dropLast(1)

                                val newWordleGrid: Array<Array<LetterDataClass>> = Array(it.size) {
                                    Array(startWordleWords.tryingWords[0].size)
                                        { LetterDataClass() }
                                }

                                for (line in wordleGrid.indices) {
                                    var k = 0
                                    for (letterData in 0 until wordleGrid[0].length/2) {
                                        val color = when (wordleGrid[line][letterData*2 + 1]) {
                                            ColorLetter.Right.name[0] -> ColorLetter.Right.color
                                            ColorLetter.Almost.name[0] -> ColorLetter.Almost.color
                                            ColorLetter.Miss.name[0] -> ColorLetter.Miss.color
                                            else -> ColorLetter.None.color
                                        }
                                        val letter = wordleGrid[line][letterData*2]
                                        newWordleGrid[line][letterData] = LetterDataClass(letter, color)

                                        if (color == ColorLetter.Right.color) k += 1
                                    }

                                    // if winner
                                    if (k == 5) {
                                        _currentState.update {
                                            CurrentState(
                                                _currentState.value.currentWord,
                                                true
                                            )
                                        }
                                    }
                                }
                                newWordleGrid
                            }

                            _currentState.update {
                                CurrentState(
                                    data.getField<String>("currentWord")!!,
                                    _currentState.value.isWin
                                )
                            }
                        }
                    }
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        class LettersEnemyViewModelFactory(private val firebaseId: String) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LettersEnemyViewModel::class.java)) {
                    return LettersEnemyViewModel(firebaseId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

}