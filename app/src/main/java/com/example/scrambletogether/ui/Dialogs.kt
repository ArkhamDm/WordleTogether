package com.example.scrambletogether.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.scrambletogether.R
import com.example.scrambletogether.data.RouteName
import com.example.scrambletogether.data.startWordleWords
import com.example.scrambletogether.data.words
import com.example.scrambletogether.firestore.data.SessionItem
import com.example.scrambletogether.firestore.ui.FirestoreViewModel
import com.example.scrambletogether.ui.theme.ScrambleTogetherTheme

@Composable
fun EndSingleGame(
    modifier: Modifier = Modifier,
    isWin: Boolean,
    correctWord: String,
    restartButton: () -> Unit = {},
    exitButton: () -> Unit = {}
) {
    Dialog(onDismissRequest = {}) {
        Card(modifier = modifier) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text =
                    if (isWin) stringResource(id = R.string.single_win)
                    else stringResource(id = R.string.single_lose),
                    fontSize = 36.sp,
                    modifier = Modifier
                        .padding(top = 6.dp, start = 40.dp, end = 40.dp, bottom = 64.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.your_word_was),
                        fontSize = 24.sp
                    )
                    Text(
                        text = correctWord,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                ) {
                    NoBorderButton(onClick = restartButton) {
                        Text(
                            text = stringResource(R.string.again_button)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    NoBorderButton(onClick = exitButton) {
                        Text(
                            text = stringResource(R.string.into_main_menu)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChangeGamemode(
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

@Composable
fun SetWordDialog(
    modifier: Modifier = Modifier,
    firestoreViewModel: FirestoreViewModel,
    onClick: () -> Unit
) {
    var isErrorWord by remember { mutableStateOf(true) }
    var wordToEnemy  by remember { mutableStateOf("") }

    if (wordToEnemy.length == startWordleWords.tryingWords[0].size) {
        isErrorWord = wordToEnemy !in words
    }

    Dialog(onDismissRequest = {}) {
        Card(modifier = modifier) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.enter_word)
                )
                TextField(
                    value = wordToEnemy,
                    onValueChange = {wordToEnemy = it.uppercase()},
                    modifier = Modifier.padding(16.dp),
                    isError = isErrorWord
                )

                Spacer(modifier = Modifier.height(32.dp))

                NoBorderButton(
                    onClick =
                    if (!isErrorWord) {
                        {
                            onClick()
                            firestoreViewModel.updateWord(wordToEnemy)
                        }
                    } else {
                        {}
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.done),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MultiplayerDialog(
    modifier: Modifier = Modifier,
    navController: NavController,
    firestoreViewModel: FirestoreViewModel,
    closeDialog: () -> Unit
) {
    var findClicked by remember { mutableStateOf(false) }
    var createClicked by remember { mutableStateOf(false) }
    var connectClicked by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = closeDialog) {
        Card(modifier = modifier) {
            if (findClicked) {
                ListSessions(
                    firestoreViewModel = firestoreViewModel,
                    connectClick = {
                        connectClicked = !connectClicked
                        findClicked = !findClicked
                    },
                    backClick = {
                        findClicked = !findClicked
                    }
                )
            } else if (createClicked ) {
                SetSessionFieldButton(firestoreViewModel = firestoreViewModel) {
                    navController.navigate(RouteName.MULTI_PLAYER.string)
                    createClicked = !createClicked
                }
            } else if (connectClicked) {
                navController.navigate(RouteName.MULTI_PLAYER.string)
            } else {
                CreateOrFindButtons(
                    createClick = {
                        createClicked = !createClicked
                    },
                    findClick = {
                        findClicked = !findClicked
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp, horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
fun CreateOrFindButtons(
    modifier: Modifier = Modifier,
    createClick: () -> Unit,
    findClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        ElevatedButton(
            onClick = createClick,
            shape = AbsoluteRoundedCornerShape(6.dp)
        ) {
            Text(
                text = stringResource(R.string.createSession),
                fontSize = 28.sp,
                modifier = Modifier
                    .padding(vertical = 20.dp)
            )
        }
        NoBorderButton(
            onClick = findClick,
            shape = AbsoluteRoundedCornerShape(6.dp)
        ) {
            Text(
                text = stringResource(R.string.findSession),
                fontSize = 26.sp,
                modifier = Modifier
                    .padding(vertical = 20.dp)
            )
        }
    }
}

@Composable
fun ListSessions(
    modifier: Modifier = Modifier,
    firestoreViewModel: FirestoreViewModel,
    backClick: () -> Unit,
    connectClick: () -> Unit,
) {
    val sessions by firestoreViewModel.sessionsList.collectAsState()
    firestoreViewModel.getSessions()

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
                                    firestoreViewModel.connectToSession(session.id)
                                    connectClick()
                                }
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                firestoreViewModel.getSessions()
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
                text = session.loseTotal.toString(),
                color = Color.Red
            )
        }
    }
}


@Composable
fun SetSessionFieldButton(
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
                firestoreViewModel.create(
                    SessionItem(
                        id = sessionId,
                        winTotal = firestoreViewModel.dataCounts.value.winCount,
                        loseTotal = firestoreViewModel.dataCounts.value.loseCount,
                        gamemode = "TwoSideMode"
                    )
                )
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

@Composable
fun NoBorderButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    shape: Shape = ButtonDefaults.shape,
    content: @Composable (RowScope.() -> Unit)
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        border = null,
        shape = shape,
        content = content
    )
}

@Composable
fun LoadingIndicator(dotsCount: Int) {
    Row {
        repeat(dotsCount) {
            Dot()
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
fun Dot() {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color = Color.White, shape = CircleShape)
    )
}

/*@Preview
@Composable
fun WhatPreview1() {
    ScrambleTogetherTheme {
        EndSingleGame(isWin = false, correctWord = "ПИЗДА")
    }
}
@Preview
@Composable
fun WhatPreview2() {
    ScrambleTogetherTheme {
        WaitForEnemyDialog()
    }
}
@Preview
@Composable
fun WhatPreview3() {
    ScrambleTogetherTheme {
        ChangeGamemode()
    }
}*/

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WhatPreview4() {
    ScrambleTogetherTheme {
        MultiplayerDialog(
            navController = rememberNavController(),
            closeDialog = {},
            firestoreViewModel = viewModel()
        )
    }
}
