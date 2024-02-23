package com.example.scrambletogether.presentation.ui.multiplayer

import android.content.ContentValues
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.scrambletogether.R
import com.example.scrambletogether.data.model.RouteName
import com.example.scrambletogether.presentation.ui.common.KeyboardGrid
import com.example.scrambletogether.presentation.ui.common.dialogs.ChangeScreenDialog
import com.example.scrambletogether.presentation.ui.common.dialogs.EndGameDialog
import com.example.scrambletogether.presentation.ui.multiplayer.dialogs.SetWordOneDeviceDialog
import com.example.scrambletogether.presentation.ui.multiplayer.dialogs.SetWordTwoDevicesDialog
import com.example.scrambletogether.presentation.utils.PlayerState
import com.example.scrambletogether.presentation.viewModel.FirestoreEvent
import com.example.scrambletogether.presentation.viewModel.FirestoreViewModel
import com.example.scrambletogether.presentation.viewModel.LetterEvent
import com.example.scrambletogether.presentation.viewModel.LettersViewModel
import kotlinx.coroutines.delay

@Composable
fun MultiPlayerOneDevice(
    modifier: Modifier = Modifier,
    navController: NavController,
    firstPlayerViewModel: LettersViewModel,
    secondPlayerViewModel: LettersViewModel
) {
    var backPressed by remember { mutableStateOf(false) }
    var setWordDialog by rememberSaveable { mutableStateOf(true) }
    var playerState: PlayerState? by remember { mutableStateOf(null) }
    var isPlayer1Play by rememberSaveable { mutableStateOf(true) }
    var isStartClock by rememberSaveable { mutableStateOf(false) }
    var restartClock by remember { mutableStateOf(false) }

    if (setWordDialog) {
        SetWordOneDeviceDialog(
            firstPlayerViewModel = firstPlayerViewModel,
            secondPlayerViewModel = secondPlayerViewModel
        ) {
            setWordDialog = false
            isStartClock = true
        }
    }

    Log.d(ContentValues.TAG, "recompose MultiPlayer")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MultiScreenOneDevice(
            firstPlayerViewModel = firstPlayerViewModel,
            secondPlayerViewModel = secondPlayerViewModel,
            endGame = {
                playerState = it
            },
            changePlayer = {
                isPlayer1Play =! isPlayer1Play
            },
            isPlayer1 = isPlayer1Play,
            isStartClock = isStartClock
        )
        KeyboardGrid(
            lettersViewModel = if (isPlayer1Play) firstPlayerViewModel else secondPlayerViewModel,
            changePlayer = {
                isPlayer1Play =! isPlayer1Play
                isStartClock = false
                restartClock = true
            }
        )
    }

    LaunchedEffect(restartClock) {
        if (restartClock) {
            delay(3000)
            isStartClock = true
            restartClock = false
        }
    }

    if (playerState != null) {
        firstPlayerViewModel.send(LetterEvent.IsDoneSwitch)
        secondPlayerViewModel.send(LetterEvent.IsDoneSwitch)
        isStartClock = false
        EndGameDialog(
            playerState = playerState!!,
            correctWord1 = firstPlayerViewModel.currentWord!!,
            correctWord2 = secondPlayerViewModel.currentWord!!,
            restartButton = {
                firstPlayerViewModel.send(LetterEvent.Restart())
                secondPlayerViewModel.send(LetterEvent.Restart())
                isPlayer1Play = true
                playerState = null
                setWordDialog = !setWordDialog
            },
            exitButton = {
                navController.navigate(RouteName.MAIN_MENU.string)
                firstPlayerViewModel.send(LetterEvent.Restart())
                secondPlayerViewModel.send(LetterEvent.Restart())
            },
            winPlayer1Text = stringResource(id = R.string.player_1_wins),
            winPlayer2Text = stringResource(id = R.string.player_2_wins),
            drawText = stringResource(id = R.string.draw)
        )
    }

    BackHandler {
        backPressed = true
    }

    if (backPressed) {
        ChangeScreenDialog(
            navigate = {
                navController.navigate(RouteName.MAIN_MENU.string)
                backPressed = false
                firstPlayerViewModel.send(LetterEvent.Restart())
                secondPlayerViewModel.send(LetterEvent.Restart())
            },
            closeDialog = {
                backPressed = false
            }
        )
    }
}

@Composable
fun MultiPlayerTwoDevices(
    modifier: Modifier = Modifier,
    navController: NavController,
    firestoreViewModel: FirestoreViewModel,
    lettersViewModel: LettersViewModel
) {
    var backPressed by remember { mutableStateOf(false) }
    var setWordDialog by remember { mutableStateOf(true) }
    var playerState: PlayerState? by remember { mutableStateOf(null) }

    if (setWordDialog) {
        SetWordTwoDevicesDialog(firestoreViewModel = firestoreViewModel) {
            setWordDialog = !setWordDialog
        }
        lettersViewModel.firebaseId = firestoreViewModel.infForViewmodel.sessionId
    }

    Log.d(ContentValues.TAG, "recompose MultiPlayer")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MultiScreenTwoDevices(
            secondPlayerViewModel = firestoreViewModel,
            firstPlayerViewModel = lettersViewModel,
            endGame = {
                playerState = it
            }
        )
        KeyboardGrid(lettersViewModel = lettersViewModel, isHost = firestoreViewModel.infForViewmodel.isHost)
    }

    if (playerState != null) {
        Log.d(ContentValues.TAG, "here")
        lettersViewModel.send(LetterEvent.IsDoneSwitch)
        firestoreViewModel.send(FirestoreEvent.IsDoneSwitch)
        EndGameDialog(
            playerState = playerState!!,
            correctWord = lettersViewModel.currentWord!!,
            restartButton = {
                lettersViewModel.send(LetterEvent.Restart(lettersViewModel.firebaseId))
                firestoreViewModel.send(FirestoreEvent.Reset)

                when (playerState) {
                    PlayerState.WIN -> {
                        firestoreViewModel.send(FirestoreEvent.IncWinCount)
                    }
                    PlayerState.LOSE -> {
                        firestoreViewModel.send(FirestoreEvent.IncLoseCount)
                    }
                    else -> firestoreViewModel.send(FirestoreEvent.IncDrawCount)
                }

                playerState = null
                setWordDialog = !setWordDialog
            },
            exitButton = {
                navController.navigate(RouteName.MAIN_MENU.string)
                lettersViewModel.send(LetterEvent.Restart(lettersViewModel.firebaseId))
                firestoreViewModel.send(FirestoreEvent.Reset)
                firestoreViewModel.send(FirestoreEvent.DisconnectFromSession)

                when (playerState) {
                    PlayerState.WIN -> {
                        firestoreViewModel.send(FirestoreEvent.IncWinCount)
                    }
                    PlayerState.LOSE -> {
                        firestoreViewModel.send(FirestoreEvent.IncLoseCount)
                    }
                    else -> firestoreViewModel.send(FirestoreEvent.IncDrawCount)
                }
            }
        )
    }

    BackHandler {
        backPressed = true
    }

    if (backPressed) {
        ChangeScreenDialog(
            navigate = {
                navController.navigate(RouteName.MAIN_MENU.string)
                backPressed = false
                lettersViewModel.send(LetterEvent.Restart(lettersViewModel.firebaseId))
                firestoreViewModel.send(FirestoreEvent.Reset)
                firestoreViewModel.send(FirestoreEvent.DisconnectFromSession)
            },
            closeDialog = {
                backPressed = false
            }
        )
    }
}