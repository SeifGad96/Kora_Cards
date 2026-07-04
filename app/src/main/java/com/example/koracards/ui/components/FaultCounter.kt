package com.example.koracards.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.koracards.ui.theme.KoraCardsTheme

/**
 * FaultCounter displays a series of dots indicating incorrect guesses or faults.
 */
@Composable
fun FaultCounter(
    faultCount: Int,
    maxFaults: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until maxFaults) {
            val isFaulted = i < faultCount
            val color = if (isFaulted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Preview(name = "FaultCounter - 1 Fault EN")
@Composable
fun FaultCounterPreview() {
    KoraCardsTheme(isArabic = false) {
        FaultCounter(faultCount = 1, maxFaults = 3)
    }
}

@Preview(name = "FaultCounter - 2 Faults AR")
@Composable
fun FaultCounterArabicPreview() {
    KoraCardsTheme(isArabic = true) {
        FaultCounter(faultCount = 2, maxFaults = 3)
    }
}
