package dev.arkhamd.wordletogether.multiplayer.online.ui.dialogs.extras

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arkhamd.wordletogether.R
import dev.arkhamd.wordletogether.profile.domain.PlayerStats
import dev.arkhamd.wordletogether.session.domain.SessionItem
import dev.arkhamd.wordletogether.wordle.ui.common.NoBorderButton

@Composable
fun SetSessionFieldAndButton(
    modifier: Modifier = Modifier,
    playerStats: PlayerStats,
    onCreateSession: (SessionItem) -> Unit,
    onClick: () -> Unit
) {
    var sessionId by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.enter_session_name)
        )
        TextField(
            value = sessionId,
            onValueChange = { sessionId = it },
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        NoBorderButton(
            onClick = {
                onCreateSession(
                    SessionItem(
                        id = sessionId,
                        winTotal = playerStats.winCount,
                        loseTotal = playerStats.loseCount,
                        drawTotal = playerStats.drawCount,
                        gamemode = "TwoSideMode"
                    )
                )
                onClick()
            }
        ) {
            Text(
                text = stringResource(id = R.string.done),
                fontSize = 16.sp
            )
        }
    }
}
