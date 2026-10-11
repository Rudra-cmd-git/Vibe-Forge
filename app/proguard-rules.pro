# Vibe Coder ProGuard Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }

# Keep Room entities and DAOs
-keep class com.vibecoder.storage.** { *; }

# Keep serialization classes
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.* *;
}

# Keep OkHttp and Retrofit
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep class com.squareup.okhttp3.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Material3
-keep class androidx.compose.material3.** { *; }

# Keep Accompanist
-keep class com.google.accompanist.** { *; }

# Keep Landscape
-keep class com.chrynan.landscape.** { *; }

# Keep our model classes
-keep class com.vibecoder.design.components.** { *; }
-keep class com.vibecoder.providers.** { *; }
-keep class com.vibecoder.agent.** { *; }
-keep class com.vibecoder.runtime.** { *; }

# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep ProRoot native libraries
-keep class com.vibecoder.runtime.proot.** { *; }