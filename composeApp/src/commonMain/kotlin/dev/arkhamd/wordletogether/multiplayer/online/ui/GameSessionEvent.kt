package dev.arkhamd.wordletogether.multiplayer.online.ui

import dev.arkhamd.wordletogether.session.domain.SessionItem
import dev.arkhamd.wordletogether.wordle.domain.PlayerState

sealed class GameSessionEvent {
    object Reset : GameSessionEvent()
    object DisconnectFromSession : GameSessionEvent()
    object GetSessions : GameSessionEvent()
    object IsDoneSwitch : GameSessionEvent()
    class RecordResult(val playerState: PlayerState) : GameSessionEvent()
    class UpdateWord(val word: String) : GameSessionEvent()
    class CreateSession(val sessionItem: SessionItem) : GameSessionEvent()
    class ConnectToSession(val id: String) : GameSessionEvent()
}
