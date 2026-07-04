package com.example.koracards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.koracards.data.model.Language
import com.example.koracards.data.repository.PreferencesRepository
import com.example.koracards.ui.navigation.AppNavHost
import com.example.koracards.ui.theme.KoraCardsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val languageState by preferencesRepository.language.collectAsStateWithLifecycle(initialValue = Language.Arabic)
            val isArabic = languageState.isArabic

            KoraCardsTheme(isArabic = isArabic) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        isArabic = isArabic,
                        onToggleLanguage = {
                            val newLanguage = if (isArabic) Language.English else Language.Arabic
                            lifecycleScope.launch {
                                preferencesRepository.setLanguage(newLanguage)
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}