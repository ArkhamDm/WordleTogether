package com.example.scrambletogether.utils

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.scrambletogether.data.LetterDataClass
import com.example.scrambletogether.data.startWordleWords
import com.example.scrambletogether.ui.theme.invisibleGray
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseUtils {
    private var randomId: String? = null

    private val readyToGame = mapOf("state" to "ready")
    private val inGame = mapOf("state" to "inGame")


    fun create() : String {
        if (randomId == null) {
            randomId = (1..6)
                .map {(('A'..'Z') + (0..9) - 'I' - 'J').random()}
                .joinToString("")
        }

        val wordleGrid = Firebase.firestore.collection(randomId!!)
        wordleGrid.document("aboutWord").set(mapOf("currentWord" to ""))
        wordleGrid.document("status").set(readyToGame)

        val startWords = startWordleWords.tryingWords

        for (line in startWords.indices) {
            val lettersCollection = wordleGrid
                .document(line.inc().toString() + " line").collection("letters")
            for (letter in 0..4) {
                lettersCollection.document(letter.inc().toString()).set(
                    mapOf(
                        "letterChar" to " ",
                        "color" to "invisibleGray"
                    )
                )
            }
        }

        return (wordleGrid.parent?.id ?: randomId)!!
    }

    fun update(
        newTryingWords: Array<Array<LetterDataClass>>,
        firebaseId: String
    ) {
        val wordleToList = newTryingWords.map { it.toList() }
        val wordleGrid = Firebase.firestore.collection(firebaseId)

        for (line in wordleToList.indices) {
            val lettersCollection = wordleGrid.document(line.inc().toString() + " line").collection("letters")
            for (_letter in 0..4) {
                val letterData = wordleToList[line][_letter]

                val letterChar = letterData.letter.toString()
                val color = when (letterData.color) {
                    invisibleGray -> {"invisibleGray"}
                    Color.Green -> {"green"}
                    Color.Yellow -> {"yellow"}
                    Color.Gray -> {"gray"}
                    else -> {"undefined"}
                }

                lettersCollection.document(_letter.inc().toString()).set(
                    mapOf(
                        "letterChar" to letterChar,
                        "color" to color
                    )
                )
            }
        }
    }

    fun delete(firebaseId: String) {
        Firebase.firestore.collection("users").document(firebaseId).delete()
            .addOnSuccessListener {
                Log.d(TAG, "$firebaseId deleted")
            }
            .addOnFailureListener {
                    e -> Log.w(TAG, "Error to delete $firebaseId", e)
            }
        //TODO nothing deletes
    }

    fun setWord(word: String, firebaseId: String) {
        val wordleGrid = Firebase.firestore.collection(firebaseId)
        wordleGrid.document("aboutWord").set(mapOf(
            "currentWord" to word
        ))
        wordleGrid.document("status").set(inGame)
    }

    fun setStatusInGame(inGameFlag: Boolean, firebaseId: String) {
        val wordleGrid = Firebase.firestore.collection(firebaseId)
        wordleGrid.document("status").set( if (inGameFlag) inGame else readyToGame )
    }

    fun isExistAndFree(firebaseId: String, callback: (Boolean) -> Unit) {
        if (firebaseId.isEmpty()) callback(false)
        else {
            Firebase.firestore.collection(firebaseId).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(
                            task.result?.isEmpty == false &&
                                    task.result.documents[1].data?.get("state") == "ready"
                        )
                    } else {
                        callback(false)
                    }
                }
        }
    }
}