package com.example.koracards.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.koracards.game.GameViewModel
import com.example.koracards.network.ConnectionState
import com.example.koracards.ui.components.ButtonVariant
import com.example.koracards.ui.components.KoraButton
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.strings.AppStrings
import kotlinx.coroutines.delay

/**
 * Connection Lost Screen (Screen 16)
 */
@Composable
fun ConnectionLostScreen(
    isArabic: Boolean,
    gameViewModel: GameViewModel,
    onGoHome: () -> Unit
) {
    val s = AppStrings(isArabic)
    val state by gameViewModel.state.collectAsStateWithLifecycle()
    var secondsLeft by remember { mutableIntStateOf(60) }

    // 60-second countdown -> auto-navigate home
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
        onGoHome()
    }

    // Auto-dismiss if reconnected
    LaunchedEffect(state.connectionState) {
        if (state.connectionState is ConnectionState.Connected) {
            onGoHome()
        }
    }

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
                Text(
                    text = s.connectionLostTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = s.connectionLostDesc,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                LinearProgressIndicator(
                    progress = { secondsLeft / 60f },
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${s.connectionLostCountdown} ${secondsLeft}s",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (state.connectionState is ConnectionState.Reconnecting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "جاري إعادة الاتصال..." else "Reconnecting...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        KoraButton(
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Danger
        ) {
            Text(s.connectionLostGoHome)
        }
    }
}
