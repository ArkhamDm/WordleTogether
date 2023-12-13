package com.example.scrambletogether.ui.viewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.scrambletogether.data.LetterDataClass
import com.example.scrambletogether.data.startWordleWords
import com.example.scrambletogether.ui.theme.invisibleGray
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LettersEnemyViewModel(firebaseId: String): ViewModel() {
    private val _wordleWords = MutableStateFlow(startWordleWords.tryingWords)
    var wordleWords: StateFlow<Array<Array<LetterDataClass>>> = _wordleWords.asStateFlow()

    private val _currentState = MutableStateFlow<Array<Any>>(arrayOf("", false))
    var currentState: StateFlow<Array<Any>> = _currentState.asStateFlow()

    fun restartGame() {
        _wordleWords.update {
            startWordleWords.tryingWords
        }
        _currentState.update {
            arrayOf(
                "",
                false
            )
        }
    }

    init {
        Firebase.firestore.collectionGroup("letters")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed Documents", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val data = snapshot.documents.filter {
                        firebaseId in it.reference.path
                    }
                    if (data.isNotEmpty()) {
                        _wordleWords.update {
                            val newWordleGrid: Array<Array<LetterDataClass>> = Array(it.size) {
                                Array(5) { LetterDataClass() }
                            }
                            for (i in it.indices) {
                                var k = 0
                                for (j in it[i].indices) {
                                    val letterChar: Char =
                                        data[i * 5 + j].data?.get("letterChar").toString()[0]
                                    val color = when (data[i * 5 + j].data?.get("color")) {
                                        "invisibleGray" -> invisibleGray
                                        "green" -> Color.Green
                                        "yellow" -> Color.Yellow
                                        "gray" -> Color.Gray
                                        else -> Color.Red
                                    }

                                    if (color == Color.Green) k++

                                    newWordleGrid[i][j] = LetterDataClass(letterChar, color)
                                }

                                if (k == 5) {
                                    _currentState.update {
                                        arrayOf(
                                            _currentState.value[0],
                                            true
                                        )
                                    }
                                }
                            }
                            newWordleGrid
                        }
                    }
                }
            }

        Firebase.firestore.collectionGroup(firebaseId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed Word", e)
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    Log.d(TAG, "No such document")
                } else {
                    val data = snapshot.documents.find {
                        firebaseId in it.reference.path
                    }
                    _currentState.update {
                        arrayOf(
                            data?.data?.get("currentWord").toString(),
                            false
                        )
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