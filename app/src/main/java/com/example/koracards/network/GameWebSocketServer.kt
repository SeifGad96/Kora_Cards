package com.example.koracards.network

import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import java.net.ServerSocket

/**
 * Host-side Ktor CIO WebSocket server.
 *
 * Accepts a single guest connection on `/game`. All incoming frames are decoded as
 * [GameMessage] and emitted on [incomingMessages]. Use [send] to push messages to the guest.
 *
 * Call [start] with a coroutine scope, then read [port] to pass to [UdpBroadcaster].
 * Call [stop] when the game ends or the host navigates away.
 */
class GameWebSocketServer {

    private var engine: EmbeddedServer<*, *>? = null

    private val _incomingMessages = MutableSharedFlow<GameMessage>(extraBufferCapacity = 64)
    /** Incoming [GameMessage] stream from the connected guest. Collect in a coroutine. */
    val incomingMessages: SharedFlow<GameMessage> = _incomingMessages

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Lost)
    /**
     * Guest connection state.
     * - [ConnectionState.Lost]      — server started, no guest yet (or guest disconnected)
     * - [ConnectionState.Connected] — guest is connected and exchanging messages
     */
    val connectionState: StateFlow<ConnectionState> = _connectionState

    /** Fixed port for the game WebSocket server. */
    val port: Int = 47_778

    @Volatile private var guestSession: WebSocketSession? = null

    /**
     * Starts the Ktor CIO server on the fixed port.
     * Returns immediately; the server runs on Ktor's internal engine threads.
     */
    fun start() {
        engine = embeddedServer(CIO, port = port, host = "0.0.0.0") {
            install(WebSockets)
            routing {
                webSocket("/game") {
                    guestSession = this
                    _connectionState.value = ConnectionState.Connected
                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                runCatching {
                                    val msg = NetworkJson.decodeFromString<GameMessage>(frame.readText())
                                    _incomingMessages.tryEmit(msg)
                                }
                            }
                        }
                    } finally {
                        guestSession = null
                        _connectionState.value = ConnectionState.Lost
                    }
                }
            }
        }.start(wait = false)
    }

    /**
     * Sends [message] to the connected guest.
     * Silently no-ops if no guest is currently connected.
     */
    suspend fun send(message: GameMessage) {
        guestSession?.send(NetworkJson.encodeToString<GameMessage>(message))
    }

    /** Gracefully shuts down the server and releases the port. */
    fun stop() {
        engine?.stop(gracePeriodMillis = 500L, timeoutMillis = 1_000L)
        engine = null
        guestSession = null
        _connectionState.value = ConnectionState.Lost
    }
}
