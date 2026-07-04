package com.example.koracards.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.koracards.ui.components.ButtonVariant
import com.example.koracards.ui.components.KoraButton
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.strings.AppStrings

/** Settings Screen (Screen 15) */
@Composable
fun SettingsScreen(
    isArabic: Boolean,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit
) {
    val s = AppStrings(isArabic)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        KoraCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = s.settingsTitle, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text(text = s.settingsCurrentLang, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                KoraButton(onClick = onToggleLanguage, modifier = Modifier.fillMaxWidth()) {
                    Text(s.settingsToggleBtn)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        KoraCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(s.settingsAboutTitle, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "KoraCards",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(s.settingsTagline, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = s.settingsVersion,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        KoraButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Secondary
        ) {
            Text(s.back)
        }
    }
}
