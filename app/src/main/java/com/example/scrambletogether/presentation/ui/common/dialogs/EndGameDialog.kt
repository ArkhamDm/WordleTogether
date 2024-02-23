package com.example.scrambletogether.presentation.ui.common.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.scrambletogether.R
import com.example.scrambletogether.presentation.ui.common.NoBorderButton
import com.example.scrambletogether.presentation.utils.PlayerState

@Composable
fun EndGameDialog(
    modifier: Modifier = Modifier,
    playerState: PlayerState,
    correctWord: String,
    restartButton: () -> Unit = {},
    exitButton: () -> Unit = {},
    winText: String = stringResource(id = R.string.single_win),
    loseText: String = stringResource(id = R.string.single_lose),
    drawText: String = stringResource(id = R.string.draw)
) {
    Dialog(onDismissRequest = {}) {
        Card(modifier = modifier) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text =
                    when (playerState) {
                        PlayerState.WIN -> winText
                        PlayerState.LOSE -> loseText
                        else -> drawText
                    },
                    fontSize = 36.sp,
                    modifier = Modifier
                        .padding(top = 6.dp, start = 40.dp, end = 40.dp, bottom = 64.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.your_word_was),
                        fontSize = 24.sp
                    )
                    Text(
                        text = correctWord,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                ) {
                    NoBorderButton(onClick = restartButton) {
                        Text(
                            text = stringResource(R.string.again_button)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    NoBorderButton(onClick = exitButton) {
                        Text(
                            text = stringResource(R.string.into_main_menu)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

//for one device
@Composable
fun EndGameDialog(
    modifier: Modifier = Modifier,
    playerState: PlayerState,
    correctWord1: String,
    correctWord2: String,
    restartButton: () -> Unit,
    exitButton: () -> Unit,
    winPlayer1Text: String,
    winPlayer2Text: String,
    drawText: String
) {
    Dialog(onDismissRequest = {}) {
        Card(modifier = modifier) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text =
                    when (playerState) {
                        PlayerState.WIN -> winPlayer1Text
                        PlayerState.LOSE -> winPlayer2Text
                        else -> drawText
                    },
                    fontSize = 36.sp,
                    modifier = Modifier
                        .padding(top = 6.dp, start = 40.dp, end = 40.dp, bottom = 32.dp),
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.player_1_word_was),
                        fontSize = 24.sp
                    )
                    Text(
                        text = correctWord1,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.player_2_word_was),
                        fontSize = 24.sp
                    )
                    Text(
                        text = correctWord2,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                ) {
                    NoBorderButton(onClick = restartButton) {
                        Text(
                            text = stringResource(R.string.again_button)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    NoBorderButton(onClick = exitButton) {
                        Text(
                            text = stringResource(R.string.into_main_menu)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}