package com.example.koracards.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.components.TurnIndicator
import com.example.koracards.ui.strings.AppStrings
import kotlinx.coroutines.delay

/**
 * Round Start Screen (Screen 7)
 */
@Composable
fun RoundStartScreen(
    isArabic: Boolean,
    roundNumber: Int,
    isMyTurn: Boolean,
    onProceedToGameplay: () -> Unit
) {
    val s = AppStrings(isArabic)
    var targetScale by remember { mutableStateOf(0.8f) }

    LaunchedEffect(Unit) {
        targetScale = 1f
        delay(3000L)
        onProceedToGameplay()
    }

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "round_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "${s.roundStartLabel} $roundNumber",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = s.roundStartGetReady,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                TurnIndicator(
                    isMyTurn = isMyTurn,
                    myLabel = s.roundStartMyTurn,
                    opponentLabel = s.roundStartOpponent
                )
            }
        }
    }
}
