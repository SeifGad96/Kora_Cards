package com.example.koracards.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException

/** Internal deserialization model for UDP discovery packets sent by [UdpBroadcaster]. */
@Serializable
internal data class UdpPayload(val roomCode: String, val ip: String, val port: Int)

/**
 * Guest-side UDP listener.
 *
 * Binds to [UDP_PORT] and waits for broadcast packets from [UdpBroadcaster].
 * Compares the incoming [UdpPayload.roomCode] to [expectedRoomCode]; on a match,
 * invokes [onDiscovered] with the host's IP and WebSocket port, then stops listening.
 *
 * The socket uses a 2-second receive timeout so the coroutine can check [isActive]
 * regularly and respond to cancellation without blocking forever.
 */
class UdpListener {
    private var job: Job? = null

    /**
     * Starts listening on [scope]'s [Dispatchers.IO].
     *
     * @param scope            Coroutine scope — cancelled via [stop] or the scope's own lifecycle.
     * @param expectedRoomCode The 4-char code the user typed on the Join Game screen.
     * @param onDiscovered     Called once on the IO dispatcher when a matching host is found.
     */
    fun start(
        scope: CoroutineScope,
        expectedRoomCode: String,
        onDiscovered: (ip: String, port: Int) -> Unit
    ) {
        stop()
        job = scope.launch(Dispatchers.IO) {
            val socket = DatagramSocket(UDP_PORT)
            socket.soTimeout = 2_000 // 2s timeout → loop checks isActive
            val buffer = ByteArray(512)
            try {
                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    try {
                        socket.receive(packet)
                        val json = String(packet.data, 0, packet.length, Charsets.UTF_8)
                        val payload = NetworkJson.decodeFromString<UdpPayload>(json)
                        if (payload.roomCode == expectedRoomCode) {
                            onDiscovered(payload.ip, payload.port)
                            break
                        }
                    } catch (e: SocketTimeoutException) {
                        // Expected — loop and check isActive again
                    } catch (e: Exception) {
                        // Malformed packet — ignore and continue
                    }
                }
            } finally {
                runCatching { socket.close() }
            }
        }
    }

    /** Cancels the listener coroutine and releases the socket. */
    fun stop() {
        job?.cancel()
        job = null
    }
}
