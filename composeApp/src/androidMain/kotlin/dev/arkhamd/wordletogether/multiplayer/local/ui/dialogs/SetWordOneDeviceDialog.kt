package dev.arkhamd.wordletogether.multiplayer.local.ui.dialogs

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
import dev.arkhamd.wordletogether.R
import dev.arkhamd.wordletogether.wordle.domain.WordleDefaults
import dev.arkhamd.wordletogether.wordle.ui.common.NoBorderButton

@Composable
fun SetWordOneDeviceCard(
    modifier: Modifier = Modifier,
    isValidWord: (String) -> Boolean,
    onSetFirstPlayerWord: (String) -> Unit,
    onSetSecondPlayerWord: (String) -> Unit,
    onClick: () -> Unit
) {
    var wordToEnemy by remember { mutableStateOf("") }
    var setToPlayer2 by remember { mutableStateOf(true) }
    val isErrorWord = wordToEnemy.length != WordleDefaults.WORD_LENGTH ||
        !isValidWord(wordToEnemy)

    Card(modifier = modifier) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (setToPlayer2) {
                    stringResource(R.string.word_for_player_2)
                } else {
                    stringResource(R.string.word_for_player_1)
                }
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
                                onSetSecondPlayerWord(wordToEnemy)
                            } else {
                                onSetFirstPlayerWord(wordToEnemy)
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
