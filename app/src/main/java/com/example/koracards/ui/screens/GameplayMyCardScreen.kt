package com.example.koracards.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
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
 * Gameplay Screen - My Card (Screen 8)
 */
@Composable
fun GameplayMyCardScreen(
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

        // 4. My Card details
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
                if (state.myCard != null) {
                    val player = state.myCard!!
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${s.teamLabel}: ${player.team(isArabic)}", style = MaterialTheme.typography.bodyLarge)
                        Text("${s.positionLabel}: ${player.position(isArabic)}", style = MaterialTheme.typography.bodyLarge)
                        Text("${s.nationalityLabel}: ${player.nationality(isArabic)}", style = MaterialTheme.typography.bodyLarge)
                        Text("${s.leagueLabel}: ${player.league(isArabic)}", style = MaterialTheme.typography.bodyLarge)
                        Text("${s.footLabel}: ${player.preferredFoot(isArabic)}", style = MaterialTheme.typography.bodyLarge)
                        Text("${s.ageLabel}: ${player.age}", style = MaterialTheme.typography.bodyLarge)
                        Text("${s.shirtLabel}: ${player.shirtNumber}", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        }

        // 5. Fault Counter (shows opponent's faults since I am judging my card)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${state.opponentNickname.ifEmpty { s.guest }} ${s.faultsLabel}:",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            FaultCounter(
                faultCount = state.opponentFaults,
                maxFaults = state.config.maxFaults
            )
        }

        // 6. Action Buttons (shown only if I am the card holder - i.e., guest is guessing my card)
        if (state.isCardHolder) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KoraButton(
                    onClick = { gameViewModel.handleCorrectGuess() },
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(s.correctBtn)
                }
                KoraButton(
                    onClick = { gameViewModel.handleWrongGuess() },
                    variant = ButtonVariant.Danger,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(s.wrongBtn)
                }
            }
        } else {
            // When it is my turn to guess, we show a waiting text if needed,
            // though actually during my turn to guess, the AppNavHost will navigate to GameplayOpponentGuessingScreen.
            Text(
                text = s.waitingForOpponent,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
