package com.example.scrambletogether.ui

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrambletogether.R
import com.example.scrambletogether.data.Letter
import com.example.scrambletogether.firestore.ui.FirestoreViewModel
import com.example.scrambletogether.ui.viewModels.LettersViewModel
import kotlinx.coroutines.delay

@Composable
fun MultiScreen(
    modifier: Modifier = Modifier,
    firestoreViewModel: FirestoreViewModel,
    lettersViewModel: LettersViewModel,
    endGame: (Boolean) -> Unit,
) {
    val viewModelStateYours by lettersViewModel.wordleWords.collectAsState()
    val viewModelStateEnemy by firestoreViewModel.session.collectAsState()
    Log.d(TAG, "recompose MutliScreen")
    lettersViewModel.currentWord = viewModelStateEnemy.selfWord


    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.server_name) + firestoreViewModel.infForViewmodel.sessionId,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        DoubleGrid(
            yourWords = viewModelStateYours.tryingWords,
            enemyWords = viewModelStateEnemy.listenGrid,
            enemyCurrentWord = viewModelStateEnemy.enemyWord,
            selfCurrentWord = viewModelStateEnemy.selfWord,
            isWait = viewModelStateEnemy.isWait
        )
    }

    if (viewModelStateYours.isDone or viewModelStateEnemy.isDone) {
        endGame(viewModelStateYours.tryingWords[viewModelStateYours.wordsInLine]
            .count { it.color == Color.Green } == 5)
    }

}

@Composable
fun DoubleGrid(
    modifier: Modifier = Modifier,
    enemyCurrentWord: String,
    selfCurrentWord: String,
    yourWords: Array<Array<Letter>>,
    enemyWords: Array<Array<Letter>>,
    isWait: Boolean
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.you),
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            if (selfCurrentWord == "") {
                var dotsCount by remember { mutableIntStateOf(0) }
                Spacer(modifier = Modifier.height(34.dp))
                if (isWait) {
                    Text(
                        text = stringResource(R.string.waiting_enemy),
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(175.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.enemy_choosing_word),
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(175.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                LaunchedEffect(key1 = true) {
                    // delay for adding new dots
                    while (true) {
                        delay(500)
                        dotsCount = dotsCount % 5 + 1
                    }
                }
                LoadingIndicator(dotsCount = dotsCount)
            } else {
                ListWords(
                    tryingWords = yourWords, fontSize = 24.sp, padding = 1.dp
                )
                Text(
                    text = stringResource(R.string.word) + ": ?????",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }


        Divider(
            color = Color.Black,
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight(0.4f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.enemy),
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            ListWords(
                tryingWords = enemyWords, fontSize = 24.sp, padding = 1.dp
            )
            Text(
                text = "${stringResource(R.string.word)}: $enemyCurrentWord",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAAA() {
    val tryingWords = Array(6) {
        Array(5) {
            Letter()
        }
    }
    DoubleGrid(
        enemyCurrentWord = "????", selfCurrentWord = "", yourWords = tryingWords,
        enemyWords = tryingWords, isWait = false
    )
}