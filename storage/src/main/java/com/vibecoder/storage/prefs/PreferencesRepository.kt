package com.vibecoder.storage.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.rx.preferenceDataStore
import androidx.datastore.rx.RxDataStore
import com.google.common.util.concurrent.ListenableFuture
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

/**
 * Preferences repository using DataStore
 */
class PreferencesRepository @javax.inject.Inject constructor(
    private val context: Context
) {
    private val dataStore: RxDataStore<Preferences> = preferenceDataStore(
        name = "vibecoder_prefs",
        corruptionHandler = { it.delete() }
    )

    // Keys
    private val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
    private val SELECTED_PROVIDER = stringPreferencesKey("selected_provider")
    private val SELECTED_MODEL = stringPreferencesKey("selected_model")
    private val API_KEY_ENCRYPTED = stringPreferencesKey("api_key_encrypted")
    private val BUILD_TYPE = stringPreferencesKey("build_type")
    private val THEME_DARK = booleanPreferencesKey("theme_dark")
    private val LAST_PROJECT_ID = stringPreferencesKey("last_project_id")

    // Setup
    suspend fun isSetupComplete(): Boolean {
        return dataStore.data
            .map { it[SETUP_COMPLETE] ?: false }
            .firstOrNull()?.await() ?: false
    }

    suspend fun setSetupComplete(complete: Boolean) {
        dataStore.updateDataAsync { it[SETUP_COMPLETE] = complete }.await()
    }

    // Provider
    suspend fun getSelectedProvider(): String? {
        return dataStore.data.map { it[SELECTED_PROVIDER] }.firstOrNull()?.await()
    }

    suspend fun setSelectedProvider(provider: String) {
        dataStore.updateDataAsync { it[SELECTED_PROVIDER] = provider }.await()
    }

    // Model
    suspend fun getSelectedModel(): String? {
        return dataStore.data.map { it[SELECTED_MODEL] }.firstOrNull()?.await()
    }

    suspend fun setSelectedModel(model: String) {
        dataStore.updateDataAsync { it[SELECTED_MODEL] = model }.await()
    }

    // API Key (encrypted)
    suspend fun getApiKey(): String? {
        val encrypted = dataStore.data.map { it[API_KEY_ENCRYPTED] }.firstOrNull()?.await()
        return encrypted?.let { decrypt(it) }
    }

    suspend fun setApiKey(apiKey: String) {
        val encrypted = encrypt(apiKey)
        dataStore.updateDataAsync { it[API_KEY_ENCRYPTED] = encrypted }.await()
    }

    // Build type
    suspend fun getBuildType(): String {
        return dataStore.data.map { it[BUILD_TYPE] ?: "online" }.firstOrNull()?.await() ?: "online"
    }

    suspend fun setBuildType(buildType: String) {
        dataStore.updateDataAsync { it[BUILD_TYPE] = buildType }.await()
    }

    // Theme
    suspend fun isDarkTheme(): Boolean {
        return dataStore.data.map { it[THEME_DARK] ?: true }.firstOrNull()?.await() ?: true
    }

    suspend fun setDarkTheme(dark: Boolean) {
        dataStore.updateDataAsync { it[THEME_DARK] = dark }.await()
    }

    // Last project
    suspend fun getLastProjectId(): String? {
        return dataStore.data.map { it[LAST_PROJECT_ID] }.firstOrNull()?.await()
    }

    suspend fun setLastProjectId(projectId: String?) {
        dataStore.updateDataAsync { it[LAST_PROJECT_ID] = projectId }.await()
    }

    // Encryption using Android Keystore
    private fun encrypt(text: String): String {
        // In real implementation, use EncryptedSharedPreferences or Security library
        // For now, simple base64 (replace with proper encryption)
        return android.util.Base64.encodeToString(text.toByteArray(), android.util.Base64.DEFAULT)
    }

    private fun decrypt(encrypted: String): String {
        return String(android.util.Base64.decode(encrypted, android.util.Base64.DEFAULT))
    }
}