package com.example.scrambletogether.firestore.repository

import com.example.scrambletogether.data.Letter
import com.example.scrambletogether.firestore.data.SessionItem
import com.example.scrambletogether.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {

    fun addSession(sessionItem: SessionItem) : Flow<ResultState<String>>
    fun getAllSessions(): Flow<ResultState<List<SessionItem>>>
    fun updateGrid(idSession:String, grid: Array<Array<Letter>>, isHost: Boolean): Flow<ResultState<String>>
    fun updateWord(idSession: String, word: String, isHost: Boolean): Flow<ResultState<String>>
    fun connect(idSession: String): Flow<ResultState<String>>
    fun disconnect(idSession: String, isHost: Boolean): Flow<ResultState<String>>
    fun reset(idSession: String, isHost: Boolean): Flow<ResultState<String>>

}