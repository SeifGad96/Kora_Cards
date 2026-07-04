package com.example.koracards.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.koracards.ui.components.ButtonVariant
import com.example.koracards.ui.components.KoraButton
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.strings.AppStrings

/** Host Waiting / Game Lobby Screen (Screen 3) */
@Composable
fun HostWaitingScreen(
    isArabic: Boolean,
    lobbyViewModel: LobbyViewModel,
    onGuestConnected: () -> Unit,
    onBackToHome: () -> Unit
) {
    val s = AppStrings(isArabic)
    val uiState by lobbyViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        // Reset any previous session first — this disconnects lingering WebSocket/UDP
        // connections and resets connectionState to Lost, preventing the stale-
        // isConnected=true bug that would otherwise immediately navigate away.
        lobbyViewModel.resetForNewSession()
        lobbyViewModel.startAsHost()
    }

    LaunchedEffect(uiState.isConnected) {
        if (uiState.isConnected) {
            onGuestConnected()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lobby_pulse")
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lobby_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KoraCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = s.lobbyTitle, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = uiState.roomCode.ifEmpty { "----" },
                    style = MaterialTheme.typography.displaySmall,
                    letterSpacing = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.alpha(pulsingAlpha)
                ) {
                    Text(text = s.lobbyWaiting, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        KoraButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Secondary
        ) {
            Text(s.lobbyLeaveBtn)
        }
    }
}
