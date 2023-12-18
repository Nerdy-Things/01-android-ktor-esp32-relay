package io.nerdythings.chapter01.ktor.esp32

import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketServer @Inject constructor() {

    // Hold a reference to websocket server
    private val engine = AtomicReference<NettyApplicationEngine>()

    // Hold a mutable list of connected devices
    private val _connectedDevices = MutableStateFlow<List<ConnectedDevice>>(listOf())
    // Expose an immutable list of connected devices
    val connectedDevices = _connectedDevices.asStateFlow()
    // We don't want to block a Main Thread
    private val scope = CoroutineScope(Dispatchers.IO)

    // Store sockets to be able to send a message
    private val openedSockets = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    // Store devices states
    private val relayStates = ConcurrentHashMap<String, Boolean>()

    // Some magic to synchronize threads
    private val mutex = Mutex()

    /**
     * Starts a WebSockets server
     */
    fun start() = scope.launch {
        engine.set(
            // Starting webserver
            embeddedServer(Netty, port = PORT) {
                // Installing WebSockets plug in
                install(WebSockets)
                // Create a routing map
                routing {
                    // We need only main socket, connected to WS://IP:PORT/
                    webSocket("/") {
                        // Create a UUID for a connected device
                        val uuid = UUID.randomUUID().toString()
                        // Send OK message
                        send("OK")
                        mutex.withLock {
                            // Store socket connection and a device state
                            openedSockets[uuid] = this
                            relayStates[uuid] = false
                            updateActiveConnections()
                        }
                        // Looping in a messages
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val receivedText = frame.readText()
                                // We don't need to handle responses
                                println("Server received: $receivedText")
                            }
                        }
                        // Connection is closed, clearing state
                        mutex.withLock {
                            openedSockets.remove(uuid)
                            updateActiveConnections()
                        }
                    }
                }
            }.start(wait = true)
        )
    }

    /**
     * Creates a list of connected websocket clients and pass it to a Flow (and listeners)
     */
    private fun updateActiveConnections() {
        val connectedDevices = mutableListOf<ConnectedDevice>()
        for ((key, _) in openedSockets) {
            connectedDevices.add(ConnectedDevice(key, relayStates[key] ?: false))
        }
        connectedDevices.sortBy { it.id }
        _connectedDevices.value = connectedDevices
    }

    /**
     * Stops a WebSockets server
     */
    fun stop() {
        engine.get()?.stop()
        engine.set(null)
    }

    /**
     * Sends a message to a socket of a device (if connected)
     */
    fun switchDeviceState(id: String, targetState: Boolean) = scope.launch {
        mutex.withLock {
            if (targetState) {
                openedSockets[id]?.send("ON")
            } else {
                openedSockets[id]?.send("OFF")
            }
            relayStates[id] = targetState
            updateActiveConnections()
        }
    }

    companion object {
        const val PORT = 8080
    }

    data class ConnectedDevice(
        val id: String,
        val isEnabled: Boolean,
    )
}