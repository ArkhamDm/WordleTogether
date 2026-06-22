package dev.arkhamd.wordletogether.multiplayer.local.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import dev.arkhamd.wordletogether.R
import dev.arkhamd.wordletogether.multiplayer.local.ui.dialogs.SetWordOneDeviceCard
import dev.arkhamd.wordletogether.multiplayer.local.ui.dialogs.TimerTimeChanger
import dev.arkhamd.wordletogether.platform.rememberInvalidWordFeedback
import dev.arkhamd.wordletogether.wordle.domain.Letter
import dev.arkhamd.wordletogether.wordle.domain.PlayerState
import dev.arkhamd.wordletogether.wordle.domain.WordleGameState
import dev.arkhamd.wordletogether.wordle.domain.canSubmitGuess
import dev.arkhamd.wordletogether.wordle.ui.LetterEvent
import dev.arkhamd.wordletogether.wordle.ui.LettersViewModel
import dev.arkhamd.wordletogether.wordle.ui.common.KeyboardGrid
import dev.arkhamd.wordletogether.wordle.ui.common.dialogs.ChangeScreenDialog
import dev.arkhamd.wordletogether.wordle.ui.common.dialogs.EndGameDialog

@Composable
fun MultiPlayerOneDeviceRoute(
    modifier: Modifier = Modifier,
    firstPlayerViewModel: LettersViewModel,
    secondPlayerViewModel: LettersViewModel,
    onNavigateToMainMenu: () -> Unit
) {
    val firstPlayerState by firstPlayerViewModel.wordleWords.collectAsState()
    val secondPlayerState by secondPlayerViewModel.wordleWords.collectAsState()
    val firstPlayerKeyboard by firstPlayerViewModel.keyboardLetters.collectAsState()
    val secondPlayerKeyboard by secondPlayerViewModel.keyboardLetters.collectAsState()
    val firstPlayerCurrentWord by firstPlayerViewModel.currentWord.collectAsState()
    val secondPlayerCurrentWord by secondPlayerViewModel.currentWord.collectAsState()
    val onInvalidWordAttempt = rememberInvalidWordFeedback()

    MultiPlayerOneDevice(
        firstPlayerState = firstPlayerState,
        secondPlayerState = secondPlayerState,
        firstPlayerKeyboard = firstPlayerKeyboard,
        secondPlayerKeyboard = secondPlayerKeyboard,
        firstPlayerCurrentWord = firstPlayerCurrentWord,
        secondPlayerCurrentWord = secondPlayerCurrentWord,
        firstPlayerCanSubmitGuess = firstPlayerState.canSubmitGuess(firstPlayerViewModel::isValidWord),
        secondPlayerCanSubmitGuess = secondPlayerState.canSubmitGuess(secondPlayerViewModel::isValidWord),
        isFirstPlayerValidWord = firstPlayerViewModel::isValidWord,
        onSetFirstPlayerWord = { firstPlayerViewModel.send(LetterEvent.SetCurrentWord(it)) },
        onSetSecondPlayerWord = { secondPlayerViewModel.send(LetterEvent.SetCurrentWord(it)) },
        onCloseFirstPlayerLine = { firstPlayerViewModel.send(LetterEvent.CloseLine) },
        onCloseSecondPlayerLine = { secondPlayerViewModel.send(LetterEvent.CloseLine) },
        onFirstPlayerAddLetter = { firstPlayerViewModel.send(LetterEvent.AddLetter(it)) },
        onSecondPlayerAddLetter = { secondPlayerViewModel.send(LetterEvent.AddLetter(it)) },
        onFirstPlayerDeleteLetter = { firstPlayerViewModel.send(LetterEvent.DeleteLetter()) },
        onSecondPlayerDeleteLetter = { secondPlayerViewModel.send(LetterEvent.DeleteLetter()) },
        onFirstPlayerCheckAnswer = { firstPlayerViewModel.send(LetterEvent.CheckAnswer()) },
        onSecondPlayerCheckAnswer = { secondPlayerViewModel.send(LetterEvent.CheckAnswer()) },
        onInvalidWordAttempt = onInvalidWordAttempt,
        onRoundDone = {
            firstPlayerViewModel.send(LetterEvent.IsDoneSwitch)
            secondPlayerViewModel.send(LetterEvent.IsDoneSwitch)
        },
        onRestartPlayers = {
            firstPlayerViewModel.send(LetterEvent.Restart())
            secondPlayerViewModel.send(LetterEvent.Restart())
        },
        onExitToMainMenu = {
            firstPlayerViewModel.send(LetterEvent.Restart())
            secondPlayerViewModel.send(LetterEvent.Restart())
            onNavigateToMainMenu()
        },
        modifier = modifier
    )
}

@Composable
fun MultiPlayerOneDevice(
    firstPlayerState: WordleGameState,
    secondPlayerState: WordleGameState,
    firstPlayerKeyboard: List<List<Letter>>,
    secondPlayerKeyboard: List<List<Letter>>,
    firstPlayerCurrentWord: String,
    secondPlayerCurrentWord: String,
    firstPlayerCanSubmitGuess: Boolean,
    secondPlayerCanSubmitGuess: Boolean,
    isFirstPlayerValidWord: (String) -> Boolean,
    onSetFirstPlayerWord: (String) -> Unit,
    onSetSecondPlayerWord: (String) -> Unit,
    onCloseFirstPlayerLine: () -> Unit,
    onCloseSecondPlayerLine: () -> Unit,
    onFirstPlayerAddLetter: (Char) -> Unit,
    onSecondPlayerAddLetter: (Char) -> Unit,
    onFirstPlayerDeleteLetter: () -> Unit,
    onSecondPlayerDeleteLetter: () -> Unit,
    onFirstPlayerCheckAnswer: () -> Unit,
    onSecondPlayerCheckAnswer: () -> Unit,
    onInvalidWordAttempt: () -> Unit,
    onRoundDone: () -> Unit,
    onRestartPlayers: () -> Unit,
    onExitToMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var backPressed by remember { mutableStateOf(false) }
    var setWordDialog by rememberSaveable { mutableStateOf(true) }
    var playerState: PlayerState? by remember { mutableStateOf(null) }
    var isPlayer1Play by rememberSaveable { mutableStateOf(true) }
    var isStartClock by rememberSaveable { mutableStateOf(false) }
    var timerTime by remember { mutableIntStateOf(1) }
    val activePlayerKeyboard = if (isPlayer1Play) firstPlayerKeyboard else secondPlayerKeyboard
    val activePlayerCanSubmitGuess = if (isPlayer1Play) {
        firstPlayerCanSubmitGuess
    } else {
        secondPlayerCanSubmitGuess
    }

    if (setWordDialog) {
        Dialog(onDismissRequest = {}) {
            if (timerTime == 1) {
                TimerTimeChanger(
                    getTimerTime = { timerTime = it }
                )
            } else {
                SetWordOneDeviceCard(
                    isValidWord = isFirstPlayerValidWord,
                    onSetFirstPlayerWord = onSetFirstPlayerWord,
                    onSetSecondPlayerWord = onSetSecondPlayerWord
                ) {
                    setWordDialog = false
                    isStartClock = true
                }
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MultiScreenOneDevice(
            firstPlayerState = firstPlayerState,
            secondPlayerState = secondPlayerState,
            endGame = {
                playerState = it
            },
            changePlayer = {
                isPlayer1Play = !isPlayer1Play
            },
            closeActiveLine = {
                if (isPlayer1Play) {
                    onCloseFirstPlayerLine()
                } else {
                    onCloseSecondPlayerLine()
                }
            },
            isPlayer1 = isPlayer1Play,
            isStartClock = isStartClock,
            timerTime = timerTime
        )
        KeyboardGrid(
            keyboardLetters = activePlayerKeyboard,
            onAddLetter = {
                if (isPlayer1Play) {
                    onFirstPlayerAddLetter(it)
                } else {
                    onSecondPlayerAddLetter(it)
                }
            },
            onDeleteLetter = {
                if (isPlayer1Play) {
                    onFirstPlayerDeleteLetter()
                } else {
                    onSecondPlayerDeleteLetter()
                }
            },
            onSubmitGuess = {
                if (activePlayerCanSubmitGuess) {
                    if (isPlayer1Play) {
                        onFirstPlayerCheckAnswer()
                    } else {
                        onSecondPlayerCheckAnswer()
                    }
                    isPlayer1Play = !isPlayer1Play
                    true
                } else {
                    onInvalidWordAttempt()
                    false
                }
            }
        )
    }

    val oneDevicePlayerState = playerState
    if (oneDevicePlayerState != null) {
        LaunchedEffect(oneDevicePlayerState) {
            onRoundDone()
            isStartClock = false
        }

        EndGameDialog(
            playerState = oneDevicePlayerState,
            correctWord1 = firstPlayerCurrentWord,
            correctWord2 = secondPlayerCurrentWord,
            restartButton = {
                onRestartPlayers()
                isPlayer1Play = true
                playerState = null
                setWordDialog = !setWordDialog
                timerTime = 1
            },
            exitButton = {
                onExitToMainMenu()
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
                backPressed = false
                onExitToMainMenu()
            },
            closeDialog = {
                backPressed = false
            }
        )
    }
}
