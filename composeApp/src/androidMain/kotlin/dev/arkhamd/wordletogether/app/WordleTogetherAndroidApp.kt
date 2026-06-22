package dev.arkhamd.wordletogether.app

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.arkhamd.wordletogether.app.ui.MainMenuRoute
import dev.arkhamd.wordletogether.design.theme.WordleTogetherTheme
import dev.arkhamd.wordletogether.multiplayer.local.ui.MultiPlayerOneDeviceRoute
import dev.arkhamd.wordletogether.multiplayer.online.ui.GameSessionEvent
import dev.arkhamd.wordletogether.multiplayer.online.ui.GameSessionViewModel
import dev.arkhamd.wordletogether.multiplayer.online.ui.MultiPlayerTwoDevicesRoute
import dev.arkhamd.wordletogether.profile.data.AndroidPlayerProfileRepository
import dev.arkhamd.wordletogether.session.data.LocalGameSessionRepository
import dev.arkhamd.wordletogether.singleplayer.ui.SinglePlayerRoute
import dev.arkhamd.wordletogether.wordle.data.AndroidAssetWordDictionary
import dev.arkhamd.wordletogether.wordle.ui.LettersViewModel

@Composable
fun WordleTogetherAndroidApp(
    context: Context,
    onExit: () -> Unit
) {
    val appContext = remember(context) { context.applicationContext }
    val playerProfileRepository = remember(appContext) {
        AndroidPlayerProfileRepository(context = appContext)
    }
    val wordDictionary = remember(appContext) {
        AndroidAssetWordDictionary(context = appContext)
    }
    val gameSessionGateway = remember {
        LocalGameSessionRepository()
    }

    WordleTogetherTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            val lettersViewModel: LettersViewModel = viewModel(
                factory = LettersViewModel.Companion.Factory(
                    gameSessionGateway = gameSessionGateway,
                    wordDictionary = wordDictionary
                )
            )
            val gameSessionViewModel: GameSessionViewModel = viewModel(
                factory = GameSessionViewModel.Companion.Factory(
                    playerProfileRepository = playerProfileRepository,
                    gameSessionGateway = gameSessionGateway
                )
            )

            DisposableEffect(gameSessionViewModel) {
                onDispose {
                    gameSessionViewModel.send(GameSessionEvent.DisconnectFromSession)
                }
            }

            NavHost(navController = navController, startDestination = RouteName.MAIN_MENU.string) {
                composable(RouteName.MAIN_MENU.string) {
                    MainMenuRoute(
                        gameSessionViewModel = gameSessionViewModel,
                        onNavigateToSinglePlayer = {
                            navController.navigate(RouteName.SINGLE_PLAYER.string)
                        },
                        onNavigateToLocalMultiplayer = {
                            navController.navigate(RouteName.MULTI_PLAYER_ONE_DEVICE.string)
                        },
                        onNavigateToOnlineMultiplayer = {
                            navController.navigate(RouteName.MULTI_PLAYER_TWO_DEVICES.string)
                        },
                        onExit = onExit
                    )
                }
                composable(RouteName.SINGLE_PLAYER.string) {
                    SinglePlayerRoute(
                        lettersViewModel = lettersViewModel,
                        onNavigateToMainMenu = {
                            navController.navigate(RouteName.MAIN_MENU.string)
                        }
                    )
                }
                composable(RouteName.MULTI_PLAYER_ONE_DEVICE.string) {
                    MultiPlayerOneDeviceRoute(
                        firstPlayerViewModel = lettersViewModel,
                        secondPlayerViewModel = viewModel(
                            factory = LettersViewModel.Companion.Factory(
                                gameSessionGateway = gameSessionGateway,
                                wordDictionary = wordDictionary
                            )
                        ),
                        onNavigateToMainMenu = {
                            navController.navigate(RouteName.MAIN_MENU.string)
                        }
                    )
                }
                composable(RouteName.MULTI_PLAYER_TWO_DEVICES.string) {
                    MultiPlayerTwoDevicesRoute(
                        lettersViewModel = lettersViewModel,
                        gameSessionViewModel = gameSessionViewModel,
                        onNavigateToMainMenu = {
                            navController.navigate(RouteName.MAIN_MENU.string)
                        }
                    )
                }
            }
        }
    }
}
