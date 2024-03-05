package com.example.scrambletogether.presentation.ui.multiplayer

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrambletogether.R
import com.example.scrambletogether.domain.model.Letter
import com.example.scrambletogether.presentation.ui.common.ListWords
import com.example.scrambletogether.presentation.ui.multiplayer.dialogs.extras.LoadingIndicator
import com.example.scrambletogether.presentation.utils.PlayerState
import com.example.scrambletogether.presentation.viewModel.FirestoreViewModel
import com.example.scrambletogether.presentation.viewModel.LetterEvent
import com.example.scrambletogether.presentation.viewModel.LettersViewModel
import kotlinx.coroutines.delay

@Composable
fun MultiScreenOneDevice(
    modifier: Modifier = Modifier,
    firstPlayerViewModel: LettersViewModel,
    secondPlayerViewModel: LettersViewModel,
    endGame: (PlayerState) -> Unit,
    isPlayer1: Boolean,
    changePlayer: () -> Unit,
    isStartClock: Boolean,
    timerTime: Int,
) {
    val viewModelStateYours by firstPlayerViewModel.wordleWords.collectAsState()
    val viewModelStateEnemy by secondPlayerViewModel.wordleWords.collectAsState()
    Log.d(TAG, "recompose MutliScreen")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Clock(
            modifier = Modifier.size(50.dp),
            start = isStartClock,
            timerTime = timerTime,
            changePlayerAndLine = {
                if (isPlayer1) firstPlayerViewModel.send(LetterEvent.CloseLine)
                else secondPlayerViewModel.send(LetterEvent.CloseLine)

                changePlayer()
            },
            isPlayer1 = isPlayer1
        )
        Spacer(modifier = Modifier.height(8.dp))
        DoubleGrid(
            yourWords = viewModelStateYours.tryingWords,
            enemyWords = viewModelStateEnemy.tryingWords,
            enemyCurrentWord = "?????",
            selfCurrentWord = "?????",
            isWait = false,
            nameForPlayer1 = stringResource(R.string.player1),
            nameForPlayer2 = stringResource(R.string.player2),
            isPlayer1 = isPlayer1
        )
    }

    if (viewModelStateYours.isWin or viewModelStateEnemy.isWin)
    {
        endGame(if (viewModelStateYours.isWin) PlayerState.WIN else PlayerState.LOSE)
    } else if (viewModelStateYours.isLose and viewModelStateEnemy.isLose) {
        endGame(PlayerState.DRAW)
    }
}




@Composable
fun MultiScreenTwoDevices(
    modifier: Modifier = Modifier,
    firstPlayerViewModel: LettersViewModel,
    secondPlayerViewModel: FirestoreViewModel,
    endGame: (PlayerState) -> Unit
) {
    val viewModelStateYours by firstPlayerViewModel.wordleWords.collectAsState()
    val viewModelStateEnemy by secondPlayerViewModel.session.collectAsState()
    Log.d(TAG, "recompose MutliScreen")
    firstPlayerViewModel.currentWord = viewModelStateEnemy.selfWord

    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.server_name) + secondPlayerViewModel.infForViewmodel.sessionId,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        DoubleGrid(
            yourWords = viewModelStateYours.tryingWords,
            enemyWords = viewModelStateEnemy.listenGrid,
            enemyCurrentWord = viewModelStateEnemy.enemyWord,
            selfCurrentWord = viewModelStateEnemy.selfWord,
            isWait = viewModelStateEnemy.isWait,
            nameForPlayer1 = stringResource(R.string.you),
            nameForPlayer2 = stringResource(R.string.enemy)
        )
    }

    if (viewModelStateYours.isWin or viewModelStateEnemy.isWin)
    {
        endGame(if (viewModelStateYours.isWin) PlayerState.WIN else PlayerState.LOSE)
    } else if (viewModelStateYours.isLose and viewModelStateEnemy.isLose) {
        endGame(PlayerState.DRAW)
    }

}


@Composable
fun DoubleGrid(
    modifier: Modifier = Modifier,
    enemyCurrentWord: String,
    selfCurrentWord: String,
    yourWords: Array<Array<Letter>>,
    enemyWords: Array<Array<Letter>>,
    isWait: Boolean,
    nameForPlayer1: String,
    nameForPlayer2: String,
    isPlayer1: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = nameForPlayer1,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 6.dp),
                color = if (isPlayer1) Color.Green else Color.White
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
                text = nameForPlayer2,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 6.dp),
                color = if (isPlayer1) Color.White else Color.Green
            )
            ListWords(
                tryingWords = enemyWords, fontSize = 24.sp, padding = 1.dp
            )
            if (enemyCurrentWord != "") {
                Text(
                    text = "${stringResource(R.string.word)}: $enemyCurrentWord",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun Clock(
    modifier: Modifier,
    changePlayerAndLine: () -> Unit,
    start: Boolean,
    timerTime: Int,
    isPlayer1: Boolean
) {
    val rotationState by remember { mutableFloatStateOf(1f) }

    val animate = remember { Animatable(rotationState) }

    if (start) {
        LaunchedEffect(isPlayer1) {
            animate.snapTo(0f)
            animate.animateTo(
                360f,
                animationSpec = tween(timerTime*1000, easing = LinearEasing)
            )
            if (!animate.isRunning) {
                animate.snapTo(0f)
                changePlayerAndLine()
            }
        }
    }

    val animatedRotation = remember(animate.value) { animate.value }
    val animatedColor = remember(animate.value) {
        Color(
            red = (animate.value/360f),
            green = 1f - (animate.value/360f),
            blue = 0f
        )
    }

    Canvas(
        modifier = modifier
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val circleRadius = 20.dp.toPx()
        val arrowLength = 15.dp.toPx()

        drawCircle(
            color = animatedColor,
            radius = circleRadius,
            style = Stroke(width = 4f)
        )

        // Отрисовываем стрелку с вращением
        rotate(degrees = animatedRotation, pivot = center) {
            drawLine(
                color = animatedColor,
                start = center,
                end = Offset(center.x, center.y - arrowLength),
                strokeWidth = 5f
            )
        }
    }
}
