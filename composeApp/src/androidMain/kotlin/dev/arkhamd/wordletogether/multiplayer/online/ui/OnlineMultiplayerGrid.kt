package dev.arkhamd.wordletogether.multiplayer.online.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.arkhamd.wordletogether.R
import dev.arkhamd.wordletogether.multiplayer.online.domain.evaluateOnlineMultiplayerRoundResult
import dev.arkhamd.wordletogether.multiplayer.ui.DoubleGrid
import dev.arkhamd.wordletogether.session.domain.SessionExtra
import dev.arkhamd.wordletogether.session.domain.toWordleGrid
import dev.arkhamd.wordletogether.wordle.domain.PlayerState
import dev.arkhamd.wordletogether.wordle.domain.WordleGameState

@Composable
fun MultiScreenTwoDevices(
    modifier: Modifier = Modifier,
    yourState: WordleGameState,
    session: SessionExtra,
    onTargetWordChanged: (String) -> Unit,
    endGame: (PlayerState) -> Unit
) {
    val playerResult = evaluateOnlineMultiplayerRoundResult(
        yourState = yourState,
        session = session
    )

    LaunchedEffect(session.selfWord) {
        onTargetWordChanged(session.selfWord)
    }

    LaunchedEffect(playerResult) {
        playerResult?.let(endGame)
    }

    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.server_name) + session.sessionId,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        DoubleGrid(
            yourWords = yourState.tryingWords,
            enemyWords = session.listenGrid.toWordleGrid(),
            enemyCurrentWord = session.enemyWord,
            selfCurrentWord = session.selfWord,
            isWait = session.isWait,
            nameForPlayer1 = stringResource(R.string.you),
            nameForPlayer2 = stringResource(R.string.enemy)
        )
    }
}
