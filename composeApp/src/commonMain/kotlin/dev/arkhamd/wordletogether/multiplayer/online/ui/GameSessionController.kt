package dev.arkhamd.wordletogether.multiplayer.online.ui

import dev.arkhamd.wordletogether.profile.domain.IncWinLoseDrawUseCase
import dev.arkhamd.wordletogether.profile.domain.PlayerProfileRepository
import dev.arkhamd.wordletogether.profile.domain.PlayerStats
import dev.arkhamd.wordletogether.session.domain.MultiplayerSessionGateway
import dev.arkhamd.wordletogether.session.domain.ResultState
import dev.arkhamd.wordletogether.session.domain.SessionExtra
import dev.arkhamd.wordletogether.session.domain.SessionGatewayError
import dev.arkhamd.wordletogether.session.domain.SessionHandle
import dev.arkhamd.wordletogether.session.domain.SessionItem
import dev.arkhamd.wordletogether.wordle.domain.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameSessionController(
    private val scope: CoroutineScope,
    playerProfileRepository: PlayerProfileRepository,
    private val gateway: MultiplayerSessionGateway
) {
    private val profileCounter = IncWinLoseDrawUseCase(playerProfileRepository)

    private val _session = MutableStateFlow(SessionExtra())
    val session: StateFlow<SessionExtra> = _session.asStateFlow()

    private val _sessionHandle = MutableStateFlow<SessionHandle?>(null)
    val sessionHandle: StateFlow<SessionHandle?> = _sessionHandle.asStateFlow()

    private val _sessionsList = MutableStateFlow(SessionsList())
    val sessionsList: StateFlow<SessionsList> = _sessionsList.asStateFlow()

    private val _dataCounts = MutableStateFlow(PlayerStats())
    val dataCounts: StateFlow<PlayerStats> = _dataCounts.asStateFlow()

    private val _lastError = MutableStateFlow<GameSessionError?>(null)
    val lastError: StateFlow<GameSessionError?> = _lastError.asStateFlow()

    private var listenerJob: Job? = null

    init {
        scope.launch {
            playerProfileRepository.stats.collect {
                _dataCounts.value = it
            }
        }
    }

    fun send(event: GameSessionEvent) {
        when (event) {
            is GameSessionEvent.ConnectToSession -> connectToSession(event.id)
            is GameSessionEvent.CreateSession -> create(event.sessionItem)
            is GameSessionEvent.DisconnectFromSession -> disconnectFromSession()
            is GameSessionEvent.GetSessions -> getSessions()
            is GameSessionEvent.IsDoneSwitch -> clearWinFlag()
            is GameSessionEvent.RecordResult -> recordResult(event.playerState)
            is GameSessionEvent.Reset -> reset()
            is GameSessionEvent.UpdateWord -> updateWord(event.word)
        }
    }

    fun clear() {
        listenerJob?.cancel()
        listenerJob = null
    }

    private fun incWinCount() = scope.launch {
        runCatching { profileCounter.win() }
            .onFailure { recordProfileFailure(GameSessionOperation.IncrementWinCount, it) }
    }

    private fun incLoseCount() = scope.launch {
        runCatching { profileCounter.lose() }
            .onFailure { recordProfileFailure(GameSessionOperation.IncrementLoseCount, it) }
    }

    private fun incDrawCount() = scope.launch {
        runCatching { profileCounter.draw() }
            .onFailure { recordProfileFailure(GameSessionOperation.IncrementDrawCount, it) }
    }

    private fun recordResult(playerState: PlayerState) {
        when (playerState) {
            PlayerState.WIN -> incWinCount()
            PlayerState.LOSE -> incLoseCount()
            PlayerState.DRAW -> incDrawCount()
        }
    }

    private fun reset() = scope.launch {
        val handle = sessionHandle.value ?: return@launch
        gateway.resetRound(handle).collect {
            when (it) {
                is ResultState.Success -> Unit
                is ResultState.Failure -> recordFailure(GameSessionOperation.Reset, it.error)
                ResultState.Loading -> Unit
            }
        }
    }

    private fun updateWord(word: String) = scope.launch {
        val handle = sessionHandle.value ?: return@launch
        gateway.setSecretWord(handle, word).collect {
            when (it) {
                is ResultState.Success -> Unit
                is ResultState.Failure -> recordFailure(GameSessionOperation.UpdateWord, it.error)
                ResultState.Loading -> Unit
            }
        }
    }

    private fun create(sessionItem: SessionItem) {
        scope.launch {
            gateway.createSession(sessionItem).collect {
                when (it) {
                    is ResultState.Success -> {
                        _sessionHandle.value = it.data
                        _session.value = SessionExtra(
                            sessionId = it.data.sessionId,
                            isHost = true
                        )
                        listenSession(true)
                    }

                    is ResultState.Failure -> recordFailure(GameSessionOperation.CreateSession, it.error)

                    ResultState.Loading -> Unit
                }
            }
        }
    }

    private fun getSessions() = scope.launch {
        gateway.listOpenSessions().collect {
            when (it) {
                is ResultState.Success -> {
                    _sessionsList.value = SessionsList(
                        sessions = it.data,
                        isLoading = false
                    )
                }

                is ResultState.Failure -> recordFailure(GameSessionOperation.ListSessions, it.error)

                ResultState.Loading -> {
                    _sessionsList.value = SessionsList(isLoading = true)
                }
            }
        }
    }

    private fun connectToSession(id: String) = scope.launch {
        gateway.joinSession(id).collect {
            when (it) {
                is ResultState.Success -> {
                    _sessionHandle.value = it.data
                    _session.value = _session.value.copy(
                        sessionId = it.data.sessionId,
                        isHost = false
                    )
                    listenSession(true)
                }

                is ResultState.Failure -> recordFailure(GameSessionOperation.ConnectToSession, it.error)

                ResultState.Loading -> Unit
            }
        }
    }

    private fun disconnectFromSession() = scope.launch {
        val handle = sessionHandle.value ?: return@launch

        gateway.disconnect(handle).collect {
            when (it) {
                is ResultState.Success -> {
                    listenSession(false)
                    _sessionHandle.value = null
                }

                is ResultState.Failure -> recordFailure(GameSessionOperation.DisconnectFromSession, it.error)

                ResultState.Loading -> Unit
            }
        }
    }

    private fun listenSession(isListen: Boolean) {
        if (isListen) {
            val handle = sessionHandle.value ?: return
            listenerJob?.cancel()
            listenerJob = scope.launch {
                gateway.observeSession(handle).collect { result ->
                    when (result) {
                        is ResultState.Success -> {
                            _session.update { result.data }
                        }

                        is ResultState.Failure -> recordFailure(GameSessionOperation.ObserveSession, result.error)

                        ResultState.Loading -> Unit
                    }
                }
            }
        } else {
            listenerJob?.cancel()
            listenerJob = null
        }
    }

    private fun clearWinFlag() {
        _session.update {
            it.copy(isWin = false)
        }
    }

    private fun recordProfileFailure(operation: GameSessionOperation, error: Throwable) {
        _lastError.value = GameSessionError(
            operation = operation,
            message = error.message ?: error.toString()
        )
    }

    private fun recordFailure(operation: GameSessionOperation, error: SessionGatewayError) {
        _lastError.value = GameSessionError(
            operation = operation,
            message = error.message
        )
    }
}

data class GameSessionError(
    val operation: GameSessionOperation,
    val message: String
)

enum class GameSessionOperation {
    CreateSession,
    ListSessions,
    ConnectToSession,
    DisconnectFromSession,
    ObserveSession,
    Reset,
    UpdateWord,
    IncrementWinCount,
    IncrementLoseCount,
    IncrementDrawCount
}
