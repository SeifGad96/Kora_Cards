package com.example.koracards.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.koracards.game.GameViewModel
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.strings.AppStrings

/**
 * Card Reveal Screen (Screen 11)
 */
@Composable
fun CardRevealScreen(
    isArabic: Boolean,
    gameViewModel: GameViewModel
) {
    val s = AppStrings(isArabic)
    val state by gameViewModel.state.collectAsStateWithLifecycle()

    val rotation by animateFloatAsState(
        targetValue = 180f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "card_flip"
    )
    val isFlipped = rotation > 90f
    val opponentCard = state.opponentCard

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = s.revealingIn,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(24.dp))

        KoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isFlipped && opponentCard != null) {
                    // Un-mirror the text by counter-rotating the content
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = s.opponentCard,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = opponentCard.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${s.teamLabel}: ${opponentCard.team(isArabic)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${s.positionLabel}: ${opponentCard.position(isArabic)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${s.nationalityLabel}: ${opponentCard.nationality(isArabic)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${s.leagueLabel}: ${opponentCard.league(isArabic)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚽",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }
            }
        }
    }
}
