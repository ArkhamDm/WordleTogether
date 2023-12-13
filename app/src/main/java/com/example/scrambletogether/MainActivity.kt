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
import com.example.scrambletogether.data.words
import com.example.scrambletogether.ui.MainMenu
import com.example.scrambletogether.ui.MultiPlayer
import com.example.scrambletogether.ui.SinglePlayer
import com.example.scrambletogether.ui.theme.ScrambleTogetherTheme
import com.example.scrambletogether.ui.viewModels.LettersViewModel

class MainActivity : ComponentActivity() {
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

                    val lettersViewModel: LettersViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "mainMenu") {
                        composable("mainMenu") { MainMenu(navController = navController, lettersViewModel = lettersViewModel) }
                        composable("singlePlayer") { SinglePlayer(navController = navController, lettersViewModel = lettersViewModel) }
                        composable("multiPlayer/{enemyFirebaseId}") {
                            MultiPlayer(
                                enemyFirebase = it.arguments?.getString("enemyFirebaseId") ?: "undefined",
                                navController = navController,
                                lettersViewModel = lettersViewModel
                            )
                        }

                    }
                }
            }
        }
    }
}