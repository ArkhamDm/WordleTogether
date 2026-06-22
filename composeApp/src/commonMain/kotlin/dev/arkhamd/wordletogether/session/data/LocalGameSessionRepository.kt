package dev.arkhamd.wordletogether.session.data

import dev.arkhamd.wordletogether.session.domain.MultiplayerSessionGateway
import dev.arkhamd.wordletogether.session.domain.ResultState
import dev.arkhamd.wordletogether.session.domain.SessionExtra
import dev.arkhamd.wordletogether.session.domain.SessionGatewayError
import dev.arkhamd.wordletogether.session.domain.SessionHandle
import dev.arkhamd.wordletogether.session.domain.SessionItem
import dev.arkhamd.wordletogether.session.domain.SessionLetter
import dev.arkhamd.wordletogether.session.domain.SessionPlayer
import dev.arkhamd.wordletogether.session.domain.emptySessionGrid
import dev.arkhamd.wordletogether.wordle.domain.LetterFeedback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LocalGameSessionRepository : MultiplayerSessionGateway {

    override fun createSession(sessionItem: SessionItem): Flow<ResultState<SessionHandle>> = flow {
        emit(ResultState.Loading)
        if (sessionItem.id.isBlank()) {
            emit(ResultState.Failure(SessionGatewayError.InvalidSessionId))
            return@flow
        }

        val result = mutex.withLock {
            if (sessions.containsKey(sessionItem.id)) {
                ResultState.Failure(SessionGatewayError.AlreadyExists(sessionItem.id))
            } else {
                sessions[sessionItem.id] = MutableStateFlow(LocalSessionState.from(sessionItem))
                ResultState.Success(SessionHandle(sessionItem.id, SessionPlayer.Host))
            }
        }
        emit(result)
    }

    override fun listOpenSessions(): Flow<ResultState<List<SessionItem>>> = flow {
        emit(ResultState.Loading)
        val waitingSessions = mutex.withLock {
            sessions.values
                .map { it.value }
                .filter { it.isWait }
                .map { it.toSessionItem() }
        }

        emit(ResultState.Success(waitingSessions))
    }

    override fun submitGrid(
        handle: SessionHandle,
        grid: List<List<SessionLetter>>
    ): Flow<ResultState<String>> = updateSession(handle.sessionId) {
        if (handle.player == SessionPlayer.Host) {
            copy(hostGrid = grid.copyGrid())
        } else {
            copy(guestGrid = grid.copyGrid())
        }
    }

    override fun setSecretWord(
        handle: SessionHandle,
        word: String
    ): Flow<ResultState<String>> = updateSession(handle.sessionId) {
        if (handle.player == SessionPlayer.Host) {
            copy(guestCurrentWord = word)
        } else {
            copy(hostCurrentWord = word)
        }
    }

    override fun joinSession(sessionId: String): Flow<ResultState<SessionHandle>> = flow {
        emit(ResultState.Loading)
        if (sessionId.isBlank()) {
            emit(ResultState.Failure(SessionGatewayError.InvalidSessionId))
            return@flow
        }

        val result = mutex.withLock {
            val session = sessions[sessionId]
            if (session == null) {
                ResultState.Failure(SessionGatewayError.NotFound(sessionId))
            } else {
                val current = session.value
                if (!current.isWait) {
                    ResultState.Failure(SessionGatewayError.Full(sessionId))
                } else {
                    session.value = current.copy(isWait = false)
                    ResultState.Success(SessionHandle(sessionId, SessionPlayer.Guest))
                }
            }
        }
        emit(result)
    }

    override fun disconnect(handle: SessionHandle): Flow<ResultState<String>> = updateSession(handle.sessionId) {
        copy(isWait = handle.player != SessionPlayer.Host)
    }

    override fun resetRound(handle: SessionHandle): Flow<ResultState<String>> = updateSession(handle.sessionId) {
        if (handle.player == SessionPlayer.Host) {
            copy(hostGrid = emptyGrid(), guestCurrentWord = "")
        } else {
            copy(guestGrid = emptyGrid(), hostCurrentWord = "")
        }
    }

    override fun observeSession(handle: SessionHandle): Flow<ResultState<SessionExtra>> = flow {
        val sessionId = handle.sessionId
        val session = mutex.withLock { sessions[sessionId] }
        if (session == null) {
            emit(ResultState.Failure(SessionGatewayError.NotFound(sessionId)))
        } else {
            emitAll(session.map { ResultState.Success(it.toSessionExtra(handle)) })
        }
    }

    private fun updateSession(
        sessionId: String,
        transform: LocalSessionState.() -> LocalSessionState
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        val result = mutex.withLock {
            val session = sessions[sessionId]
            if (session == null) {
                ResultState.Failure(SessionGatewayError.NotFound(sessionId))
            } else {
                session.value = session.value.transform()
                ResultState.Success("updated local session: $sessionId")
            }
        }
        emit(result)
    }

    private data class LocalSessionState(
        val id: String,
        val winTotal: Int,
        val loseTotal: Int,
        val drawTotal: Int,
        val gamemode: String,
        val hostGrid: List<List<SessionLetter>>,
        val guestGrid: List<List<SessionLetter>>,
        val hostCurrentWord: String = "",
        val guestCurrentWord: String = "",
        val isWait: Boolean = true
    ) {
        fun toSessionItem(): SessionItem = SessionItem(
            id = id,
            winTotal = winTotal,
            loseTotal = loseTotal,
            drawTotal = drawTotal,
            gamemode = gamemode
        )

        fun toSessionExtra(handle: SessionHandle): SessionExtra {
            val isHost = handle.player == SessionPlayer.Host
            val enemyGrid = if (isHost) guestGrid else hostGrid
            val selfWord = if (isHost) hostCurrentWord else guestCurrentWord
            val enemyWord = if (isHost) guestCurrentWord else hostCurrentWord
            val gridSize = enemyGrid.size
            val wordSize = enemyGrid[0].size
            val isWin = enemyGrid.any { line ->
                line.count { letter -> letter.feedback == LetterFeedback.Right } == wordSize
            }
            val isLose = enemyGrid[gridSize - 1].count {
                it.letter != ' ' && it.feedback != LetterFeedback.None
            } == wordSize

            return SessionExtra(
                listenGrid = enemyGrid.copyGrid(),
                enemyWord = enemyWord,
                selfWord = selfWord,
                isWin = isWin,
                isLose = isLose,
                sessionId = handle.sessionId,
                isWait = isWait,
                isHost = isHost
            )
        }

        companion object {
            fun from(sessionItem: SessionItem): LocalSessionState = LocalSessionState(
                id = sessionItem.id,
                winTotal = sessionItem.winTotal,
                loseTotal = sessionItem.loseTotal,
                drawTotal = sessionItem.drawTotal,
                gamemode = sessionItem.gamemode,
                hostGrid = emptyGrid(),
                guestGrid = emptyGrid()
            )
        }
    }

    companion object {
        private val mutex = Mutex()
        private val sessions = mutableMapOf<String, MutableStateFlow<LocalSessionState>>()

        private fun emptyGrid(): List<List<SessionLetter>> = emptySessionGrid()

        private fun List<List<SessionLetter>>.copyGrid(): List<List<SessionLetter>> = map { line ->
            line.map { it.copy() }
        }
    }
}
