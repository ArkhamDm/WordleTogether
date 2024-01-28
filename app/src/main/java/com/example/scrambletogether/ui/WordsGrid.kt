@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.scrambletogether.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrambletogether.data.ColorLetter
import com.example.scrambletogether.data.Letter
import com.example.scrambletogether.ui.theme.ScrambleTogetherTheme

@Composable
fun ListWords(
    tryingWords: Array<Array<Letter>>,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 54.sp,
    padding: Dp = 3.dp
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(tryingWords) { word ->
            Word(word = word, fontSize = fontSize, padding = padding)
        }
    }
}

@Composable
fun Word(
    word: Array<Letter>,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    padding: Dp
) {
    //to size same as char
    val density = LocalDensity.current
    val letterSize = with(density) { (fontSize).toDp() }

    Row(
        modifier = modifier
    ) {
        for (letter in word) {

            //animation of revert and change color card
            val transition =
                updateTransition(targetState = letter.color, label = "RevertCardTransition")

            val rotationState by transition.animateFloat(
                transitionSpec = {
                    keyframes {
                        durationMillis = 1000
                        0f at 0 with LinearOutSlowInEasing
                    }
                }, label = "RevertCardTransition"
            ) {
                if (it == ColorLetter.None.color) 0f else 180f
            }

            val colorState by transition.animateColor(
                transitionSpec = {
                    keyframes {
                        durationMillis = 1000
                        ColorLetter.None.color at 200 with LinearOutSlowInEasing
                    }
                }, label = "RevertCardTransition"
            ) {
                it
            }
            //

            Letter(
                letter = letter.letter,
                color = colorState,
                fontSize = fontSize,
                modifier = Modifier
                    .padding(padding)
                    .size(width = (letterSize.value * 1.25).dp, height = (letterSize.value * 1.35).dp)
                    .graphicsLayer(rotationY = rotationState),
                modifierToText = Modifier
                    .graphicsLayer(rotationY = rotationState)
            )
        }
    }
}

@Composable
fun Letter(
    letter: Char,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    modifierToText: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(color),
        shape = ShapeDefaults.ExtraSmall
    ) {
        Text(
            text = letter.toString(),
            color = Color.White,
            modifier = modifierToText
                .align(Alignment.CenterHorizontally),
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    ScrambleTogetherTheme {
        ListWords(
            Array(6) {
                     Array(5) {
                         Letter()
                     }
            },
            fontSize = 54.sp
        )
    }
}