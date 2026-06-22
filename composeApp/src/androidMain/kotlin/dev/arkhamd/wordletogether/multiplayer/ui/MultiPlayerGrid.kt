package dev.arkhamd.wordletogether.multiplayer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arkhamd.wordletogether.R
import dev.arkhamd.wordletogether.multiplayer.ui.dialogs.extras.LoadingIndicator
import dev.arkhamd.wordletogether.wordle.domain.Letter
import dev.arkhamd.wordletogether.wordle.ui.common.ListWords
import kotlinx.coroutines.delay

@Composable
fun DoubleGrid(
    modifier: Modifier = Modifier,
    enemyCurrentWord: String,
    selfCurrentWord: String,
    yourWords: List<List<Letter>>,
    enemyWords: List<List<Letter>>,
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
                        modifier = Modifier.width(175.dp)
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
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(500)
                        dotsCount = dotsCount % 5 + 1
                    }
                }
                LoadingIndicator(dotsCount = dotsCount)
            } else {
                ListWords(
                    tryingWords = yourWords,
                    fontSize = 24.sp,
                    padding = 1.dp
                )
                Text(
                    text = stringResource(R.string.word) + ": ?????",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        VerticalDivider(
            color = Color.Black,
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight(0.4f),
            thickness = 2.dp
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
                tryingWords = enemyWords,
                fontSize = 24.sp,
                padding = 1.dp
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
