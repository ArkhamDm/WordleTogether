package com.example.scrambletogether.presentation.ui.multiplayer.dialogs.extras

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.scrambletogether.R
import com.example.scrambletogether.presentation.ui.theme.ScrambleTogetherTheme

@Composable
fun TimerTimeChanger(
    modifier: Modifier = Modifier,
    getTimerTime: (Int) -> Unit
) {
    var seconds by remember { mutableIntStateOf(60) }
    var selectedList by remember { mutableStateOf(listOf(false, true, false, false)) }
    Card(modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Row {
                SecondsRadioButton(seconds = 15, selected = selectedList[0], onClick = {
                    seconds = 15
                    selectedList = List(selectedList.size) { index ->
                        index == 0
                    }
                })
                SecondsRadioButton(seconds = 60, selected = selectedList[1], onClick = {
                    seconds = 60
                    selectedList = List(selectedList.size) { index ->
                        index == 1
                    }
                })
                SecondsRadioButton(seconds = 90, selected = selectedList[2], onClick = {
                    seconds = 90
                    selectedList = List(selectedList.size) { index ->
                        index == 2
                    }
                })
                SecondsRadioButton(seconds = 120, selected = selectedList[3], onClick = {
                    seconds = 120
                    selectedList = List(selectedList.size) { index ->
                        index == 3
                    }
                })
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Текущий таймер: $seconds секунд"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { getTimerTime(seconds) }) {
                Text(
                    text = stringResource(id = R.string.done)
                )
            }
        }

    }
}

@Composable
fun SecondsRadioButton(
    modifier: Modifier = Modifier,
    seconds: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "$seconds"
        )
        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

@Preview
@Composable
fun TimerTimeChangerPreview() {
    ScrambleTogetherTheme {
        TimerTimeChanger(getTimerTime = {})
    }
}