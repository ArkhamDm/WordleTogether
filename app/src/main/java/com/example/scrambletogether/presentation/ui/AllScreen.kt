package com.example.scrambletogether.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.scrambletogether.R
import com.example.scrambletogether.data.model.RouteName
import com.example.scrambletogether.presentation.ui.multiplayer.dialogs.MultiPlayerChooseDialog
import com.example.scrambletogether.presentation.ui.theme.ScrambleTogetherTheme
import com.example.scrambletogether.presentation.ui.theme.md_theme_dark_onTertiary
import com.example.scrambletogether.presentation.viewModel.FirestoreViewModel
import kotlin.system.exitProcess

@Composable
fun ChooseGameModeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    icon: Int,
    contentDescription: String,
    color: Color = Color.Red
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .drawWithContent {
                drawContent()
                drawNeonStroke(16.dp, color, neonWidth = 20f)
            }
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier
                .height(140.dp)
                .width(120.dp)
                .padding(8.dp)
        )
    }
}

@Composable
fun MainMenu(
    modifier: Modifier = Modifier,
    navController: NavController,
    firestoreViewModel: FirestoreViewModel
) {
    var createGameDialog by remember { mutableStateOf(false) }

    if (createGameDialog) {
        MultiPlayerChooseDialog(navController = navController, firestoreViewModel = firestoreViewModel) {
            createGameDialog = !createGameDialog
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                textAlign = TextAlign.Center,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = md_theme_dark_onTertiary,
                modifier = Modifier
                    .padding(top = 36.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.eyes1),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .height(75.dp)
                    .width(300.dp)
                    .border(BorderStroke(1.dp, Color.White))
            )
        }

        Row {
            ChooseGameModeButton(
                onClick = { navController.navigate(RouteName.SINGLE_PLAYER.string) },
                icon = R.drawable.person,
                contentDescription = "singlePlayer"
            )
            Spacer(modifier = Modifier.width(64.dp))
            ChooseGameModeButton(
                onClick = { createGameDialog = true },
                icon = R.drawable.two_players_game_interface_symbol,
                contentDescription = "multiPlayer"
            )
        }

        OutlinedCard(
            modifier = modifier
                .padding(bottom = 36.dp),
            onClick = { exitProcess(0) },
            shape = AbsoluteRoundedCornerShape(
                corner = CornerSize(8.dp)
            )
        ) {
            Text(
                text = stringResource(R.string.exit),
                fontSize = 24.sp,
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = 32.dp,
                    end = 32.dp,
                    bottom = 8.dp
                ),
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

fun ContentDrawScope.drawNeonStroke(radius: Dp, color: Color, neonWidth: Float) {
    this.drawIntoCanvas {
        val paint =
            Paint().apply {
                style = PaintingStyle.Stroke
                strokeWidth = neonWidth
            }

        val frameworkPaint =
            paint.asFrameworkPaint()

        this.drawIntoCanvas {
            frameworkPaint.color = color.copy(alpha = 0f).toArgb()
            frameworkPaint.setShadowLayer(
                radius.toPx(), 0f, 0f, color.copy(alpha = .5f).toArgb()
            )
            it.drawRoundRect(
                left = 0f,
                right = size.width,
                bottom = size.height,
                top = 0f,
                radiusY = radius.toPx(),
                radiusX = radius.toPx(),
                paint = paint
            )
            drawRoundRect(
                color = color,
                size = size,
                cornerRadius = CornerRadius(radius.toPx(), radius.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Preview(showSystemUi = true,
    showBackground = true,
)
@Composable
fun MainMenuPreview() {
    ScrambleTogetherTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainMenu(navController = rememberNavController(), firestoreViewModel = viewModel())
        }
    }
}
