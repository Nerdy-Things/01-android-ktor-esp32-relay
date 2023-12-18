package io.nerdythings.chapter01.ktor.esp32

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ComposeView(this).apply {
            setContent {
                val devices by mainViewModel.connectedDevices.collectAsState()
                val ipv4Address by mainViewModel.ipAddress.collectAsState()
                MainScreen(
                    devices,
                    ipv4Address,
                    mainViewModel.port.toString(),
                    mainViewModel::switchDevice
                )
            }
        })
    }
}
