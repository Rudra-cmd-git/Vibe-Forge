# Storage module consumer ProGuard rules

# Keep storage interfaces and implementations
-keep class com.vibecoder.storage.prefs.PreferencesRepository { *; }
-keep class com.vibecoder.storage.git.GitRepository { *; }
-keep class com.vibecoder.storage.git.GitCommit { *; }
-keep class com.vibecoder.storage.git.GitStatus { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep Okio
-keep class okio.** { *; }