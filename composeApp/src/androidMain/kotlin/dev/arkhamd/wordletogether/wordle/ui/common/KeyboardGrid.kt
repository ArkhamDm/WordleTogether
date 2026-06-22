package dev.arkhamd.wordletogether.wordle.ui.common

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arkhamd.wordletogether.wordle.domain.Letter as WordleLetter
import dev.arkhamd.wordletogether.wordle.ui.model.toUiColor
import kotlinx.coroutines.delay

@Composable
fun KeyboardGrid(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    keyboardLetters: List<List<WordleLetter>>,
    onAddLetter: (Char) -> Unit,
    onDeleteLetter: () -> Unit,
    onSubmitGuess: () -> Boolean,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            for (letter in keyboardLetters.getOrElse(0) { emptyList() }) {
                Letter(
                    letter = letter.letter,
                    color = letter.feedback.toUiColor(),
                    fontSize = fontSize,
                    modifier = Modifier
                        .padding(3.dp)
                        .clickable(enabled = enabled) {
                            onAddLetter(letter.letter)
                        }
                        .width(27.dp),
                    modifierToText = Modifier
                        .padding(6.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            for (letter in keyboardLetters.getOrElse(1) { emptyList() }) {
                Letter(
                    letter = letter.letter,
                    color = letter.feedback.toUiColor(),
                    fontSize = fontSize,
                    modifier = Modifier
                        .padding(3.dp)
                        .clickable(enabled = enabled) {
                            onAddLetter(letter.letter)
                        }
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
        ) {
            Card(
                colors = CardDefaults.cardColors(Color.Gray),
                shape = ShapeDefaults.ExtraSmall
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "backspace",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(top = 6.dp, start = 16.dp, end = 16.dp, bottom = 6.dp)
                        .clickable(enabled = enabled) {
                            onDeleteLetter()
                        }
                )
            }
            for (letter in keyboardLetters.getOrElse(2) { emptyList() }) {
                Letter(
                    letter = letter.letter,
                    color = letter.feedback.toUiColor(),
                    fontSize = fontSize,
                    modifier = Modifier
                        .padding(3.dp)
                        .clickable(enabled = enabled) {
                            onAddLetter(letter.letter)
                        }
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

                LaunchedEffect(isError) {
                    if (isError) {
                        isBlinking = true
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
                        .clickable(enabled = enabled) {
                            if (!onSubmitGuess()) {
                                isError = true
                            }
                        }
                )
            }
        }
    }
}
