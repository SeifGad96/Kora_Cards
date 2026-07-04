package com.example.koracards.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koracards.network.ConnectionState
import com.example.koracards.network.GameConfig
import com.example.koracards.network.GameMessage
import com.example.koracards.network.NetworkManager
import com.example.koracards.network.RoomCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LobbyUiState(
    val roomCode: String = "",
    val myName: String = "",
    val opponentName: String = "",
    val isConnected: Boolean = false,
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val bothNamesReady: Boolean = false,
    val pendingConfig: GameConfig = GameConfig()
)

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val networkManager: NetworkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            networkManager.connectionState.collect { connState ->
                val connected = connState is ConnectionState.Connected
                _uiState.update {
                    it.copy(
                        isConnected = connected,
                        isSearching = if (connected) false else it.isSearching
                    )
                }
            }
        }
        viewModelScope.launch {
            networkManager.incomingMessages.collect { message ->
                when (message) {
                    is GameMessage.NicknameSync -> {
                        _uiState.update {
                            it.copy(opponentName = message.guestName, bothNamesReady = true)
                        }
                        networkManager.stopBroadcasting()
                    }
                    else -> Unit
                }
            }
        }
    }

    /**
     * Call this at the START of HostWaitingScreen (before startAsHost) to fully reset any
     * previous session state. Disconnects any leftover WebSocket/UDP from a prior game,
     * which resets NetworkManager._connectionState to Lost, preventing the stale-
     * isConnected=true bug that causes the lobby to immediately navigate away.
     */
    fun resetForNewSession() {
        networkManager.disconnect()
        _uiState.value = LobbyUiState()
    }

    /**
     * Call this at the START of JoinGameScreen entry (LaunchedEffect(Unit)) to clear any
     * leftover isSearching=true state that survives back-stack navigation.
     */
    fun resetGuestSearch() {
        networkManager.disconnect()
        _uiState.update {
            it.copy(isSearching = false, isConnected = false, errorMessage = null)
        }
    }

    /** Host only — call this from HostWaitingScreen's LaunchedEffect(Unit) AFTER resetForNewSession(). */
    fun startAsHost() {
        val code = RoomCodeGenerator.generate()
        _uiState.update { it.copy(roomCode = code, isSearching = true) }
        networkManager.startAsHost(code)
    }

    /** Guest only — call when user taps Connect in JoinGameScreen. */
    fun startAsGuest(roomCode: String) {
        if (roomCode.length != 4) {
            _uiState.update { it.copy(errorMessage = "Invalid code — must be 4 characters") }
            return
        }
        _uiState.update { it.copy(isSearching = true, errorMessage = null) }
        networkManager.startAsGuest(roomCode)

        // Auto-fail after 15 seconds if no connection is established.
        // Prevents the infinite-loading spinner when the host code is wrong or the host
        // is unreachable (e.g. not on the same hotspot).
        viewModelScope.launch {
            delay(15_000L)
            if (_uiState.value.isSearching && !_uiState.value.isConnected) {
                networkManager.disconnect()
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        errorMessage = "Could not find host. Check the code and try again."
                    )
                }
            }
        }
    }

    fun setMyName(name: String) {
        _uiState.update { it.copy(myName = name) }
    }

    /** Host only — sends NicknameSync to guest. */
    fun sendNicknameSync(hostName: String, guestName: String) {
        viewModelScope.launch {
            networkManager.send(GameMessage.NicknameSync(hostName = hostName, guestName = guestName))
            _uiState.update { it.copy(bothNamesReady = true) }
        }
    }

    /** Host only — store updated config locally without sending. */
    fun updateConfig(config: GameConfig) {
        _uiState.update { it.copy(pendingConfig = config) }
    }

    /** Host only — sends ConfigSynced to guest and returns the config. */
    fun sendConfigAndGetIt(): GameConfig {
        val config = _uiState.value.pendingConfig
        viewModelScope.launch {
            networkManager.send(GameMessage.ConfigSynced(config))
        }
        return config
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
