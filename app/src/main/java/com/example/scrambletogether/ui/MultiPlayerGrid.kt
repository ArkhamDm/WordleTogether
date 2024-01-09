package com.example.scrambletogether.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scrambletogether.R
import com.example.scrambletogether.data.LetterDataClass
import com.example.scrambletogether.ui.viewModels.LettersEnemyViewModel
import com.example.scrambletogether.ui.viewModels.LettersViewModel

@Composable
fun MultiScreen(
    modifier: Modifier = Modifier,
    lettersViewModel_yours: LettersViewModel = viewModel(),
    lettersViewModel_enemy: LettersEnemyViewModel = viewModel(),
    exitButton: () -> Unit = {}
) {
    val viewModelStateYours by lettersViewModel_yours.wordleWords.collectAsState()
    val viewModelStateEnemy by lettersViewModel_enemy.wordleWords.collectAsState()

    val isWait = remember { mutableStateOf(false) }

    val viewModelStateEnemyCurrentState by lettersViewModel_enemy.currentState.collectAsState()
    Column(
        modifier = modifier
    ) {
        DoubleGrid(
            yourWords = viewModelStateYours.tryingWords,
            enemyWords = viewModelStateEnemy,
            enemyCurrentWord = viewModelStateEnemyCurrentState.currentWord
        )
    }

    if (viewModelStateYours.isDone or viewModelStateEnemyCurrentState.isWin) {
        EndSingleGame(
            isWin = viewModelStateYours.tryingWords[viewModelStateYours.wordsInLine]
                .count { it.color == Color.Green } == 5,
            correctWord = lettersViewModel_yours.currentWord!!,
            restartButton = {
                lettersViewModel_yours.restartGame(lettersViewModel_yours.firebaseId!!, isMultiplayer = true)
                lettersViewModel_enemy.restartGame()
                isWait.value = true
            },
            exitButton = exitButton
        )
    }

    if (isWait.value) {
        SetWordDialog(
            lettersViewModel = lettersViewModel_yours,
            isWait = isWait
        )
    }
}

@Composable
fun DoubleGrid(
    modifier: Modifier = Modifier,
    enemyCurrentWord: String = "?????",
    yourWords: Array<Array<LetterDataClass>>,
    enemyWords: Array<Array<LetterDataClass>>
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
            ListWords(
                tryingWords = yourWords, fontSize = 24.sp, padding = 1.dp
            )
            Text(
                text = stringResource(R.string.word) + ": ?????",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        //VerticalDivider
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
            LetterDataClass()
        }
    }
    DoubleGrid(yourWords = tryingWords, enemyWords = tryingWords)
}