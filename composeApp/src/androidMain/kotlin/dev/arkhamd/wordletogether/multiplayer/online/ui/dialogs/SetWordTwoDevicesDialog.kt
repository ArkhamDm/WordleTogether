package dev.arkhamd.wordletogether.multiplayer.online.ui.dialogs

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
import dev.arkhamd.wordletogether.R
import dev.arkhamd.wordletogether.wordle.domain.WordleDefaults
import dev.arkhamd.wordletogether.wordle.ui.common.NoBorderButton

@Composable
fun SetWordTwoDevicesDialog(
    modifier: Modifier = Modifier,
    isValidWord: (String) -> Boolean,
    onUpdateWord: (String) -> Unit,
    onClick: () -> Unit
) {
    var wordToEnemy by remember { mutableStateOf("") }
    val isErrorWord = wordToEnemy.length != WordleDefaults.WORD_LENGTH ||
        !isValidWord(wordToEnemy)

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
                    onValueChange = { wordToEnemy = it.uppercase() },
                    modifier = Modifier.padding(16.dp),
                    isError = isErrorWord
                )

                Spacer(modifier = Modifier.height(32.dp))

                NoBorderButton(
                    onClick =
                        if (!isErrorWord) {
                            {
                                onClick()
                                onUpdateWord(wordToEnemy)
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
