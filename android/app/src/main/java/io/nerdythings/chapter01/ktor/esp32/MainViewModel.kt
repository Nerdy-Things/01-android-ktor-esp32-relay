package io.nerdythings.chapter01.ktor.esp32

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val webSocketServer: WebSocketServer,
) : ViewModel() {

    val connectedDevices = webSocketServer.connectedDevices
    val ipAddress = networkMonitor.ipAddress
    val port = WebSocketServer.PORT

    init {
        networkMonitor.start()
        webSocketServer.start()
    }

    override fun onCleared() {
        super.onCleared()
        webSocketServer.stop()
        networkMonitor.stop()
    }

    fun switchDevice(id: String, targetState: Boolean) {
        webSocketServer.switchDeviceState(id, targetState)
    }
}
