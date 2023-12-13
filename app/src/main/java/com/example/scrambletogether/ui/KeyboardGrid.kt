package com.example.scrambletogether.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scrambletogether.data.words
import com.example.scrambletogether.ui.viewModels.LettersViewModel
import kotlinx.coroutines.delay

@Composable
fun KeyboardGrid(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    lettersViewModel: LettersViewModel = viewModel(),
    isMultiplayer: Boolean = false
) {
    val context = LocalContext.current
    val keyboardLetter by lettersViewModel.keyboardLetters.collectAsState()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            for (letter in keyboardLetter[0]) {
                Letter(
                    letter = letter.letter,
                    color = letter.color,
                    fontSize = fontSize,
                    modifierToCard = Modifier
                        .padding(3.dp)
                        .clickable { lettersViewModel.addLetter(letter.letter, isMultiplayer) }
                        .width(27.dp),
                    modifierToText = Modifier
                        .padding(6.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            for (letter in keyboardLetter[1]) {
                Letter(
                    letter = letter.letter,
                    color = letter.color,
                    fontSize = fontSize,
                    modifierToCard = Modifier
                        .padding(3.dp)
                        .clickable { lettersViewModel.addLetter(letter.letter, isMultiplayer) }
                        .width(30.dp),
                    modifierToText = Modifier
                        .padding(6.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Card(
                colors = CardDefaults.cardColors(Color.Gray),
                shape = ShapeDefaults.ExtraSmall
            ){
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "backspace", tint = Color.White,
                    modifier = Modifier
                        .padding(top = 6.dp, start = 16.dp, end = 16.dp, bottom = 6.dp)
                        .clickable { lettersViewModel.deleteLetter(isMultiplayer) }
                )
            }
            for (letter in keyboardLetter[2]) {
                Letter(
                    letter = letter.letter,
                    color = letter.color,
                    fontSize = fontSize,
                    modifierToCard = Modifier
                        .padding(3.dp)
                        .clickable { lettersViewModel.addLetter(letter.letter, isMultiplayer) }
                        .width(26.dp),
                    modifierToText = Modifier
                        .padding(6.dp)
                )
            }

            var isError by remember { mutableStateOf(false) }
            var isBlinking by remember { mutableStateOf(false) }
            val cardColor = if (isError) {
                if (isBlinking) Color.Red else Color.Gray
            } else {
                Color.Gray
            }

            Card(
                colors = CardDefaults.cardColors(cardColor),
                shape = ShapeDefaults.ExtraSmall
            ) {
                val iconColor = if (isError) {
                    if (isBlinking) Color.White else Color.Gray
                } else {
                    Color.White
                }

                LaunchedEffect(isError, rememberUpdatedState(iconColor)) {
                    if (isError) {
                        isBlinking = true
                        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                            vibratorManager.defaultVibrator
                        } else {
                            @Suppress("DEPRECATION")
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        }
                        vibrate(vibrator)
                        delay(300) // Duration for blinking
                        isBlinking = false
                    }
                    isError = false
                }

                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "done",
                    tint = iconColor,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)
                        .clickable {
                            if (
                                lettersViewModel.wordleWords.value.madeWordInLine &&
                                String(
                                    lettersViewModel.wordleWords.value
                                        .tryingWords[lettersViewModel.wordleWords.value.wordsInLine]
                                        .map { it.letter }
                                        .toCharArray()
                                ) in words
                            ) {
                                lettersViewModel.checkAnswer(isMultiplayer)
                            } else {
                                isError = !isError
                            }
                        }
                )
            }
        }
    }
}

fun vibrate(vibrator: Vibrator) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(200)
    }
}