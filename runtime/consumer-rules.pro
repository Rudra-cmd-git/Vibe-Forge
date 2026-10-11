# Runtime module consumer ProGuard rules

# Keep PRoot runtime classes
-keep class com.vibecoder.runtime.proot.ProotRuntime { *; }
-keep class com.vibecoder.runtime.proot.ProotRuntime$* { *; }
-keep class com.vibecoder.runtime.ProotForegroundService { *; }

# Keep execution result classes
-keep class com.vibecoder.runtime.proot.ExecutionResult { *; }
-keep class com.vibecoder.runtime.proot.RuntimeInfo { *; }

# Keep coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep Okio
-keep class okio.** { *; }