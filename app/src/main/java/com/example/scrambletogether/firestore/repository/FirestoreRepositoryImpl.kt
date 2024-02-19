package com.example.scrambletogether.firestore.repository

import com.example.scrambletogether.data.ColorLetter
import com.example.scrambletogether.data.Letter
import com.example.scrambletogether.data.startWordleWords
import com.example.scrambletogether.firestore.data.SessionItem
import com.example.scrambletogether.utils.ResultState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

private const val HOST = "host "
private const val GUEST = "guest "
private const val GRID = "grid"
private const val CURRENT_WORD = "currentWord"
private const val IS_WAIT = "isWait"
private const val GAMEMODE = "gamemode"
private const val WIN_TOTAL = "winTotal"
private const val LOSE_TOTAL = "loseTotal"
private const val DRAW_TOTAL = "drawTotal"

class FirestoreRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore = Firebase.firestore
): FirestoreRepository{

    override fun addSession(sessionItem: SessionItem): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

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

        db.collection("sessions")
            .document(sessionItem.id)
            .set(
                mapOf(
                    HOST + CURRENT_WORD to "",
                    HOST + GRID to wordleGrid,
                    GUEST + CURRENT_WORD to "",
                    GUEST + GRID to wordleGrid,
                    GAMEMODE to sessionItem.gamemode,
                    WIN_TOTAL to sessionItem.winTotal,
                    LOSE_TOTAL to sessionItem.loseTotal,
                    DRAW_TOTAL to sessionItem.drawTotal,
                    IS_WAIT to true,
                )
            )
            .addOnSuccessListener {
                trySend(ResultState.Success(sessionItem.id))
            }
            .addOnFailureListener {
                trySend(ResultState.Failure(it))
            }

        awaitClose {
            close()
        }
    }

    override fun getAllSessions(): Flow<ResultState<List<SessionItem>>> = callbackFlow {
        trySend(ResultState.Loading)
        db.collection("sessions")
            .whereEqualTo(IS_WAIT, true)
            .get()
            .addOnSuccessListener {
                val sessions = it.map { data ->
                    SessionItem(
                        id = data.id,
                        winTotal = data.getField<Int>(WIN_TOTAL) ?: 0,
                        loseTotal = data.getField<Int>(LOSE_TOTAL) ?: 0,
                        drawTotal = data.getField<Int>(DRAW_TOTAL) ?: 0,
                        gamemode = data.getString(GAMEMODE) ?: "???"
                    )
                }
                trySend(ResultState.Success(sessions))
            }
            .addOnFailureListener {
                trySend(ResultState.Failure(it))
            }

        awaitClose {
            close()
        }
    }

    override fun updateGrid(
        idSession: String,
        grid: Array<Array<Letter>>,
        isHost: Boolean
    ): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        var wordleGrid = ""
        for (line in grid) {
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

        val field = (if (isHost) HOST else GUEST) + GRID
        db.collection("sessions")
            .document(idSession)
            .update(field, wordleGrid)
            .addOnSuccessListener {
                trySend(
                    ResultState.Success(
                        "updatedGrid to\n" +
                                wordleGrid
                                    .replace("|", "\n")
                                    .replace(Regex("(.{2})"), "$1 ")
                    )
                )
            }
            .addOnFailureListener {
                trySend(ResultState.Failure(it))
            }

        awaitClose {
            close()
        }
    }

    override fun updateWord(idSession: String, word: String, isHost: Boolean): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        val field = (if (isHost) GUEST else HOST) + CURRENT_WORD
        db.collection("sessions")
            .document(idSession)
            .update(field, word)
            .addOnSuccessListener {
                trySend(ResultState.Success("updated word: $word in $idSession"))
            }
            .addOnFailureListener {
                trySend(ResultState.Failure(it))
            }

        awaitClose {
            close()
        }
    }

    override fun connect(idSession: String): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        db.collection("sessions")
            .document(idSession)
            .update(IS_WAIT, false)
            .addOnSuccessListener {
                trySend(ResultState.Success("connected to $idSession"))
            }
            .addOnFailureListener {
                trySend(ResultState.Failure(it))
            }

        awaitClose {
            close()
        }
    }

    override fun disconnect(idSession: String, isHost: Boolean): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        db.collection("sessions")
            .document(idSession)
            .update(IS_WAIT, !isHost)
            .addOnSuccessListener {
                trySend(ResultState.Success("disconnected host:$isHost from $idSession"))
            }
            .addOnFailureListener {
                trySend(ResultState.Failure(it))
            }

        awaitClose {
            close()
        }
    }

    override fun reset(idSession: String, isHost: Boolean): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        val wordleGrid = Array(startWordleWords.tryingWords.size) {
            Array(startWordleWords.tryingWords[0].size) {" N"}.joinToString("")
        }.joinToString("|", postfix = "|")
        val field = (if (isHost) HOST else GUEST)

        db.collection("sessions")
            .document(idSession)
            .update(field + GRID, wordleGrid, (if (field == HOST) GUEST else HOST) + CURRENT_WORD, "")
            .addOnSuccessListener {
                trySend(ResultState.Success("reset host:$isHost in $idSession"))
            }
            .addOnFailureListener {
                trySend(ResultState.Failure(it))
            }

        awaitClose {
            close()
        }
    }

}