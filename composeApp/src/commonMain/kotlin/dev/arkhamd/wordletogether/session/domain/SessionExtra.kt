package dev.arkhamd.wordletogether.session.domain

data class SessionExtra(
    val listenGrid: List<List<SessionLetter>> = emptySessionGrid(),
    val enemyWord: String = "",
    val selfWord: String = "",
    val isWin: Boolean = false,
    val isLose: Boolean = false,
    val sessionId: String = "",
    val isWait: Boolean = true,
    val isHost: Boolean = false
)

fun emptySessionGrid(): List<List<SessionLetter>> = List(6) { List(5) { SessionLetter() } }
