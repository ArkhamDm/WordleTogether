package com.example.scrambletogether.presentation.ui.multiplayer.dialogs.extras

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scrambletogether.R
import com.example.scrambletogether.domain.model.SessionItem
import com.example.scrambletogether.presentation.viewModel.FirestoreEvent
import com.example.scrambletogether.presentation.viewModel.FirestoreViewModel

@Composable
fun ListSessions(
    modifier: Modifier = Modifier,
    firestoreViewModel: FirestoreViewModel,
    backClick: () -> Unit,
    connectClick: () -> Unit,
) {
    val sessions by firestoreViewModel.sessionsList.collectAsState()
    firestoreViewModel.send(FirestoreEvent.GetSessions)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = backClick,
            modifier = Modifier
                .align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
            )
        }
        Card(
            modifier = Modifier

        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .fillMaxWidth()
                    .border(1.dp, Color.Black.copy(alpha = 0.2f), shape = ShapeDefaults.ExtraSmall),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (sessions.isLoading) {
                    item {
                        CircularProgressIndicator()
                    }
                } else {
                    items(sessions.sessions) { session ->
                        Session(
                            session = session,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .border(1.dp, color = Color.Black, shape = ShapeDefaults.Medium)
                                .height(40.dp)
                                .clickable {
                                    firestoreViewModel.send(FirestoreEvent.ConnectToSession(session.id))
                                    connectClick()
                                }
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                firestoreViewModel.send(FirestoreEvent.GetSessions)
            },
            modifier = Modifier
                .padding(bottom = 16.dp, top = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "refresh session")
                Text(text = stringResource(R.string.refresh))
            }
        }
    }
}

@Composable
fun Session(
    modifier: Modifier = Modifier,
    session: SessionItem
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp)
        ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null)
            Text(
                text = session.id
            )
        }
        Text(
            text = session.gamemode,
            color = Color.White
        )
        Row(
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Text(
                text = session.winTotal.toString(),
                color = Color.Green
            )
            Text(text = "/")
            Text(
                text = session.drawTotal.toString(),
                color = Color.White
            )
            Text(text = "/")
            Text(
                text = session.loseTotal.toString(),
                color = Color.Red
            )
        }
    }
}