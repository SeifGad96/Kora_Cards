package com.example.koracards.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.koracards.data.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** DataStore instance scoped to the Application context. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kora_prefs")

/**
 * Persists user preferences across sessions using Jetpack DataStore.
 *
 * Currently stores:
 *  - **language**: AR/EN preference (PRD §4.11)
 */
@Singleton
class PreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
    }

    /**
     * Emits the current [Language] preference as a [Flow].
     * Defaults to [Language.Arabic] on first launch per PRD §4.11.
     */
    val language: Flow<Language> = context.dataStore.data.map { prefs ->
        Language.fromCode(prefs[Keys.LANGUAGE] ?: Language.Arabic.code)
    }

    /** Persists the selected [language] to DataStore. */
    suspend fun setLanguage(language: Language) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }
}
