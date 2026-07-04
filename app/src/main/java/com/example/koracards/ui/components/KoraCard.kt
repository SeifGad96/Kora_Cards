package com.example.koracards.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.koracards.ui.theme.KoraCardsTheme

/**
 * Custom Card shell representing player cards or dialog cards with consistent styling.
 */
@Composable
fun KoraCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Preview(name = "KoraCard - EN")
@Composable
fun KoraCardEnglishPreview() {
    KoraCardsTheme(isArabic = false) {
        KoraCard(modifier = Modifier.fillMaxWidth()) {
            Text("This is a player card inside the KoraCard component shell.")
        }
    }
}

@Preview(name = "KoraCard - AR")
@Composable
fun KoraCardArabicPreview() {
    KoraCardsTheme(isArabic = true) {
        KoraCard(modifier = Modifier.fillMaxWidth()) {
            Text("هذه بطاقة لاعب بداخل مكون KoraCard.")
        }
    }
}
