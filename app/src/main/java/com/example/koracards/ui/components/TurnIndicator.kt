package com.example.koracards.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.koracards.ui.theme.KoraCardsTheme

/**
 * Component to indicate whose turn it is currently (My Turn vs Opponent's Turn).
 */
@Composable
fun TurnIndicator(
    isMyTurn: Boolean,
    myLabel: String = "Your Turn",
    opponentLabel: String = "Opponent's Turn",
    modifier: Modifier = Modifier
) {
    val text = if (isMyTurn) myLabel else opponentLabel
    val color = if (isMyTurn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
}

@Preview(name = "TurnIndicator - My Turn EN")
@Composable
fun TurnIndicatorMyTurnPreview() {
    KoraCardsTheme(isArabic = false) {
        TurnIndicator(isMyTurn = true)
    }
}

@Preview(name = "TurnIndicator - Opponent Turn AR")
@Composable
fun TurnIndicatorOpponentTurnPreview() {
    KoraCardsTheme(isArabic = true) {
        TurnIndicator(isMyTurn = false, myLabel = "دورك", opponentLabel = "دور الخصم")
    }
}
