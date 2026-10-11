# Providers module consumer ProGuard rules

# Keep provider interfaces and implementations
-keep interface com.vibecoder.providers.LlmProvider { *; }
-keep class * implements com.vibecoder.providers.LlmProvider { *; }
-keep class com.vibecoder.providers.ProviderFactory { *; }
-keep class com.vibecoder.providers.ProviderConfig { *; }
-keep class com.vibecoder.providers.ProviderResult { *; }
-keep class com.vibecoder.providers.ModelInfo { *; }
-keep class com.vibecoder.providers.Usage { *; }
-keep class com.vibecoder.providers.Pricing { *; }
-keep class com.vibecoder.providers.ModelCapabilities { *; }
-keep class com.vibecoder.providers.ProviderMetadata { *; }

# Keep serializable classes for Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep OkHttp and Retrofit
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class com.squareup.okhttp3.** { *; }

# Keep Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }