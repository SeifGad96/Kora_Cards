package com.example.koracards.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.koracards.ui.components.ButtonVariant
import com.example.koracards.ui.components.KoraButton
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.strings.AppStrings

/** Home Screen (Screen 2) */
@Composable
fun HomeScreen(
    isArabic: Boolean,
    onHostGame: () -> Unit,
    onJoinGame: () -> Unit,
    onSettings: () -> Unit
) {
    val s = AppStrings(isArabic)
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = s.homeTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))

        KoraButton(onClick = onHostGame, modifier = Modifier.fillMaxWidth()) {
            Text(s.homeHostBtn)
        }
        Spacer(Modifier.height(16.dp))
        KoraButton(onClick = onJoinGame, modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Secondary) {
            Text(s.homeJoinBtn)
        }
        Spacer(Modifier.height(16.dp))
        KoraButton(onClick = onSettings, modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Secondary) {
            Text(s.homeSettingsBtn)
        }

        Spacer(Modifier.height(24.dp))

        // Hotspot setup instructions so both players know how to configure the network
        KoraCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = s.hotspotHintHost,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = s.hotspotHintGuest,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
