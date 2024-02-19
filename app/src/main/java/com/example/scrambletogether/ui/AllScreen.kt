@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.scrambletogether.ui

import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.scrambletogether.R
import com.example.scrambletogether.data.RouteName
import com.example.scrambletogether.firestore.ui.FirestoreViewModel
import com.example.scrambletogether.ui.theme.ScrambleTogetherTheme
import com.example.scrambletogether.ui.theme.md_theme_dark_onTertiary
import com.example.scrambletogether.ui.viewModels.LettersViewModel
import com.example.scrambletogether.utils.PlayerState
import kotlinx.coroutines.delay
import kotlin.system.exitProcess

@Composable
fun ChooseGameModeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    icon: Int,
    contentDescription: String,
    color: Color = Color.Red
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .drawWithContent {
                drawContent()
                drawNeonStroke(16.dp, color, neonWidth = 20f)
            }
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier
                .height(140.dp)
                .width(120.dp)
                .padding(8.dp)
        )
    }
}

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
        EndSingleGame(
            playerState = if (viewModelState.isWin) PlayerState.WIN else PlayerState.LOSE,
            correctWord = lettersViewModel.currentWord!!,
            restartButton = { lettersViewModel.restartGame() },
            exitButton = {
                lettersViewModel.restartGame()
                navController.navigate(RouteName.MAIN_MENU.string)
            }
        )
    }

    BackHandler {
        backPressed = true
    }

    if (backPressed) {
        ChangeGamemode(
            navigate = {
                navController.navigate(RouteName.MAIN_MENU.string)
                backPressed = false
                lettersViewModel.restartGame()
            },
            closeDialog = {
                backPressed = false
            }
        )
    }
}

@Composable
fun MultiPlayerOneDevice(
    modifier: Modifier = Modifier,
    navController: NavController,
    firstPlayerViewModel: LettersViewModel,
    secondPlayerViewModel: LettersViewModel
) {
    var backPressed by remember { mutableStateOf(false) }
    var setWordDialog by rememberSaveable { mutableStateOf(true) }
    var playerState: PlayerState? by remember { mutableStateOf(null) }
    var isPlayer1Play by rememberSaveable { mutableStateOf(true) }
    var isStartClock by rememberSaveable { mutableStateOf(false) }
    var restartClock by remember { mutableStateOf(false) }

    if (setWordDialog) {
        SetWordOneDeviceDialog(
            firstPlayerViewModel = firstPlayerViewModel,
            secondPlayerViewModel = secondPlayerViewModel
        ) {
            setWordDialog = false
            isStartClock = true
        }
    }

    Log.d(TAG, "recompose MultiPlayer")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MultiScreenOneDevice(
            firstPlayerViewModel = firstPlayerViewModel,
            secondPlayerViewModel = secondPlayerViewModel,
            endGame = {
                playerState = it
            },
            changePlayer = {
                isPlayer1Play =! isPlayer1Play
            },
            isPlayer1 = isPlayer1Play,
            isStartClock = isStartClock
        )
        KeyboardGrid(
            lettersViewModel = if (isPlayer1Play) firstPlayerViewModel else secondPlayerViewModel,
            changePlayer = {
                isPlayer1Play =! isPlayer1Play
                isStartClock = false
                restartClock = true
            }
        )
    }

    LaunchedEffect(restartClock) {
        if (restartClock) {
            delay(3000)
            isStartClock = true
            restartClock = false
        }
    }

    if (playerState != null) {
        firstPlayerViewModel.isDoneSwitch()
        secondPlayerViewModel.isDoneSwitch()
        isStartClock = false
        EndSingleGame(
            playerState = playerState!!,
            correctWord1 = firstPlayerViewModel.currentWord!!,
            correctWord2 = secondPlayerViewModel.currentWord!!,
            restartButton = {
                firstPlayerViewModel.restartGame()
                secondPlayerViewModel.restartGame()
                isPlayer1Play = true
                playerState = null
                setWordDialog = !setWordDialog
            },
            exitButton = {
                navController.navigate(RouteName.MAIN_MENU.string)
                firstPlayerViewModel.restartGame()
                secondPlayerViewModel.restartGame()
            },
            winPlayer1Text = stringResource(id = R.string.player_1_wins),
            winPlayer2Text = stringResource(id = R.string.player_2_wins),
            drawText = stringResource(id = R.string.draw)
        )
    }

    BackHandler {
        backPressed = true
    }

    if (backPressed) {
        ChangeGamemode(
            navigate = {
                navController.navigate(RouteName.MAIN_MENU.string)
                backPressed = false
                firstPlayerViewModel.restartGame()
                secondPlayerViewModel.restartGame()
            },
            closeDialog = {
                backPressed = false
            }
        )
    }
}

@Composable
fun MultiPlayerTwoDevices(
    modifier: Modifier = Modifier,
    navController: NavController,
    firestoreViewModel: FirestoreViewModel,
    lettersViewModel: LettersViewModel
) {
    var backPressed by remember { mutableStateOf(false) }
    var setWordDialog by remember { mutableStateOf(true) }
    var playerState: PlayerState? by remember { mutableStateOf(null) }

    if (setWordDialog) {
        SetWordTwoDevicesDialog(firestoreViewModel = firestoreViewModel) {
            setWordDialog = !setWordDialog
        }
        lettersViewModel.firebaseId = firestoreViewModel.infForViewmodel.sessionId
    }

    Log.d(TAG, "recompose MultiPlayer")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MultiScreenTwoDevices(
            secondPlayerViewModel = firestoreViewModel,
            firstPlayerViewModel = lettersViewModel,
            endGame = {
                playerState = it
            }
        )
        KeyboardGrid(lettersViewModel = lettersViewModel, isHost = firestoreViewModel.infForViewmodel.isHost)
    }

    if (playerState != null) {
        Log.d(TAG, "here")
        lettersViewModel.isDoneSwitch()
        firestoreViewModel.isDoneSwitch()
        EndSingleGame(
            playerState = playerState!!,
            correctWord = lettersViewModel.currentWord!!,
            restartButton = {
                lettersViewModel.restartGame(lettersViewModel.firebaseId)
                firestoreViewModel.reset()

                when (playerState) {
                    PlayerState.WIN -> {
                        firestoreViewModel.incWinCount()
                    }
                    PlayerState.LOSE -> {
                        firestoreViewModel.incLoseCount()
                    }
                    else -> firestoreViewModel.incDrawCount()
                }

                playerState = null
                setWordDialog = !setWordDialog
            },
            exitButton = {
                navController.navigate(RouteName.MAIN_MENU.string)
                lettersViewModel.restartGame(lettersViewModel.firebaseId)
                firestoreViewModel.reset()
                firestoreViewModel.disconnectFromSession()

                when (playerState) {
                    PlayerState.WIN -> {
                        firestoreViewModel.incWinCount()
                    }
                    PlayerState.LOSE -> {
                        firestoreViewModel.incLoseCount()
                    }
                    else -> firestoreViewModel.incDrawCount()
                }
            }
        )
    }

    BackHandler {
        backPressed = true
    }

    if (backPressed) {
        ChangeGamemode(
            navigate = {
                navController.navigate(RouteName.MAIN_MENU.string)
                backPressed = false
                lettersViewModel.restartGame(lettersViewModel.firebaseId)
                firestoreViewModel.reset()
                firestoreViewModel.disconnectFromSession()
            },
            closeDialog = {
                backPressed = false
            }
        )
    }
}

@Composable
fun MainMenu(
    modifier: Modifier = Modifier,
    navController: NavController,
    firestoreViewModel: FirestoreViewModel
) {
    var createGameDialog by remember { mutableStateOf(false) }

    if (createGameDialog) {
        MultiplayerDialog(navController = navController, firestoreViewModel = firestoreViewModel) {
            createGameDialog = !createGameDialog
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                textAlign = TextAlign.Center,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = md_theme_dark_onTertiary,
                modifier = Modifier
                    .padding(top = 36.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.eyes1),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .height(75.dp)
                    .width(300.dp)
                    .border(BorderStroke(1.dp, Color.White))
            )
        }

        Row {
            ChooseGameModeButton(
                onClick = { navController.navigate(RouteName.SINGLE_PLAYER.string) },
                icon = R.drawable.person,
                contentDescription = "singlePlayer"
            )
            Spacer(modifier = Modifier.width(64.dp))
            ChooseGameModeButton(
                onClick = { createGameDialog = true },
                icon = R.drawable.two_players_game_interface_symbol,
                contentDescription = "multiPlayer"
            )
        }

        OutlinedCard(
            modifier = modifier
                .padding(bottom = 36.dp),
            onClick = { exitProcess(0) },
            shape = AbsoluteRoundedCornerShape(
                corner = CornerSize(8.dp)
            )
        ) {
            Text(
                text = stringResource(R.string.exit),
                fontSize = 24.sp,
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = 32.dp,
                    end = 32.dp,
                    bottom = 8.dp
                ),
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Preview(showSystemUi = true,
    showBackground = true,
)
@Composable
fun MainMenuPreview() {
    ScrambleTogetherTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainMenu(navController = rememberNavController(), firestoreViewModel = viewModel())
        }
    }
}

fun ContentDrawScope.drawNeonStroke(radius: Dp, color: Color, neonWidth: Float) {
    this.drawIntoCanvas {
        val paint =
            Paint().apply {
                style = PaintingStyle.Stroke
                strokeWidth = neonWidth
            }

        val frameworkPaint =
            paint.asFrameworkPaint()

        this.drawIntoCanvas {
            frameworkPaint.color = color.copy(alpha = 0f).toArgb()
            frameworkPaint.setShadowLayer(
                radius.toPx(), 0f, 0f, color.copy(alpha = .5f).toArgb()
            )
            it.drawRoundRect(
                left = 0f,
                right = size.width,
                bottom = size.height,
                top = 0f,
                radiusY = radius.toPx(),
                radiusX = radius.toPx(),
                paint = paint
            )
            drawRoundRect(
                color = color,
                size = size,
                cornerRadius = CornerRadius(radius.toPx(), radius.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
