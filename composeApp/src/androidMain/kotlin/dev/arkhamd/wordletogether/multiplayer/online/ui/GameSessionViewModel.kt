package dev.arkhamd.wordletogether.multiplayer.online.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.arkhamd.wordletogether.profile.domain.PlayerProfileRepository
import dev.arkhamd.wordletogether.session.domain.MultiplayerSessionGateway

class GameSessionViewModel(
    playerProfileRepository: PlayerProfileRepository,
    gateway: MultiplayerSessionGateway
) : ViewModel() {

    private val controller = GameSessionController(
        scope = viewModelScope,
        playerProfileRepository = playerProfileRepository,
        gateway = gateway
    )

    val session = controller.session
    val sessionHandle = controller.sessionHandle
    val sessionsList = controller.sessionsList
    val dataCounts = controller.dataCounts
    val lastError = controller.lastError

    fun send(event: GameSessionEvent) {
        controller.send(event)
    }

    override fun onCleared() {
        controller.clear()
        super.onCleared()
    }

    companion object {
        class Factory(
            private val playerProfileRepository: PlayerProfileRepository,
            private val gameSessionGateway: MultiplayerSessionGateway
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(GameSessionViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return GameSessionViewModel(
                        playerProfileRepository = playerProfileRepository,
                        gateway = gameSessionGateway
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
