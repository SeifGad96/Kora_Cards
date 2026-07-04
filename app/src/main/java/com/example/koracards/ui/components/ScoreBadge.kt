package com.example.koracards.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.koracards.ui.theme.KoraCardsTheme

/**
 * ScoreBadge is a pill-shaped indicator displaying score with labels.
 */
@Composable
fun ScoreBadge(
    label: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Preview(name = "ScoreBadge - EN")
@Composable
fun ScoreBadgeEnglishPreview() {
    KoraCardsTheme(isArabic = false) {
        ScoreBadge(label = "Score", score = 5)
    }
}

@Preview(name = "ScoreBadge - AR")
@Composable
fun ScoreBadgeArabicPreview() {
    KoraCardsTheme(isArabic = true) {
        ScoreBadge(label = "النقاط", score = 5)
    }
}
