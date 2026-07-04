package com.example.koracards.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.koracards.game.GameViewModel
import com.example.koracards.game.PlayerRole
import com.example.koracards.ui.components.ButtonVariant
import com.example.koracards.ui.components.KoraButton
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.components.ScoreBadge
import com.example.koracards.ui.strings.AppStrings

/**
 * Match Winner Screen (Screen 14)
 */
@Composable
fun MatchWinnerScreen(
    isArabic: Boolean,
    winnerName: String,
    hostScore: Int,
    guestScore: Int,
    gameViewModel: GameViewModel,
    onBackToHome: () -> Unit
) {
    val s = AppStrings(isArabic)
    val state by gameViewModel.state.collectAsStateWithLifecycle()
    val isHost = state.myRole == PlayerRole.Host

    val infiniteTransition = rememberInfiniteTransition(label = "trophy_bounce")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_scale"
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
                Text(
                    text = "🏆",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.scale(trophyScale)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s.matchWinnerChampion,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = winnerName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s.matchWinnerMessage,
                    style = MaterialTheme.typography.titleMedium
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
                onClick = {
                    gameViewModel.resetMatch()
                    gameViewModel.startRound()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s.matchPlayAgainBtn)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        KoraButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Secondary
        ) {
            Text(s.matchWinnerBackBtn)
        }
    }
}
