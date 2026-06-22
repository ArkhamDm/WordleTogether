package dev.arkhamd.wordletogether.multiplayer.local.domain

import dev.arkhamd.wordletogether.wordle.domain.PlayerState
import dev.arkhamd.wordletogether.wordle.domain.WordleGameState

fun evaluateLocalMultiplayerRoundResult(
    firstPlayerState: WordleGameState,
    secondPlayerState: WordleGameState
): PlayerState? = when {
    firstPlayerState.isWin -> PlayerState.WIN
    secondPlayerState.isWin -> PlayerState.LOSE
    firstPlayerState.isLose && secondPlayerState.isLose -> PlayerState.DRAW
    else -> null
}
