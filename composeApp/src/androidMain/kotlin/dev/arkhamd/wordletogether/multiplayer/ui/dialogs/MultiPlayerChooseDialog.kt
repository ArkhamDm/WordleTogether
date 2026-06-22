package dev.arkhamd.wordletogether.multiplayer.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.arkhamd.wordletogether.multiplayer.online.ui.SessionsList
import dev.arkhamd.wordletogether.multiplayer.online.ui.dialogs.extras.ListSessions
import dev.arkhamd.wordletogether.multiplayer.online.ui.dialogs.extras.SetSessionFieldAndButton
import dev.arkhamd.wordletogether.multiplayer.ui.dialogs.extras.CreateOrFindButtons
import dev.arkhamd.wordletogether.multiplayer.ui.dialogs.extras.OneOrTwoDevicesButtons
import dev.arkhamd.wordletogether.profile.domain.PlayerStats
import dev.arkhamd.wordletogether.session.domain.SessionItem

@Composable
fun MultiPlayerChooseDialog(
    modifier: Modifier = Modifier,
    sessions: SessionsList,
    playerStats: PlayerStats,
    onOneDeviceClick: () -> Unit,
    onRefreshSessions: () -> Unit,
    onConnectToSession: (String) -> Unit,
    onCreateSession: (SessionItem) -> Unit,
    onTwoDevicesReady: () -> Unit,
    closeDialog: () -> Unit
) {
    var twoDevicesClick by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = closeDialog) {
        Card(modifier = modifier) {
            if (twoDevicesClick) {
                MultiPlayerTwoDevicesDialog(
                    sessions = sessions,
                    playerStats = playerStats,
                    onRefreshSessions = onRefreshSessions,
                    onConnectToSession = onConnectToSession,
                    onCreateSession = onCreateSession,
                    onTwoDevicesReady = onTwoDevicesReady
                )
            } else {
                OneOrTwoDevicesButtons(
                    oneDeviceClick = {
                        closeDialog()
                        onOneDeviceClick()
                    },
                    twoDevicesClick = { twoDevicesClick = !twoDevicesClick },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp, horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
fun MultiPlayerTwoDevicesDialog(
    sessions: SessionsList,
    playerStats: PlayerStats,
    onRefreshSessions: () -> Unit,
    onConnectToSession: (String) -> Unit,
    onCreateSession: (SessionItem) -> Unit,
    onTwoDevicesReady: () -> Unit
) {
    var findClicked by remember { mutableStateOf(false) }
    var createClicked by remember { mutableStateOf(false) }

    if (findClicked) {
        ListSessions(
            sessions = sessions,
            onRefreshSessions = onRefreshSessions,
            onConnectToSession = onConnectToSession,
            connectClick = onTwoDevicesReady,
            backClick = {
                findClicked = !findClicked
            }
        )
    } else if (createClicked) {
        SetSessionFieldAndButton(
            playerStats = playerStats,
            onCreateSession = onCreateSession
        ) {
            onTwoDevicesReady()
            createClicked = !createClicked
        }
    } else {
        CreateOrFindButtons(
            createClick = {
                createClicked = !createClicked
            },
            findClick = {
                findClicked = !findClicked
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 64.dp, horizontal = 12.dp)
        )
    }
}
