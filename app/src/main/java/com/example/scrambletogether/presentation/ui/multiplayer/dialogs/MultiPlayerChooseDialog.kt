package com.example.scrambletogether.presentation.ui.multiplayer.dialogs

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
import androidx.navigation.NavController
import com.example.scrambletogether.data.model.RouteName
import com.example.scrambletogether.presentation.ui.multiplayer.dialogs.extras.CreateOrFindButtons
import com.example.scrambletogether.presentation.ui.multiplayer.dialogs.extras.ListSessions
import com.example.scrambletogether.presentation.ui.multiplayer.dialogs.extras.OneOrTwoDevicesButtons
import com.example.scrambletogether.presentation.ui.multiplayer.dialogs.extras.SetSessionFieldAndButton
import com.example.scrambletogether.presentation.viewModel.FirestoreViewModel

@Composable
fun MultiPlayerChooseDialog(
    modifier: Modifier = Modifier,
    navController: NavController,
    firestoreViewModel: FirestoreViewModel,
    closeDialog: () -> Unit
) {
    var oneDeviceClick by remember { mutableStateOf(false) }
    var twoDevicesClick by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = closeDialog) {
        Card(modifier = modifier) {
            if (oneDeviceClick) {
                navController.navigate(RouteName.MULTI_PLAYER_ONE_DEVICE.string)
            } else if (twoDevicesClick) {
                MultiPlayerTwoDevicesDialog(
                    navController = navController,
                    firestoreViewModel = firestoreViewModel,
                )
            } else {
                OneOrTwoDevicesButtons(
                    oneDeviceClick = { oneDeviceClick = !oneDeviceClick },
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
    navController: NavController,
    firestoreViewModel: FirestoreViewModel
) {
    var findClicked by remember { mutableStateOf(false) }
    var createClicked by remember { mutableStateOf(false) }
    var connectClicked by remember { mutableStateOf(false) }
    if (findClicked) {
        ListSessions(
            firestoreViewModel = firestoreViewModel,
            connectClick = {
                connectClicked = !connectClicked
                findClicked = !findClicked
            },
            backClick = {
                findClicked = !findClicked
            }
        )
    } else if (createClicked ) {
        SetSessionFieldAndButton(firestoreViewModel = firestoreViewModel) {
            navController.navigate(RouteName.MULTI_PLAYER_TWO_DEVICES.string)
            createClicked = !createClicked
        }
    } else if (connectClicked) {
        navController.navigate(RouteName.MULTI_PLAYER_TWO_DEVICES.string)
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