package dev.arkhamd.wordletogether.multiplayer.local.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.arkhamd.wordletogether.R
import dev.arkhamd.wordletogether.multiplayer.local.domain.evaluateLocalMultiplayerRoundResult
import dev.arkhamd.wordletogether.multiplayer.ui.DoubleGrid
import dev.arkhamd.wordletogether.wordle.domain.PlayerState
import dev.arkhamd.wordletogether.wordle.domain.WordleGameState

@Composable
fun MultiScreenOneDevice(
    modifier: Modifier = Modifier,
    firstPlayerState: WordleGameState,
    secondPlayerState: WordleGameState,
    endGame: (PlayerState) -> Unit,
    isPlayer1: Boolean,
    changePlayer: () -> Unit,
    closeActiveLine: () -> Unit,
    isStartClock: Boolean,
    timerTime: Int
) {
    val playerResult = evaluateLocalMultiplayerRoundResult(
        firstPlayerState = firstPlayerState,
        secondPlayerState = secondPlayerState
    )

    LaunchedEffect(playerResult) {
        playerResult?.let(endGame)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Clock(
            modifier = Modifier.size(50.dp),
            start = isStartClock,
            timerTime = timerTime,
            changePlayerAndLine = {
                closeActiveLine()
                changePlayer()
            },
            isPlayer1 = isPlayer1
        )
        Spacer(modifier = Modifier.height(8.dp))
        DoubleGrid(
            yourWords = firstPlayerState.tryingWords,
            enemyWords = secondPlayerState.tryingWords,
            enemyCurrentWord = "?????",
            selfCurrentWord = "?????",
            isWait = false,
            nameForPlayer1 = stringResource(R.string.player1),
            nameForPlayer2 = stringResource(R.string.player2),
            isPlayer1 = isPlayer1
        )
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
                animationSpec = tween(timerTime * 1000, easing = LinearEasing)
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
            red = (animate.value / 360f),
            green = 1f - (animate.value / 360f),
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
