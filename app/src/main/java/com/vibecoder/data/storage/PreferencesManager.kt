package com.vibecoder.data.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val dataStore = RxPreferenceDataStoreBuilder(context, "settings").build()

    // Encrypted preferences keys
    private val KEY_GITHUB_CLIENT_SECRET = "github_client_secret"
    private val KEY_ANTHROPIC_API_KEY = "anthropic_api_key"
    private val KEY_OPENAI_API_KEY = "openai_api_key"
    private val KEY_GOOGLE_API_KEY = "google_api_key"

    // DataStore keys
    private val KEY_THEME = PreferencesKeys.stringKey("theme")
    private val KEY_BIOMETRIC_ENABLED = PreferencesKeys.booleanKey("biometric_enabled")
    private val KEY_AUTO_LOCK_TIMEOUT = PreferencesKeys.longKey("auto_lock_timeout")
    private val KEY_DEFAULT_AUTHOR_NAME = PreferencesKeys.stringKey("default_author_name")
    private val KEY_DEFAULT_AUTHOR_EMAIL = PreferencesKeys.stringKey("default_author_email")
    private val KEY_AUTO_COMMIT = PreferencesKeys.booleanKey("auto_commit")
    private val KEY_AUTO_PUSH = PreferencesKeys.booleanKey("auto_push")
    private val KEY_SHOW_LINE_NUMBERS = PreferencesKeys.booleanKey("show_line_numbers")
    private val KEY_CODE_FONT_SIZE = PreferencesKeys.intKey("code_font_size")
    private val KEY_TAB_SIZE = PreferencesKeys.intKey("tab_size")
    private val KEY_USE_SPACES = PreferencesKeys.booleanKey("use_spaces")
    private val KEY_AGENT_MODEL = PreferencesKeys.stringKey("agent_model")
    private val KEY_AGENT_TEMPERATURE = PreferencesKeys.floatKey("agent_temperature")
    private val KEY_AGENT_MAX_TOKENS = PreferencesKeys.intKey("agent_max_tokens")
    private val KEY_LANGUAGE = PreferencesKeys.stringKey("language")
    private val KEY_FIRST_LAUNCH = PreferencesKeys.booleanKey("first_launch")
    private val KEY_RECENT_PROJECTS = PreferencesKeys.stringKey("recent_projects")
    private val KEY_NOTIFICATIONS_ENABLED = PreferencesKeys.booleanKey("notifications_enabled")

    // In-memory state flows for reactive UI
    private val _theme = MutableStateFlow("system")
    val theme = _theme.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(true)
    val biometricEnabled = _biometricEnabled.asStateFlow()

    private val _autoLockTimeout = MutableStateFlow(300000L) // 5 minutes
    val autoLockTimeout = _autoLockTimeout.asStateFlow()

    private val _defaultAuthorName = MutableStateFlow("")
    val defaultAuthorName = _defaultAuthorName.asStateFlow()

    private val _defaultAuthorEmail = MutableStateFlow("")
    val defaultAuthorEmail = _defaultAuthorEmail.asStateFlow()

    private val _autoCommit = MutableStateFlow(true)
    val autoCommit = _autoCommit.asStateFlow()

    private val _autoPush = MutableStateFlow(false)
    val autoPush = _autoPush.asStateFlow()

    private val _showLineNumbers = MutableStateFlow(true)
    val showLineNumbers = _showLineNumbers.asStateFlow()

    private val _codeFontSize = MutableStateFlow(14)
    val codeFontSize = _codeFontSize.asStateFlow()

    private val _tabSize = MutableStateFlow(4)
    val tabSize = _tabSize.asStateFlow()

    private val _useSpaces = MutableStateFlow(true)
    val useSpaces = _useSpaces.asStateFlow()

    private val _agentModel = MutableStateFlow("claude-3-5-sonnet-20241022")
    val agentModel = _agentModel.asStateFlow()

    private val _agentTemperature = MutableStateFlow(0.7f)
    val agentTemperature = _agentTemperature.asStateFlow()

    private val _agentMaxTokens = MutableStateFlow(8192)
    val agentMaxTokens = _agentMaxTokens.asStateFlow()

    private val _language = MutableStateFlow("system")
    val language = _language.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load from DataStore
                val prefs = dataStore.data.firstOrNull().blockingGet() ?: Preferences.EMPTY
                
                _theme.value = prefs[KEY_THEME] ?: "system"
                _biometricEnabled.value = prefs[KEY_BIOMETRIC_ENABLED] ?: true
                _autoLockTimeout.value = prefs[KEY_AUTO_LOCK_TIMEOUT] ?: 300000L
                _defaultAuthorName.value = prefs[KEY_DEFAULT_AUTHOR_NAME] ?: ""
                _defaultAuthorEmail.value = prefs[KEY_DEFAULT_AUTHOR_EMAIL] ?: ""
                _autoCommit.value = prefs[KEY_AUTO_COMMIT] ?: true
                _autoPush.value = prefs[KEY_AUTO_PUSH] ?: false
                _showLineNumbers.value = prefs[KEY_SHOW_LINE_NUMBERS] ?: true
                _codeFontSize.value = prefs[KEY_CODE_FONT_SIZE] ?: 14
                _tabSize.value = prefs[KEY_TAB_SIZE] ?: 4
                _useSpaces.value = prefs[KEY_USE_SPACES] ?: true
                _agentModel.value = prefs[KEY_AGENT_MODEL] ?: "claude-3-5-sonnet-20241022"
                _agentTemperature.value = prefs[KEY_AGENT_TEMPERATURE] ?: 0.7f
                _agentMaxTokens.value = prefs[KEY_AGENT_MAX_TOKENS] ?: 8192
                _language.value = prefs[KEY_LANGUAGE] ?: "system"
                _notificationsEnabled.value = prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
            } catch (e: Exception) {
                // Use defaults
            }
        }
    }

    // Encrypted preferences for API keys
    fun saveGitHubClientSecret(secret: String) {
        encryptedPrefs.edit().putString(KEY_GITHUB_CLIENT_SECRET, secret).apply()
    }

    fun getGitHubClientSecret(): String? {
        return encryptedPrefs.getString(KEY_GITHUB_CLIENT_SECRET, null)
    }

    fun saveAnthropicApiKey(key: String) {
        encryptedPrefs.edit().putString(KEY_ANTHROPIC_API_KEY, key).apply()
    }

    fun getAnthropicApiKey(): String? {
        return encryptedPrefs.getString(KEY_ANTHROPIC_API_KEY, null)
    }

    fun saveOpenAiApiKey(key: String) {
        encryptedPrefs.edit().putString(KEY_OPENAI_API_KEY, key).apply()
    }

    fun getOpenAiApiKey(): String? {
        return encryptedPrefs.getString(KEY_OPENAI_API_KEY, null)
    }

    fun saveGoogleApiKey(key: String) {
        encryptedPrefs.edit().putString(KEY_GOOGLE_API_KEY, key).apply()
    }

    fun getGoogleApiKey(): String? {
        return encryptedPrefs.getString(KEY_GOOGLE_API_KEY, null)
    }

    fun clearAllApiKeys() {
        encryptedPrefs.edit()
            .remove(KEY_GITHUB_CLIENT_SECRET)
            .remove(KEY_ANTHROPIC_API_KEY)
            .remove(KEY_OPENAI_API_KEY)
            .remove(KEY_GOOGLE_API_KEY)
            .apply()
    }

    // DataStore settings
    private suspend fun updateDataStore(block: Preferences.Editor.() -> Unit) {
        dataStore.updateData { prefs ->
            prefs.toMutablePreferences().apply(block).freeze()
        }.await()
        loadSettings()
    }

    fun setTheme(theme: String) = updateDataStore { putString(KEY_THEME, theme) }
    fun setBiometricEnabled(enabled: Boolean) = updateDataStore { putBoolean(KEY_BIOMETRIC_ENABLED, enabled) }
    fun setAutoLockTimeout(timeout: Long) = updateDataStore { putLong(KEY_AUTO_LOCK_TIMEOUT, timeout) }
    fun setDefaultAuthorName(name: String) = updateDataStore { putString(KEY_DEFAULT_AUTHOR_NAME, name) }
    fun setDefaultAuthorEmail(email: String) = updateDataStore { putString(KEY_DEFAULT_AUTHOR_EMAIL, email) }
    fun setAutoCommit(enabled: Boolean) = updateDataStore { putBoolean(KEY_AUTO_COMMIT, enabled) }
    fun setAutoPush(enabled: Boolean) = updateDataStore { putBoolean(KEY_AUTO_PUSH, enabled) }
    fun setShowLineNumbers(enabled: Boolean) = updateDataStore { putBoolean(KEY_SHOW_LINE_NUMBERS, enabled) }
    fun setCodeFontSize(size: Int) = updateDataStore { putInt(KEY_CODE_FONT_SIZE, size) }
    fun setTabSize(size: Int) = updateDataStore { putInt(KEY_TAB_SIZE, size) }
    fun setUseSpaces(useSpaces: Boolean) = updateDataStore { putBoolean(KEY_USE_SPACES, useSpaces) }
    fun setAgentModel(model: String) = updateDataStore { putString(KEY_AGENT_MODEL, model) }
    fun setAgentTemperature(temp: Float) = updateDataStore { putFloat(KEY_AGENT_TEMPERATURE, temp) }
    fun setAgentMaxTokens(tokens: Int) = updateDataStore { putInt(KEY_AGENT_MAX_TOKENS, tokens) }
    fun setLanguage(lang: String) = updateDataStore { putString(KEY_LANGUAGE, lang) }
    fun setNotificationsEnabled(enabled: Boolean) = updateDataStore { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }

    fun setFirstLaunchComplete() = updateDataStore { putBoolean(KEY_FIRST_LAUNCH, false) }
    fun isFirstLaunch(): Boolean {
        return dataStore.data.map { it[KEY_FIRST_LAUNCH] ?: true }.blockingFirst()
    }

    fun addRecentProject(projectId: String) {
        val recent = getRecentProjects()
        val updated = (projectId +: recent.filter { it != projectId }.take(9)).distinct()
        saveRecentProjects(updated)
    }

    fun getRecentProjects(): List<String> {
        val json = encryptedPrefs.getString(KEY_RECENT_PROJECTS, "[]")
        return try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveRecentProjects(projects: List<String>) {
        val json = kotlinx.serialization.json.Json.encodeToString(projects)
        encryptedPrefs.edit().putString(KEY_RECENT_PROJECTS, json).apply()
    }

    fun clearRecentProjects() {
        encryptedPrefs.edit().remove(KEY_RECENT_PROJECTS).apply()
    }
}