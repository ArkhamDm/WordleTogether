package com.example.scrambletogether

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scrambletogether.data.model.RouteName
import com.example.scrambletogether.data.model.StartValues.words
import com.example.scrambletogether.data.repository.UserRepositoryImpl
import com.example.scrambletogether.presentation.ui.MainMenu
import com.example.scrambletogether.presentation.ui.multiplayer.MultiPlayerOneDevice
import com.example.scrambletogether.presentation.ui.multiplayer.MultiPlayerTwoDevices
import com.example.scrambletogether.presentation.ui.singleplayer.SinglePlayer
import com.example.scrambletogether.presentation.ui.theme.ScrambleTogetherTheme
import com.example.scrambletogether.presentation.viewModel.FirestoreEvent
import com.example.scrambletogether.presentation.viewModel.FirestoreViewModel
import com.example.scrambletogether.presentation.viewModel.LettersViewModel

class MainActivity : ComponentActivity() {
    private val userPreferences by lazy { UserRepositoryImpl(context = this) }
    private lateinit var firestoreViewModel: FirestoreViewModel
    private lateinit var lettersViewModel: LettersViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        words = applicationContext.assets.open("rus_5letters.json")
            .bufferedReader().readLines()
            .map { it.replace("[^а-я]".toRegex(), "").uppercase() }
            .filter { it.isNotEmpty() }

        setContent {
            ScrambleTogetherTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    lettersViewModel = viewModel()
                    firestoreViewModel = viewModel(
                        factory = FirestoreViewModel.Companion.SettingsViewModelFactory(userPreferences)
                    )

                    NavHost(navController = navController, startDestination = RouteName.MAIN_MENU.string) {
                        composable(RouteName.MAIN_MENU.string) {
                            MainMenu(
                                navController = navController,
                                firestoreViewModel = firestoreViewModel
                            )
                        }
                        composable(RouteName.SINGLE_PLAYER.string) {
                            SinglePlayer(
                                navController = navController,
                                lettersViewModel = lettersViewModel
                            )
                        }
                        composable(RouteName.MULTI_PLAYER_ONE_DEVICE.string) {
                            MultiPlayerOneDevice(
                                navController = navController,
                                firstPlayerViewModel = lettersViewModel,
                                secondPlayerViewModel = viewModel()
                            )
                        }
                        composable(RouteName.MULTI_PLAYER_TWO_DEVICES.string) {
                            MultiPlayerTwoDevices(
                                navController = navController,
                                lettersViewModel = lettersViewModel,
                                firestoreViewModel = firestoreViewModel
                            )
                        }

                    }
                }
            }
        }
    }

    override fun onDestroy() {
        firestoreViewModel.send(FirestoreEvent.DisconnectFromSession)
        super.onDestroy()
    }
}