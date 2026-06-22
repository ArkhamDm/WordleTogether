package dev.arkhamd.wordletogether.singleplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import dev.arkhamd.wordletogether.platform.rememberInvalidWordFeedback
import dev.arkhamd.wordletogether.wordle.domain.Letter
import dev.arkhamd.wordletogether.wordle.domain.PlayerState
import dev.arkhamd.wordletogether.wordle.domain.WordleGameState
import dev.arkhamd.wordletogether.wordle.domain.canSubmitGuess
import dev.arkhamd.wordletogether.wordle.ui.LetterEvent
import dev.arkhamd.wordletogether.wordle.ui.LettersViewModel
import dev.arkhamd.wordletogether.wordle.ui.common.KeyboardGrid
import dev.arkhamd.wordletogether.wordle.ui.common.ListWords
import dev.arkhamd.wordletogether.wordle.ui.common.dialogs.ChangeScreenDialog
import dev.arkhamd.wordletogether.wordle.ui.common.dialogs.EndGameDialog

@Composable
fun SinglePlayerRoute(
    modifier: Modifier = Modifier,
    lettersViewModel: LettersViewModel,
    onNavigateToMainMenu: () -> Unit
) {
    val wordleState by lettersViewModel.wordleWords.collectAsState()
    val keyboardLetters by lettersViewModel.keyboardLetters.collectAsState()
    val currentWord by lettersViewModel.currentWord.collectAsState()
    val onInvalidWordAttempt = rememberInvalidWordFeedback()

    SinglePlayer(
        wordleState = wordleState,
        keyboardLetters = keyboardLetters,
        currentWord = currentWord,
        canSubmitGuess = wordleState.canSubmitGuess(lettersViewModel::isValidWord),
        onAddLetter = { lettersViewModel.send(LetterEvent.AddLetter(it)) },
        onDeleteLetter = { lettersViewModel.send(LetterEvent.DeleteLetter()) },
        onCheckAnswer = { lettersViewModel.send(LetterEvent.CheckAnswer()) },
        onInvalidWordAttempt = onInvalidWordAttempt,
        onRestart = { lettersViewModel.send(LetterEvent.Restart()) },
        onExitToMainMenu = {
            lettersViewModel.send(LetterEvent.Restart())
            onNavigateToMainMenu()
        },
        modifier = modifier
    )
}

@Composable
fun SinglePlayer(
    wordleState: WordleGameState,
    keyboardLetters: List<List<Letter>>,
    currentWord: String,
    canSubmitGuess: Boolean,
    onAddLetter: (Char) -> Unit,
    onDeleteLetter: () -> Unit,
    onCheckAnswer: () -> Unit,
    onInvalidWordAttempt: () -> Unit,
    onRestart: () -> Unit,
    onExitToMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var backPressed by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = modifier
    ) {
        ListWords(
            tryingWords = wordleState.tryingWords,
            fontSize = 54.sp
        )
        KeyboardGrid(
            keyboardLetters = keyboardLetters,
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

    if (wordleState.isLose or wordleState.isWin) {
        EndGameDialog(
            playerState = if (wordleState.isWin) PlayerState.WIN else PlayerState.LOSE,
            correctWord = currentWord,
            restartButton = onRestart,
            exitButton = onExitToMainMenu
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
