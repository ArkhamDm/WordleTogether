package com.example.scrambletogether.presentation.ui.multiplayer.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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
import androidx.compose.ui.window.Dialog
import com.example.scrambletogether.R
import com.example.scrambletogether.data.model.StartValues
import com.example.scrambletogether.presentation.ui.common.NoBorderButton
import com.example.scrambletogether.presentation.viewModel.FirestoreEvent
import com.example.scrambletogether.presentation.viewModel.FirestoreViewModel
import com.example.scrambletogether.presentation.viewModel.LettersViewModel

@Composable
fun SetWordTwoDevicesDialog(
    modifier: Modifier = Modifier,
    firestoreViewModel: FirestoreViewModel,
    onClick: () -> Unit
) {
    var isErrorWord by remember { mutableStateOf(true) }
    var wordToEnemy  by remember { mutableStateOf("") }

    if (wordToEnemy.length >= StartValues.startWordleWords.tryingWords[0].size) {
        isErrorWord = wordToEnemy !in StartValues.words
    }

    Dialog(onDismissRequest = {}) {
        Card(modifier = modifier) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.enter_word)
                )
                TextField(
                    value = wordToEnemy,
                    onValueChange = {wordToEnemy = it.uppercase()},
                    modifier = Modifier.padding(16.dp),
                    isError = isErrorWord
                )

                Spacer(modifier = Modifier.height(32.dp))

                NoBorderButton(
                    onClick =
                    if (!isErrorWord) {
                        {
                            onClick()
                            firestoreViewModel.send(FirestoreEvent.UpdateWord(wordToEnemy))
                        }
                    } else {
                        {}
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.done),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SetWordOneDeviceDialog(
    modifier: Modifier = Modifier,
    firstPlayerViewModel: LettersViewModel,
    secondPlayerViewModel: LettersViewModel,
    onClick: () -> Unit
) {
    var isErrorWord by remember { mutableStateOf(true) }
    var wordToEnemy  by remember { mutableStateOf("") }

    var setToPlayer2 by remember { mutableStateOf(true) }

    if (wordToEnemy.length >= StartValues.startWordleWords.tryingWords[0].size) {
        isErrorWord = wordToEnemy !in StartValues.words
    }

    Dialog(onDismissRequest = {}) {
        Card(modifier = modifier) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text =  if (setToPlayer2) stringResource(R.string.word_for_player_2)
                    else stringResource(R.string.word_for_player_1)
                )
                TextField(
                    value = wordToEnemy,
                    onValueChange = { wordToEnemy = it.uppercase() },
                    modifier = Modifier.padding(16.dp),
                    isError = isErrorWord
                )

                Spacer(modifier = Modifier.height(32.dp))

                NoBorderButton(
                    onClick =
                    if (!isErrorWord) {
                        {
                            if (setToPlayer2) {
                                secondPlayerViewModel.currentWord = wordToEnemy
                            } else {
                                firstPlayerViewModel.currentWord = wordToEnemy
                                onClick()
                            }
                            setToPlayer2 = !setToPlayer2
                            wordToEnemy = ""
                        }
                    } else {
                        {}
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.done),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}