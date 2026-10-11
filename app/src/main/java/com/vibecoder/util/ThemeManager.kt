package com.vibecoder.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.sideeffect.LaunchedEffect
import com.vibecoder.data.storage.PreferencesManager

object ThemeManager {
    
    enum class ThemeMode(val value: String) {
        SYSTEM("system"),
        LIGHT("light"),
        DARK("dark")
    }

    fun applyTheme(activity: Activity) {
        val prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val themeValue = prefs.getString("theme", "system") ?: "system"
        val mode = ThemeMode.valueOf(themeValue.uppercase())
        
        when (mode) {
            ThemeMode.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    @Composable
    fun observeThemeChanges(preferencesManager: PreferencesManager) {
        val context = LocalContext.current
        LaunchedEffect(preferencesManager.theme) {
            val theme = preferencesManager.theme.value
            val mode = ThemeMode.valueOf(theme.uppercase())
            val activity = context as? Activity
            activity?.let {
                when (mode) {
                    ThemeMode.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                activity.recreate()
            }
        }
    }
}