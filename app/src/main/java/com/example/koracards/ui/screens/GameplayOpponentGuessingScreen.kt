package com.example.koracards.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.koracards.game.GameViewModel
import com.example.koracards.ui.components.*
import com.example.koracards.ui.strings.AppStrings

/**
 * Gameplay Screen - Opponent Guessing (Screen 9)
 */
@Composable
fun GameplayOpponentGuessingScreen(
    isArabic: Boolean,
    gameViewModel: GameViewModel
) {
    val s = AppStrings(isArabic)
    val state by gameViewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Score Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreBadge(label = state.myNickname.ifEmpty { s.host }, score = state.myScore)
            Text("vs", style = MaterialTheme.typography.titleMedium)
            ScoreBadge(label = state.opponentNickname.ifEmpty { s.guest }, score = state.opponentScore)
        }

        // 2. Turn Indicator
        TurnIndicator(
            isMyTurn = state.isMyTurn,
            myLabel = s.yourTurn,
            opponentLabel = s.opponentTurn,
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Turn Timer
        if (state.config.turnTimerSeconds != null && state.timerSecondsLeft > 0) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { state.timerSecondsLeft.toFloat() / state.config.turnTimerSeconds!!.toFloat() },
                    color = if (state.timerSecondsLeft <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${s.timerLabel}: ${state.timerSecondsLeft}s",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 4. Instructions Card
        KoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = s.opponentGuessTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = s.opponentGuessHint,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(32.dp))

                // MY fault count - I am guessing, so show my faults
                Text(
                    text = "${s.faultsLabel}:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FaultCounter(
                    faultCount = state.myFaults,
                    maxFaults = state.config.maxFaults
                )
            }
        }

        // No Action Buttons at the bottom as I'm the guesser;
        // cardholder opponent handles Correct / Wrong on their device.
        Spacer(modifier = Modifier.height(16.dp))
    }
}
