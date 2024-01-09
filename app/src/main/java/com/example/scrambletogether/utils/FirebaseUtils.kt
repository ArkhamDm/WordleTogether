package com.example.scrambletogether.utils

import android.util.Log
import com.example.scrambletogether.data.ColorLetter
import com.example.scrambletogether.data.LetterDataClass
import com.example.scrambletogether.data.startWordleWords
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "FirebaseUtils"
private const val ABOUT_WORDLE = "aboutWordle"
private const val STATUS = "status"
private const val GRID = "grid"
private const val CURRENT_WORD = "currentWord"

object FirebaseUtils {
    private var randomId: String? = null

    private val readyToGame = mapOf("state" to "ready")
    private val inGame = mapOf("state" to "inGame")

    fun create() : String {
        // id for firebase collection
        if (randomId == null) {
            randomId = (1..6)
                .map {(('A'..'Z') + (0..9) - 'I' - 'J').random()}
                .joinToString("")
        }

        val wordleRef = Firebase.firestore.collection(randomId!!)

        // 6x5 wordleGrid (L - Letter, C - color)
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        val wordleGrid = Array(startWordleWords.tryingWords.size) {
            Array(startWordleWords.tryingWords[0].size) {" N"}.joinToString("")
        }.joinToString("|", postfix = "|")

        wordleRef.document(ABOUT_WORDLE).set(
            mapOf(
                CURRENT_WORD to "",
                GRID to wordleGrid
            )
        )
        wordleRef.document(STATUS).set(readyToGame)

        return (wordleRef.parent?.id ?: randomId)!!
    }

    fun reset(firebaseId: String? = randomId) {
        if (firebaseId == null) {
            logFirebaseError("reset")
            return
        }

        val wordleRef = Firebase.firestore.collection(randomId!!)
        // 6x5 wordleGrid (L - Letter, C - color)
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        val wordleGrid = Array(startWordleWords.tryingWords.size) {
            Array(startWordleWords.tryingWords[0].size) {" N"}.joinToString("")
        }.joinToString("|", postfix = "|")

        wordleRef.document(ABOUT_WORDLE).update(GRID, wordleGrid)
    }

    fun update(
        newTryingWords: Array<Array<LetterDataClass>>,
        firebaseId: String? = randomId
    ) {
        if (firebaseId == null) {
            logFirebaseError("update")
            return
        }

        val wordleRef = Firebase.firestore.collection(firebaseId)
        var wordleGrid = ""
        for (line in newTryingWords) {
            for (letter in line) {
                val color = when (letter.color) {
                    ColorLetter.Right.color -> {
                        ColorLetter.Right.name[0]
                    }
                    ColorLetter.Almost.color -> {
                        ColorLetter.Almost.name[0]
                    }
                    ColorLetter.Miss.color -> {
                        ColorLetter.Miss.name[0]
                    }
                    else -> ColorLetter.None.name[0]
                }
                wordleGrid += "${letter.letter}" + color
            }
            wordleGrid += "|"
        }

        Log.d(TAG,
            "updatedGrid to\n" + wordleGrid
                .replace("|", "\n").replace(Regex("(.{2})"), "$1 ")
        )

        wordleRef.document(ABOUT_WORDLE).update(GRID, wordleGrid)
    }

    fun delete(firebaseId: String? = randomId) {
        if (firebaseId == null) {
            logFirebaseError("delete")
            return
        }

        Firebase.firestore.collection("users").document(firebaseId).delete()
            .addOnSuccessListener {
                Log.d(TAG, "$firebaseId deleted")
            }
            .addOnFailureListener {
                    e -> Log.w(TAG, "Error to delete $firebaseId", e)
            }
        //TODO nothing deletes
    }

    fun setWord(word: String, firebaseId: String? = randomId) {
        if (firebaseId == null) {
            logFirebaseError("setWord")
            return
        }

        val wordleRef = Firebase.firestore.collection(firebaseId)
        wordleRef.document(ABOUT_WORDLE).update(CURRENT_WORD, word)
        Log.d(TAG, "Word $word set in $firebaseId")
        wordleRef.document(STATUS).update(inGame)
    }

    fun setStatus(inGameFlag: Boolean, firebaseId: String? = randomId) {
        if (firebaseId == null) {
            logFirebaseError("setStatus")
            return
        }

        val wordleRef = Firebase.firestore.collection(firebaseId)
        wordleRef.document(STATUS).update( if (inGameFlag) inGame else readyToGame )
    }

    fun isExistAndFree(firebaseId: String, callback: (Boolean) -> Unit) {
        if (firebaseId.isEmpty()) callback(false)
        else {
            Firebase.firestore.collection(firebaseId).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(
                            task.result?.isEmpty == false &&
                                    task.result.documents[1].data?.equals(readyToGame) == true
                        )
                    } else {
                        callback(false)
                    }
                }
        }
    }
}

fun logFirebaseError(funcName: String) = Log.e(TAG, "FirebaseId isn`t declared /${funcName}")