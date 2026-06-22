package dev.arkhamd.wordletogether.session.domain

import kotlinx.coroutines.flow.Flow

interface MultiplayerSessionGateway {
    fun createSession(sessionItem: SessionItem): Flow<ResultState<SessionHandle>>
    fun listOpenSessions(): Flow<ResultState<List<SessionItem>>>
    fun submitGrid(
        handle: SessionHandle,
        grid: List<List<SessionLetter>>
    ): Flow<ResultState<String>>
    fun setSecretWord(handle: SessionHandle, word: String): Flow<ResultState<String>>
    fun joinSession(sessionId: String): Flow<ResultState<SessionHandle>>
    fun disconnect(handle: SessionHandle): Flow<ResultState<String>>
    fun resetRound(handle: SessionHandle): Flow<ResultState<String>>
    fun observeSession(handle: SessionHandle): Flow<ResultState<SessionExtra>>
}

data class SessionHandle(
    val sessionId: String,
    val player: SessionPlayer
)

enum class SessionPlayer {
    Host,
    Guest
}
