package com.example.scrambletogether.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.scrambletogether.R
import com.example.scrambletogether.data.words
import com.example.scrambletogether.ui.theme.ScrambleTogetherTheme
import com.example.scrambletogether.ui.viewModels.LettersViewModel
import com.example.scrambletogether.utils.FirebaseUtils
import kotlinx.coroutines.delay

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
fun SetWordDialog(
    modifier: Modifier = Modifier,
    lettersViewModel: LettersViewModel = viewModel(),
    isWait: MutableState<Boolean>
) {
    var waitForEnemy by remember { mutableStateOf(false) }
    var isErrorWord by remember { mutableStateOf(false) }
    var dotsCount by remember { mutableIntStateOf(0) }
    val wordFromEnemy by lettersViewModel.wordFromEnemy.collectAsState()

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = modifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var wordToEnemy  by remember { mutableStateOf("") }
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
                    onClick = {
                        isErrorWord = wordToEnemy !in words
                        if (!isErrorWord) {
                            lettersViewModel.waitWord(true)
                            FirebaseUtils.setWord(wordToEnemy, lettersViewModel.enemyFirebaseId!!)
                            waitForEnemy = true
                        }
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.done),
                        fontSize = 16.sp
                    )
                }

                if (waitForEnemy) {
                    Spacer(modifier = Modifier.height(32.dp))

                    LaunchedEffect(key1 = true) {
                        // delay for adding new dots
                        while (true) {
                            delay(500)
                            dotsCount = dotsCount % 5 + 1
                        }
                    }
                    LoadingIndicator(dotsCount = dotsCount)
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (wordFromEnemy.length == 5) {
                    isWait.value = false
                    lettersViewModel.waitWord(false)
                }
            }
        }
    }
}

@Composable
fun CreateOrJoinGame(
    modifier: Modifier = Modifier,
    navController: NavController,
    lettersViewModel: LettersViewModel = viewModel(),
    closeDialog: () -> Unit
) {
    if (lettersViewModel.firebaseId == null) lettersViewModel.firebaseId = FirebaseUtils.create()

    var enemyFirebaseId by remember { mutableStateOf("") }
    var wordToEnemy  by remember { mutableStateOf("") }
    var isErrorFirebase by remember { mutableStateOf(true) }
    var isErrorWord by remember { mutableStateOf(false) }
    var clickedNext by remember { mutableStateOf(false) }

    val wordFromEnemy by lettersViewModel.wordFromEnemy.collectAsState()

    Dialog(onDismissRequest = closeDialog) {
        if (!clickedNext) {
            Card(modifier = modifier) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.your_code),
                        modifier = Modifier.padding(12.dp)
                    )
                    Card(modifier = Modifier.padding(6.dp)) {
                        Text(
                            text = lettersViewModel.firebaseId!!,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Divider(thickness = 5.dp)

                    Text(
                        text = stringResource(R.string.enemy_code),
                        modifier = Modifier.padding(12.dp)
                    )
                    Card {
                        TextField(
                            value = enemyFirebaseId,
                            onValueChange = { enemyFirebaseId = it.uppercase() },
                            isError = isErrorFirebase,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                    Divider(thickness = 5.dp)

                    Text(
                        text = stringResource(R.string.guess_word),
                        modifier = Modifier.padding(12.dp)
                    )
                    Card {
                        TextField(
                            value = wordToEnemy,
                            onValueChange = {
                                wordToEnemy = it.uppercase()
                            },
                            isError = isErrorWord,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    NoBorderButton(
                        onClick = {
                            isErrorWord = wordToEnemy !in words
                            FirebaseUtils.isExistAndFree(firebaseId = enemyFirebaseId) { exists ->
                                isErrorFirebase = !exists
                            }

                            if (!isErrorWord and !isErrorFirebase) {
                                lettersViewModel.waitWord(true)
                                FirebaseUtils.setWord(wordToEnemy, enemyFirebaseId)
                                lettersViewModel.enemyFirebaseId = enemyFirebaseId
                                clickedNext = true
                            }
                        },
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.done)
                        )
                    }
                }
            }
        } else {
            WaitForEnemyDialog(code = lettersViewModel.firebaseId!!)
            if (wordFromEnemy.length == 5) {
                lettersViewModel.waitWord(false)
                navController.navigate("multiPlayer/${lettersViewModel.enemyFirebaseId}")
                FirebaseUtils.setStatus(true, lettersViewModel.firebaseId!!)
            }
        }
    }
}

@Composable
fun WaitForEnemyDialog(
    modifier: Modifier = Modifier,
    code: String = "7gdvDv"
) {
    var dotsCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = true) {
        // delay for adding new dots
        while (true) {
            delay(500)
            dotsCount = dotsCount % 5 + 1
        }
    }

    Card(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.wait_enemy),
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = code,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            LoadingIndicator(dotsCount)

            Spacer(modifier = Modifier.height(32.dp))
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
fun CreateOrJoinGameDialog(
    modifier: Modifier = Modifier,
    navController: NavController,
    /*lettersViewModel: LettersViewModel = viewModel(),*/
    closeDialog: () -> Unit
) {
    Dialog(onDismissRequest = closeDialog) {
        Card(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedButton(
                    onClick = { /*TODO*/ },
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
                    onClick = { /*TODO*/ },
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
    }
}

@Composable
fun ListSessionsDialog(
    modifier: Modifier = Modifier,
    navController: NavController,
    /*lettersViewModel: LettersViewModel = viewModel(),*/
    closeDialog: () -> Unit
) {
    Dialog(onDismissRequest = closeDialog) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

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
        CreateOrJoinGameDialog(
            navController = rememberNavController(),
            closeDialog = {},
            modifier = Modifier.fillMaxHeight(0.35f)
        )
    }
}
