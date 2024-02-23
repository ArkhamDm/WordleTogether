package com.example.scrambletogether.presentation.ui.multiplayer.dialogs.extras

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrambletogether.R
import com.example.scrambletogether.presentation.ui.common.NoBorderButton

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
fun OneOrTwoDevicesButtons(
    modifier: Modifier = Modifier,
    oneDeviceClick: () -> Unit,
    twoDevicesClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        ElevatedButton(
            onClick = oneDeviceClick,
            shape = AbsoluteRoundedCornerShape(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.one_device),
                contentDescription = "onePhone",
                modifier = Modifier
                    .height(75.dp)
                    .width(85.dp)
            )
        }
        NoBorderButton(
            onClick = twoDevicesClick,
            shape = AbsoluteRoundedCornerShape(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.two_devices),
                contentDescription = "twoPhones",
                modifier = Modifier
                    .height(75.dp)
                    .width(85.dp)
            )
        }
    }
}