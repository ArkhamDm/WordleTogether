@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.scrambletogether.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrambletogether.data.LetterDataClass
import com.example.scrambletogether.ui.theme.ScrambleTogetherTheme

@Composable
fun ListWords(
    tryingWords: Array<Array<LetterDataClass>>,
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
    word: Array<LetterDataClass>,
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
            Letter(
                letter = letter.letter,
                color = letter.color,
                fontSize = fontSize,
                modifierToCard = Modifier
                    .padding(padding)
                    .size(width = (letterSize.value * 1.25).dp, height = (letterSize.value * 1.35).dp)
            )
        }
    }
}

@Composable
fun Letter(
    letter: Char,
    color: Color,
    fontSize: TextUnit,
    modifierToCard: Modifier = Modifier,
    modifierToText: Modifier = Modifier
) {
    Card(
        modifier = modifierToCard,
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
                         LetterDataClass()
                     }
            },
            fontSize = 54.sp
        )
    }
}