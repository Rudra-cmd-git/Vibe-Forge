package com.vibecoder.data.github

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitHubTokenManager @Inject constructor(
    @Suppress("UNUSED_PARAMETER") private val context: Context
) {

    private val prefs by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "github_tokens",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val gson = Gson()
    private val tokenType = object : TypeToken<GitHubTokenData>() {}.type

    data class GitHubTokenData(
        val accessToken: String,
        val tokenType: String = "Bearer",
        val scope: String = "",
        val expiresIn: Long = 0,
        val refreshToken: String? = null,
        val obtainedAt: Long = System.currentTimeMillis()
    )

    suspend fun saveToken(tokenData: GitHubTokenData) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putString("github_token", gson.toJson(tokenData, tokenType))
            .apply()
    }

    suspend fun getToken(): GitHubTokenData? = withContext(Dispatchers.IO) {
        val json = prefs.getString("github_token", null)
        json?.let { gson.fromJson(it, tokenType) }
    }

    suspend fun clearToken() = withContext(Dispatchers.IO) {
        prefs.edit().remove("github_token").apply()
    }

    suspend fun hasToken(): Boolean = withContext(Dispatchers.IO) {
        prefs.contains("github_token")
    }

    fun getAuthHeader(tokenData: GitHubTokenData): String {
        return "${tokenData.tokenType} ${tokenData.accessToken}"
    }

    fun isTokenExpired(tokenData: GitHubTokenData): Boolean {
        if (tokenData.expiresIn <= 0) return false
        val expiryTime = tokenData.obtainedAt + (tokenData.expiresIn * 1000)
        return System.currentTimeMillis() >= expiryTime - 60000 // 1 min buffer
    }
}