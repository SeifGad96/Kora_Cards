package com.example.koracards.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.koracards.ui.theme.KoraCardsTheme

enum class ButtonVariant {
    Primary, Secondary, Danger, Outlined
}

/**
 * Standard button component for KoraCards.
 *
 * Supports Primary, Secondary, Danger, and Outlined variants.
 * Enforces a minimum touch target height of 48dp per PRD §8.4 accessibility requirements.
 */
@Composable
fun KoraButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    // PRD §8.4: Minimum touch target size 48dp
    val targetModifier = modifier.heightIn(min = 48.dp)

    when (variant) {
        ButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = targetModifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            content = content
        )
        else -> {
            val colors = when (variant) {
                ButtonVariant.Primary -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                ButtonVariant.Secondary -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
                ButtonVariant.Danger -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
                ButtonVariant.Outlined -> ButtonDefaults.buttonColors()
            }

            Button(
                onClick = onClick,
                modifier = targetModifier,
                enabled = enabled,
                colors = colors,
                shape = MaterialTheme.shapes.medium,
                content = content
            )
        }
    }
}

@Preview(name = "KoraButton - Primary EN", group = "Button")
@Composable
fun KoraButtonPrimaryEnPreview() {
    KoraCardsTheme(isArabic = false) {
        KoraButton(onClick = {}) {
            Text("Primary Button")
        }
    }
}

@Preview(name = "KoraButton - Secondary AR", group = "Button")
@Composable
fun KoraButtonSecondaryArPreview() {
    KoraCardsTheme(isArabic = true) {
        KoraButton(onClick = {}, variant = ButtonVariant.Secondary) {
            Text("زر ثانوي")
        }
    }
}

@Preview(name = "KoraButton - Danger EN", group = "Button")
@Composable
fun KoraButtonDangerEnPreview() {
    KoraCardsTheme(isArabic = false) {
        KoraButton(onClick = {}, variant = ButtonVariant.Danger) {
            Text("Wrong ✗")
        }
    }
}

@Preview(name = "KoraButton - Outlined EN", group = "Button")
@Composable
fun KoraButtonOutlinedEnPreview() {
    KoraCardsTheme(isArabic = false) {
        KoraButton(onClick = {}, variant = ButtonVariant.Outlined) {
            Text("Cancel")
        }
    }
}

