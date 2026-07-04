package com.example.koracards.e2e.infra

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class TestNetworkCoordinator(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _hostIncoming = MutableSharedFlow<String>(extraBufferCapacity = 64)
    private val _guestIncoming = MutableSharedFlow<String>(extraBufferCapacity = 64)

    val hostIncoming: SharedFlow<String> = _hostIncoming.asSharedFlow()
    val guestIncoming: SharedFlow<String> = _guestIncoming.asSharedFlow()

    private var isConnected = true
    private var latencyMs = 0L

    fun sendToHost(message: String) {
        if (!isConnected) return
        scope.launch {
            if (latencyMs > 0) delay(latencyMs)
            _hostIncoming.emit(message)
        }
    }

    fun sendToGuest(message: String) {
        if (!isConnected) return
        scope.launch {
            if (latencyMs > 0) delay(latencyMs)
            _guestIncoming.emit(message)
        }
    }

    fun simulateDisconnection() {
        isConnected = false
    }

    fun simulateReconnection() {
        isConnected = true
    }

    fun setLatency(ms: Long) {
        latencyMs = ms
    }
}
