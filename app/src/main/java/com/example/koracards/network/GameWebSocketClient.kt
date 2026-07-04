package com.example.koracards.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

/** Maximum number of reconnection attempts before transitioning to [ConnectionState.Lost]. */
private const val MAX_RECONNECT_ATTEMPTS = 5

/**
 * Guest-side Ktor OkHttp WebSocket client.
 *
 * Connects to `ws://{hostIp}:{serverPort}/game` and handles automatic reconnection
 * with exponential back-off (1s → 2s → 4s → 8s → 16s) on unexpected disconnects.
 *
 * Call [connect] to start. Collect [incomingMessages] to receive [GameMessage]s from the host.
 * Call [send] to push messages. Call [disconnect] when done.
 */
class GameWebSocketClient(
    private val hostIp: String,
    private val serverPort: Int
) {
    private val httpClient = HttpClient(OkHttp) {
        install(WebSockets)
    }

    private val _incomingMessages = MutableSharedFlow<GameMessage>(extraBufferCapacity = 64)
    /** Incoming [GameMessage] stream from the host. Collect in a coroutine. */
    val incomingMessages: SharedFlow<GameMessage> = _incomingMessages

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Lost)
    /** Live connection state — transitions to [ConnectionState.Connected] once the handshake completes. */
    val connectionState: StateFlow<ConnectionState> = _connectionState

    @Volatile private var session: DefaultClientWebSocketSession? = null
    private var connectJob: Job? = null

    /**
     * Initiates the WebSocket connection on [scope]'s [Dispatchers.IO].
     * Automatically retries up to [MAX_RECONNECT_ATTEMPTS] times on unexpected failure.
     */
    fun connect(scope: CoroutineScope) {
        connectJob = scope.launch(Dispatchers.IO) {
            connectWithRetry()
        }
    }

    private suspend fun connectWithRetry() {
        var attemptsLeft = MAX_RECONNECT_ATTEMPTS
        var delayMs = 1_000L

        while (attemptsLeft >= 0) {
            var cleanClose = false
            try {
                httpClient.webSocket("ws://$hostIp:$serverPort/game") {
                    session = this
                    _connectionState.value = ConnectionState.Connected
                    // Reset retry counters on a successful connection
                    attemptsLeft = MAX_RECONNECT_ATTEMPTS
                    delayMs = 1_000L

                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            runCatching {
                                val msg = NetworkJson.decodeFromString<GameMessage>(frame.readText())
                                _incomingMessages.emit(msg)
                            }
                        }
                    }
                    // Reaching here means the server sent a clean WebSocket close frame
                    cleanClose = true
                }
            } catch (e: CancellationException) {
                // Propagate coroutine cancellation — do not retry
                throw e
            } catch (e: Exception) {
                // Network error — fall through to retry logic below
            } finally {
                session = null
            }

            if (cleanClose) break // Server intentionally closed — do not retry

            attemptsLeft--
            if (attemptsLeft < 0) break

            _connectionState.value = ConnectionState.Reconnecting(attemptsLeft)
            delay(delayMs)
            delayMs = (delayMs * 2).coerceAtMost(16_000L)
        }

        _connectionState.value = ConnectionState.Lost
    }

    /**
     * Sends [message] to the host.
     * Silently no-ops if not currently connected.
     */
    suspend fun send(message: GameMessage) {
        session?.send(NetworkJson.encodeToString<GameMessage>(message))
    }

    /** Cancels the connection loop and closes the underlying OkHttp client. */
    fun disconnect() {
        connectJob?.cancel()
        runCatching { httpClient.close() }
    }
}
