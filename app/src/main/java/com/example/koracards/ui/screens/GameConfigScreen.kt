package com.example.koracards.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.koracards.game.GameViewModel
import com.example.koracards.network.GameConfig
import com.example.koracards.ui.components.ButtonVariant
import com.example.koracards.ui.components.KoraButton
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.strings.AppStrings

/**
 * Game Config Screen (Screen 6)
 */
@Composable
fun GameConfigScreen(
    isArabic: Boolean,
    hostName: String,
    lobbyViewModel: LobbyViewModel,
    gameViewModel: GameViewModel,
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    val s = AppStrings(isArabic)
    val scrollState = rememberScrollState()

    var roundsToWin by remember { mutableIntStateOf(3) }   // Slider: 1f..10f, steps=8
    var maxFaults   by remember { mutableIntStateOf(3) }   // Slider: 1f..5f,  steps=3
    var timerEnabled by remember { mutableStateOf(false) } // Switch
    var timerSeconds by remember { mutableIntStateOf(30) } // Slider: 15f..60f, steps=8
    var mcqEnabled   by remember { mutableStateOf(true) }  // Switch
    var mcqThreshold by remember { mutableIntStateOf(5) }  // Slider: 3f..15f, steps=11

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        KoraCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = s.gameConfigTitle,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${s.gameConfigHostLabel}: $hostName",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = s.gameConfigHint,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Configuration Card
        KoraCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rounds to Win
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${s.configRoundsToWin}: $roundsToWin",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = roundsToWin.toFloat(),
                        onValueChange = { roundsToWin = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }

                // Max Faults
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${s.configMaxFaults}: $maxFaults",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = maxFaults.toFloat(),
                        onValueChange = { maxFaults = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }

                // Turn Timer Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = s.configTimerLabel,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = timerEnabled,
                        onCheckedChange = { timerEnabled = it }
                    )
                }

                // Turn Timer Seconds Slider
                if (timerEnabled) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${s.configTimerLabel}: $timerSeconds ${s.configTimerSeconds}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = timerSeconds.toFloat(),
                            onValueChange = { timerSeconds = it.toInt() },
                            valueRange = 15f..60f,
                            steps = 8
                        )
                    }
                }

                // MCQ Enable Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = s.configMcqToggle,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = mcqEnabled,
                        onCheckedChange = { mcqEnabled = it }
                    )
                }

                // MCQ Threshold Slider
                if (mcqEnabled) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${s.configMcqThreshold}: $mcqThreshold",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = mcqThreshold.toFloat(),
                            onValueChange = { mcqThreshold = it.toInt() },
                            valueRange = 3f..15f,
                            steps = 11
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        KoraButton(
            onClick = {
                val config = GameConfig(
                    roundsToWin = roundsToWin,
                    maxFaults = maxFaults,
                    turnTimerSeconds = if (timerEnabled) timerSeconds else null,
                    mcqEnabled = mcqEnabled,
                    mcqQuestionThreshold = mcqThreshold
                )
                gameViewModel.applyConfig(config)
                lobbyViewModel.updateConfig(config)
                lobbyViewModel.sendConfigAndGetIt()
                lobbyViewModel.sendNicknameSync(
                    hostName = hostName,
                    guestName = "Guest"
                )
                gameViewModel.applyNicknameSync(
                    hostName = hostName,
                    guestName = "Guest"
                )
                gameViewModel.startRound()
                onStartGame()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(s.configStartGame)
        }

        Spacer(modifier = Modifier.height(16.dp))

        KoraButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Secondary
        ) {
            Text(s.back)
        }
    }
}
