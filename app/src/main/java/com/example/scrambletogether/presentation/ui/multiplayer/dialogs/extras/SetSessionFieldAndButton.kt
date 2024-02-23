package com.example.scrambletogether.presentation.ui.multiplayer.dialogs.extras

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrambletogether.R
import com.example.scrambletogether.domain.model.SessionItem
import com.example.scrambletogether.presentation.ui.common.NoBorderButton
import com.example.scrambletogether.presentation.viewModel.FirestoreEvent
import com.example.scrambletogether.presentation.viewModel.FirestoreViewModel

@Composable
fun SetSessionFieldAndButton(
    modifier: Modifier = Modifier,
    firestoreViewModel: FirestoreViewModel,
    onClick: () -> Unit
) {
    var sessionId  by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.enter_session_name)
        )
        TextField(
            value = sessionId,
            onValueChange = {sessionId = it},
            modifier = Modifier.padding(16.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        NoBorderButton(
            onClick = {
                firestoreViewModel.send(
                    FirestoreEvent.CreateSession(
                        SessionItem(
                            id = sessionId,
                            winTotal = firestoreViewModel.dataCounts.value.winCount,
                            loseTotal = firestoreViewModel.dataCounts.value.loseCount,
                            drawTotal = firestoreViewModel.dataCounts.value.drawCount,
                            gamemode = "TwoSideMode"
                        )
                    ))
                onClick()
            }
        ) {
            Text(
                text = stringResource(id = R.string.done),
                fontSize = 16.sp
            )
        }
    }
}