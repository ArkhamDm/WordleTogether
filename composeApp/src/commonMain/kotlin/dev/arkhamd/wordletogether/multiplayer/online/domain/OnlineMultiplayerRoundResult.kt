package dev.arkhamd.wordletogether.multiplayer.online.domain

import dev.arkhamd.wordletogether.session.domain.SessionExtra
import dev.arkhamd.wordletogether.wordle.domain.PlayerState
import dev.arkhamd.wordletogether.wordle.domain.WordleGameState

fun evaluateOnlineMultiplayerRoundResult(
    yourState: WordleGameState,
    session: SessionExtra
): PlayerState? = when {
    yourState.isWin -> PlayerState.WIN
    session.isWin -> PlayerState.LOSE
    yourState.isLose && session.isLose -> PlayerState.DRAW
    else -> null
}
