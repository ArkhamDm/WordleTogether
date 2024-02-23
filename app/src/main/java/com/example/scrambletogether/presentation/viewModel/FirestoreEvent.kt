package com.example.scrambletogether.presentation.viewModel

import com.example.scrambletogether.domain.model.SessionItem


sealed class FirestoreEvent {
    object Reset: FirestoreEvent()
    object DisconnectFromSession: FirestoreEvent()
    object IncWinCount: FirestoreEvent()
    object IncDrawCount: FirestoreEvent()
    object IncLoseCount: FirestoreEvent()
    object GetSessions : FirestoreEvent()
    object IsDoneSwitch: FirestoreEvent()
    class UpdateWord(val word: String): FirestoreEvent()
    class CreateSession(val sessionItem: SessionItem): FirestoreEvent()
    class ConnectToSession(val id: String): FirestoreEvent()
}
