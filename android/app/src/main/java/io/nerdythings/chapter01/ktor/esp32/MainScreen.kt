package io.nerdythings.chapter01.ktor.esp32

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

private val textStyle = TextStyle(
    color = Color(0xFF2196F3),
    fontSize = 16.sp,
    textAlign = TextAlign.Center,
    fontWeight = FontWeight.Bold,
)

private val textLight = TextStyle(
    color = Color(0xFFA5A5A5),
    fontSize = 16.sp,
    textAlign = TextAlign.Center,
    fontWeight = FontWeight.Bold,
)

private val mainColor = Color(0xff1e1f22)

@Composable
fun MainScreen(
    devices: List<WebSocketServer.ConnectedDevice>,
    ipv4Address: String?,
    port: String?,
    switchDevice: (String, Boolean) -> Unit
) {
    MaterialTheme {
        Column(
            Modifier
                .background(mainColor)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (ipv4Address.isNullOrBlank()) {
                Text(
                    text = stringResource(id = R.string.network_not_found),
                    style = textStyle,
                )
            } else {
                Text(
                    text = "${stringResource(id = R.string.connect_esp)}:\nws://$ipv4Address:$port",
                    style = textStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
            if (devices.isEmpty()) {
                NobodyConnectedScreen()
            } else {
                ConnectedDeviceList(devices, switchDevice)
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.nerdy_things_channel),
                contentDescription = "Logo"
            )
            Text(
                text = stringResource(id = R.string.nerdy_things),
                style = textLight,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun ConnectedDeviceList(
    devices: List<WebSocketServer.ConnectedDevice>,
    switchDevice: (String, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        for (device in devices) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val deviceLastChars = device.id.substring(
                    max(0, device.id.length - 4),
                    device.id.length,
                )
                Text(
                    text = "${stringResource(id = R.string.relay_device)}: $deviceLastChars",
                    style = textStyle.copy(textAlign = TextAlign.Start),
                    modifier = Modifier
                        .padding(12.dp)
                        .weight(1f),
                )
                Switch(
                    checked = device.isEnabled,
                    onCheckedChange = {
                        switchDevice.invoke(device.id, it)
                    },
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

@Composable
fun NobodyConnectedScreen() {
    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nobody connected",
            style = textStyle,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNobodyConnectedScreen() {
    MaterialTheme {
        MainScreen(
            listOf(),
            "192.168.1.1",
            "8080"
        ) { _, _ -> }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDevicesScreen() {
    MaterialTheme {
        MaterialTheme {
            MainScreen(
                listOf(
                    WebSocketServer.ConnectedDevice("id", isEnabled = false)
                ),
                "192.168.1.1",
                "8080"
            ) { _, _ -> }
        }
    }
}