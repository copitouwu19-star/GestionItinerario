package com.gestion.itinerario.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val PIN_KEY = stringPreferencesKey("user_pin")
    private val PIN_ENABLED_KEY = stringPreferencesKey("pin_enabled")
    private val PALETTE_KEY = stringPreferencesKey("color_palette")

    val pin: Flow<String?> = context.dataStore.data.map { it[PIN_KEY] }
    val isPinEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[PIN_ENABLED_KEY] == "true"
    }

    /** Id de la paleta de colores elegida por el usuario (ver AppPalettes). */
    val colorPalette: Flow<String> = context.dataStore.data.map { it[PALETTE_KEY] ?: "indigo" }

    suspend fun setColorPalette(id: String) {
        context.dataStore.edit { prefs -> prefs[PALETTE_KEY] = id }
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[PIN_KEY] = pin
            prefs[PIN_ENABLED_KEY] = "true"
        }
    }

    suspend fun disablePin() {
        context.dataStore.edit { prefs ->
            prefs.remove(PIN_KEY)
            prefs[PIN_ENABLED_KEY] = "false"
        }
    }
}
