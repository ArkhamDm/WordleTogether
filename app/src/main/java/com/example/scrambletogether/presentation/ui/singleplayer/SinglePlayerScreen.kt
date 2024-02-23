package com.example.scrambletogether.presentation.ui.singleplayer

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.scrambletogether.data.model.RouteName
import com.example.scrambletogether.presentation.ui.common.KeyboardGrid
import com.example.scrambletogether.presentation.ui.common.ListWords
import com.example.scrambletogether.presentation.ui.common.dialogs.ChangeScreenDialog
import com.example.scrambletogether.presentation.ui.common.dialogs.EndGameDialog
import com.example.scrambletogether.presentation.utils.PlayerState
import com.example.scrambletogether.presentation.viewModel.LetterEvent
import com.example.scrambletogether.presentation.viewModel.LettersViewModel

@Composable
fun SinglePlayer(
    modifier: Modifier = Modifier,
    lettersViewModel: LettersViewModel = viewModel(),
    navController: NavController
) {
    val viewModelState by lettersViewModel.wordleWords.collectAsState()
    var backPressed by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = modifier
    ) {
        ListWords(
            tryingWords = viewModelState.tryingWords,
            fontSize = 54.sp
        )
        KeyboardGrid(lettersViewModel = lettersViewModel)
    }

    if (viewModelState.isLose or viewModelState.isWin) {
        EndGameDialog(
            playerState = if (viewModelState.isWin) PlayerState.WIN else PlayerState.LOSE,
            correctWord = lettersViewModel.currentWord!!,
            restartButton = { lettersViewModel.send(LetterEvent.Restart()) },
            exitButton = {
                lettersViewModel.send(LetterEvent.Restart())
                navController.navigate(RouteName.MAIN_MENU.string)
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
                lettersViewModel.send(LetterEvent.Restart())
            },
            closeDialog = {
                backPressed = false
            }
        )
    }
}