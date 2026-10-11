# Agent module consumer ProGuard rules
# This file is included in apps that depend on this library

# Keep serializable classes for Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Tool interfaces and implementations
-keep interface com.vibecoder.agent.tools.Tool { *; }
-keep class * implements com.vibecoder.agent.tools.Tool { *; }

# Keep AgentLoop and related classes
-keep class com.vibecoder.agent.loop.AgentLoop { *; }
-keep class com.vibecoder.agent.loop.AgentLoop$* { *; }

# Keep memory classes
-keep class com.vibecoder.agent.memory.ProjectMemory { *; }

# Keep coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep Okio
-keep class okio.** { *; }