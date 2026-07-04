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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.koracards.ui.components.ButtonVariant
import com.example.koracards.ui.components.KoraButton
import com.example.koracards.ui.components.KoraCard
import com.example.koracards.ui.strings.AppStrings

/**
 * Join Game Screen (Screen 4)
 */
@Composable
fun JoinGameScreen(
    isArabic: Boolean,
    lobbyViewModel: LobbyViewModel,
    onConnected: () -> Unit,
    onBack: () -> Unit
) {
    val s = AppStrings(isArabic)
    var codeInput by remember { mutableStateOf("") }
    val uiState by lobbyViewModel.uiState.collectAsStateWithLifecycle()

    // Reset any leftover search state from a previous attempt every time this screen is entered.
    // Without this, isSearching=true would survive back-stack navigation and show an
    // un-cancellable spinner.
    LaunchedEffect(Unit) {
        lobbyViewModel.resetGuestSearch()
    }

    LaunchedEffect(uiState.isConnected) {
        if (uiState.isConnected) {
            onConnected()
        }
    }

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
                    text = s.joinTitle,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = s.joinHint,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { new ->
                        if (new.length <= 4) {
                            codeInput = new.uppercase()
                            lobbyViewModel.clearError()
                        }
                    },
                    label = { Text(s.joinRoomCodeLabel) },
                    singleLine = true,
                    isError = uiState.errorMessage != null,
                    supportingText = uiState.errorMessage?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (codeInput.length == 4) {
                                lobbyViewModel.startAsGuest(codeInput)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.isSearching) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = s.joinConnecting, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            KoraButton(
                onClick = { lobbyViewModel.startAsGuest(codeInput) },
                enabled = codeInput.length == 4,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s.joinConfirmBtn)
            }
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
