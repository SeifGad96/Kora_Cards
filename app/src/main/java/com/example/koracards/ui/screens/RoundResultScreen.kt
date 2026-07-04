package com.example.koracards.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.koracards.game.GameViewModel
import com.example.koracards.game.PlayerRole
import com.example.koracards.ui.components.KoraButton
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.components.ScoreBadge
import com.example.koracards.ui.strings.AppStrings

/**
 * Round Result Screen (Screen 12)
 */
@Composable
fun RoundResultScreen(
    isArabic: Boolean,
    winnerName: String,
    hostScore: Int,
    guestScore: Int,
    gameViewModel: GameViewModel
) {
    val s = AppStrings(isArabic)
    val state by gameViewModel.state.collectAsStateWithLifecycle()
    val isHost = state.myRole == PlayerRole.Host

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
                    text = s.roundResultTitle,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${s.roundResultWinner}: $winnerName",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScoreBadge(label = state.hostNickname.ifEmpty { s.host }, score = hostScore)
                    ScoreBadge(label = state.guestNickname.ifEmpty { s.guest }, score = guestScore)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isHost) {
            KoraButton(
                onClick = { gameViewModel.startNextRound() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s.roundResultNextBtn)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = s.guestWaiting,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
