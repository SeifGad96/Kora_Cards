package com.example.koracards.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.koracards.game.GameViewModel
import com.example.koracards.game.PlayerRole
import com.example.koracards.ui.components.ButtonVariant
import com.example.koracards.ui.components.KoraButton
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.strings.AppStrings

/**
 * Player Name Entry Screen (Screen 5)
 */
@Composable
fun PlayerNameEntryScreen(
    isArabic: Boolean,
    isHost: Boolean,
    lobbyViewModel: LobbyViewModel,
    gameViewModel: GameViewModel,
    onHostProceedToConfig: (String) -> Unit,
    onGuestWait: () -> Unit,
    onBack: () -> Unit
) {
    val s = AppStrings(isArabic)
    var nameInput by remember { mutableStateOf("") }
    var isConfirmed by remember { mutableStateOf(false) }

    LaunchedEffect(isHost) {
        gameViewModel.setRole(if (isHost) PlayerRole.Host else PlayerRole.Guest)
    }

    val roleText = if (isHost) s.playerNameRoleHost else s.playerNameRoleGuest

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
                    text = s.playerNameTitle,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${s.playerNameRoleLabel}: $roleText",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (!isConfirmed) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { if (it.length <= 20) nameInput = it },
                        label = { Text(s.playerNameEnterYours) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (nameInput.trim().isNotEmpty()) {
                                    val name = nameInput.trim()
                                    lobbyViewModel.setMyName(name)
                                    isConfirmed = true
                                    if (isHost) {
                                        onHostProceedToConfig(name)
                                    } else {
                                        onGuestWait()
                                    }
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = s.playerNameHostWaiting,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (!isConfirmed) {
            Spacer(modifier = Modifier.height(32.dp))
            KoraButton(
                onClick = {
                    val name = nameInput.trim().ifEmpty { if (isHost) s.playerNameRoleHost else s.playerNameRoleGuest }
                    lobbyViewModel.setMyName(name)
                    isConfirmed = true
                    if (isHost) {
                        onHostProceedToConfig(name)
                    } else {
                        onGuestWait()
                    }
                },
                enabled = nameInput.trim().isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s.playerNameConfirm)
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
}
