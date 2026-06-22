package dev.arkhamd.wordletogether.multiplayer.online.ui

import dev.arkhamd.wordletogether.session.domain.SessionItem

data class SessionsList(
    val sessions: List<SessionItem> = emptyList(),
    val isLoading: Boolean = true
)
