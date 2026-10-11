# VibeCoder ProGuard Rules

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep class * extends dagger.hilt.android.HiltActivity
-keep class * extends dagger.hilt.android.HiltFragment
-keep class * extends dagger.hilt.android.HiltViewModel
-keep class * extends dagger.hilt.android.HiltService

# Keep Room entities and DAOs
-keep class com.vibecoder.data.storage.** { *; }

# Keep JGit classes
-keep class org.eclipse.jgit.** { *; }
-dontwarn org.eclipse.jgit.**

# Keep Retrofit and OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Moshi
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Keep Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Keep Kotlinx Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Keep Markwon
-keep class io.noties.markwon.** { *; }
-dontwarn io.noties.markwon.**

# Keep Editor
-keep class io.github.rosemoe.editor.** { *; }
-dontwarn io.github.rosemoe.editor.**

# Keep Security Crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Keep WorkManager
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Keep Biometric
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Keep DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Keep Hilt Navigation
-keep class androidx.hilt.navigation.** { *; }
-dontwarn androidx.hilt.navigation.**

# Keep Material3
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.material3.**

# Keep Navigation
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Keep Lifecycle
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Keep Activity/Fragment
-keep class androidx.activity.** { *; }
-keep class androidx.fragment.** { *; }
-dontwarn androidx.activity.**
-dontwarn androidx.fragment.**

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Kotlin stdlib
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# Keep our model classes
-keep class com.vibecoder.data.** { *; }
-keep class com.vibecoder.agent.** { *; }
-keep class com.vibecoder.ui.** { *; }

# Keep Parcelable
-keep class * implements android.os.Parcelable { *; }

# Keep Enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep annotation processors
-keep @interface * { *; }
-keep @kotlin.Metadata class *

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}