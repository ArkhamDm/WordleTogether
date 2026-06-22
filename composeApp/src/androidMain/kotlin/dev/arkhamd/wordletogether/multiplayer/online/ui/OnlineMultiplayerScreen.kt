package dev.arkhamd.wordletogether.multiplayer.online.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.arkhamd.wordletogether.multiplayer.online.ui.dialogs.SetWordTwoDevicesDialog
import dev.arkhamd.wordletogether.platform.rememberInvalidWordFeedback
import dev.arkhamd.wordletogether.session.domain.SessionExtra
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
fun MultiPlayerTwoDevicesRoute(
    modifier: Modifier = Modifier,
    gameSessionViewModel: GameSessionViewModel,
    lettersViewModel: LettersViewModel,
    onNavigateToMainMenu: () -> Unit
) {
    val session by gameSessionViewModel.session.collectAsState()
    val sessionHandle by gameSessionViewModel.sessionHandle.collectAsState()
    val wordleState by lettersViewModel.wordleWords.collectAsState()
    val keyboardLetters by lettersViewModel.keyboardLetters.collectAsState()
    val currentWord by lettersViewModel.currentWord.collectAsState()
    val onInvalidWordAttempt = rememberInvalidWordFeedback()

    LaunchedEffect(sessionHandle) {
        val handle = sessionHandle
        if (handle != null) {
            lettersViewModel.send(LetterEvent.SetSessionHandle(handle))
        }
    }

    MultiPlayerTwoDevices(
        session = session,
        wordleState = wordleState,
        keyboardLetters = keyboardLetters,
        currentWord = currentWord,
        isRoundReady = sessionHandle != null && session.selfWord.isNotBlank(),
        canSubmitGuess = wordleState.canSubmitGuess(lettersViewModel::isValidWord),
        isValidWord = lettersViewModel::isValidWord,
        onUpdateWord = { gameSessionViewModel.send(GameSessionEvent.UpdateWord(it)) },
        onTargetWordChanged = { lettersViewModel.send(LetterEvent.SetCurrentWord(it)) },
        onAddLetter = { lettersViewModel.send(LetterEvent.AddLetter(it, session.isHost)) },
        onDeleteLetter = { lettersViewModel.send(LetterEvent.DeleteLetter(session.isHost)) },
        onCheckAnswer = { lettersViewModel.send(LetterEvent.CheckAnswer(session.isHost)) },
        onInvalidWordAttempt = onInvalidWordAttempt,
        onRoundDone = {
            lettersViewModel.send(LetterEvent.IsDoneSwitch)
            gameSessionViewModel.send(GameSessionEvent.IsDoneSwitch)
        },
        onRestartRound = {
            lettersViewModel.send(LetterEvent.Restart(sessionHandle))
            gameSessionViewModel.send(GameSessionEvent.Reset)
        },
        onRecordResult = {
            gameSessionViewModel.send(GameSessionEvent.RecordResult(it))
        },
        onExitRound = {
            onNavigateToMainMenu()
            lettersViewModel.send(LetterEvent.Restart(sessionHandle))
            gameSessionViewModel.send(GameSessionEvent.Reset)
            gameSessionViewModel.send(GameSessionEvent.DisconnectFromSession)
            gameSessionViewModel.send(GameSessionEvent.RecordResult(it))
        },
        onLeaveSession = {
            onNavigateToMainMenu()
            lettersViewModel.send(LetterEvent.Restart(sessionHandle))
            gameSessionViewModel.send(GameSessionEvent.Reset)
            gameSessionViewModel.send(GameSessionEvent.DisconnectFromSession)
        },
        modifier = modifier
    )
}

@Composable
fun MultiPlayerTwoDevices(
    session: SessionExtra,
    wordleState: WordleGameState,
    keyboardLetters: List<List<Letter>>,
    currentWord: String,
    isRoundReady: Boolean,
    canSubmitGuess: Boolean,
    isValidWord: (String) -> Boolean,
    onUpdateWord: (String) -> Unit,
    onTargetWordChanged: (String) -> Unit,
    onAddLetter: (Char) -> Unit,
    onDeleteLetter: () -> Unit,
    onCheckAnswer: () -> Unit,
    onInvalidWordAttempt: () -> Unit,
    onRoundDone: () -> Unit,
    onRestartRound: () -> Unit,
    onRecordResult: (PlayerState) -> Unit,
    onExitRound: (PlayerState) -> Unit,
    onLeaveSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    var backPressed by remember { mutableStateOf(false) }
    var setWordDialog by remember { mutableStateOf(true) }
    var playerState: PlayerState? by remember { mutableStateOf(null) }

    if (setWordDialog) {
        SetWordTwoDevicesDialog(
            isValidWord = isValidWord,
            onUpdateWord = onUpdateWord
        ) {
            setWordDialog = !setWordDialog
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MultiScreenTwoDevices(
            session = session,
            yourState = wordleState,
            onTargetWordChanged = onTargetWordChanged,
            endGame = {
                playerState = it
            }
        )
        KeyboardGrid(
            keyboardLetters = keyboardLetters,
            enabled = isRoundReady,
            onAddLetter = onAddLetter,
            onDeleteLetter = onDeleteLetter,
            onSubmitGuess = {
                if (canSubmitGuess) {
                    onCheckAnswer()
                    true
                } else {
                    onInvalidWordAttempt()
                    false
                }
            }
        )
    }

    val twoDevicesPlayerState = playerState
    if (twoDevicesPlayerState != null) {
        LaunchedEffect(twoDevicesPlayerState) {
            onRoundDone()
        }

        EndGameDialog(
            playerState = twoDevicesPlayerState,
            correctWord = currentWord,
            restartButton = {
                onRestartRound()
                onRecordResult(twoDevicesPlayerState)
                playerState = null
                setWordDialog = !setWordDialog
            },
            exitButton = {
                onExitRound(twoDevicesPlayerState)
            }
        )
    }

    BackHandler {
        backPressed = true
    }

    if (backPressed) {
        ChangeScreenDialog(
            navigate = {
                backPressed = false
                onLeaveSession()
            },
            closeDialog = {
                backPressed = false
            }
        )
    }
}
