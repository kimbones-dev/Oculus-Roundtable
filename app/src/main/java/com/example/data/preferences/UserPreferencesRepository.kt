package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "oculus_preferences")

data class UserPreferences(
    val isFirstLaunch: Boolean,
    val diagnosticDomain: String,
    val preferredOutputFormat: String,
    val workflowRigor: String,
    val baselineTemperature: Float
)

class UserPreferencesRepository(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val KEY_DIAGNOSTIC_DOMAIN = stringPreferencesKey("diagnostic_domain")
        val KEY_OUTPUT_FORMAT = stringPreferencesKey("output_format")
        val KEY_WORKFLOW_RIGOR = stringPreferencesKey("workflow_rigor")
        val KEY_BASELINE_TEMPERATURE = floatPreferencesKey("baseline_temperature")
    }

    val preferencesFlow: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            isFirstLaunch = prefs[KEY_IS_FIRST_LAUNCH] ?: true,
            diagnosticDomain = prefs[KEY_DIAGNOSTIC_DOMAIN] ?: "Autonomous Systems & AI Architecture",
            preferredOutputFormat = prefs[KEY_OUTPUT_FORMAT] ?: "Synthesis Matrix & Actionable Spec",
            workflowRigor = prefs[KEY_WORKFLOW_RIGOR] ?: "Adversarial Stress-Testing",
            baselineTemperature = prefs[KEY_BASELINE_TEMPERATURE] ?: 0.7f
        )
    }

    suspend fun completeOnboarding(
        domain: String,
        outputFormat: String,
        rigor: String,
        temperature: Float
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_FIRST_LAUNCH] = false
            prefs[KEY_DIAGNOSTIC_DOMAIN] = domain
            prefs[KEY_OUTPUT_FORMAT] = outputFormat
            prefs[KEY_WORKFLOW_RIGOR] = rigor
            prefs[KEY_BASELINE_TEMPERATURE] = temperature
        }
    }

    suspend fun resetOnboarding() {
        dataStore.edit { prefs ->
            prefs[KEY_IS_FIRST_LAUNCH] = true
        }
    }
}
