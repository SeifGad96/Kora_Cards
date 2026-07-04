package com.example.koracards.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for the WebSocket host↔guest loopback path.
 *
 * Uses [GameWebSocketServer] and [GameWebSocketClient] directly (no [NetworkManager]) so the
 * tests have no Android dependency and run on the JVM. The server binds to a free local port;
 * the client connects to `127.0.0.1:{port}`.
 *
 * All tests are bounded by a 10-second [withTimeout] to keep CI fast.
 */
class NetworkLoopbackTest {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var server: GameWebSocketServer
    private var client: GameWebSocketClient? = null

    @Before
    fun setUp() {
        server = GameWebSocketServer()
        server.start()
    }

    @After
    fun tearDown() {
        client?.disconnect()
        client = null
        server.stop()
        scope.cancel()
    }

    // -----------------------------------------------------------------------
    // Host → Guest
    // -----------------------------------------------------------------------

    @Test
    fun `host sends NicknameSync - guest receives it`() = runBlocking {
        withTimeout(10_000L) {
            val cli = connect()

            val expected = GameMessage.NicknameSync(hostName = "Seif", guestName = "Ahmed")

            // Subscribe BEFORE sending so the SharedFlow emission is not missed
            val received = async(Dispatchers.IO) { cli.incomingMessages.first() }
            delay(50L) // give the async collector time to register on IO dispatcher

            server.send(expected)
            assertEquals(expected, received.await())
        }
    }

    @Test
    fun `host sends ConfigSynced - guest receives correct config`() = runBlocking {
        withTimeout(10_000L) {
            val cli = connect()

            val config = GameConfig(roundsToWin = 5, maxFaults = 2, turnTimerSeconds = 30)
            val expected = GameMessage.ConfigSynced(config)

            val received = async(Dispatchers.IO) { cli.incomingMessages.first() }
            delay(50L)

            server.send(expected)
            assertEquals(expected, received.await())
        }
    }

    // -----------------------------------------------------------------------
    // Guest → Host
    // -----------------------------------------------------------------------

    @Test
    fun `guest sends TurnChanged - host receives it`() = runBlocking {
        withTimeout(10_000L) {
            val cli = connect()

            val expected = GameMessage.TurnChanged(activePlayerId = "player_guest")

            // Subscribe to server's incoming BEFORE sending from client
            val received = async(Dispatchers.IO) { server.incomingMessages.first() }
            delay(50L)

            cli.send(expected)
            assertEquals(expected, received.await())
        }
    }

    // -----------------------------------------------------------------------
    // Connection state
    // -----------------------------------------------------------------------

    @Test
    fun `server connection state becomes Lost after client disconnects`() = runBlocking {
        withTimeout(10_000L) {
            val cli = connect()

            assertEquals(ConnectionState.Connected, server.connectionState.value)

            cli.disconnect()
            client = null // prevent double-disconnect in tearDown

            // Wait for the server to detect the disconnect
            server.connectionState.first { it == ConnectionState.Lost }
            assertEquals(ConnectionState.Lost, server.connectionState.value)
        }
    }

    // -----------------------------------------------------------------------
    // Helper — connects a client and waits for both sides to be ready
    // -----------------------------------------------------------------------

    /**
     * Creates a [GameWebSocketClient], connects it to the local server, and waits
     * for [ConnectionState.Connected] on the client's StateFlow before returning.
     * This replaces unreliable fixed delays.
     */
    private suspend fun connect(): GameWebSocketClient {
        val cli = GameWebSocketClient("127.0.0.1", server.port)
        client = cli
        cli.connect(scope)
        // Wait for the actual handshake rather than an arbitrary sleep
        withTimeout(5_000L) {
            cli.connectionState.first { it == ConnectionState.Connected }
        }
        // Also wait for the server to register the session
        withTimeout(5_000L) {
            server.connectionState.first { it == ConnectionState.Connected }
        }
        return cli
    }
}
