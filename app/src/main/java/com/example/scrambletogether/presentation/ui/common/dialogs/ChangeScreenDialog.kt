package com.example.scrambletogether.presentation.ui.common.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.scrambletogether.R
import com.example.scrambletogether.presentation.ui.common.NoBorderButton

@Composable
fun ChangeScreenDialog(
    modifier: Modifier = Modifier,
    closeDialog: () -> Unit = {},
    navigate: () -> Unit = {}
) {
    Dialog(onDismissRequest = closeDialog) {
        Card(modifier = modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.exit_gamemode),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.sure_to_change),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(60.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NoBorderButton(onClick = navigate) {
                        Text(
                            text = stringResource(R.string.yes)
                        )
                    }
                    NoBorderButton(onClick = closeDialog) {
                        Text(
                            text = stringResource(R.string.close)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}