# Versioning module consumer ProGuard rules

# Keep versioning classes
-keep class com.vibecoder.versioning.VersionManager { *; }
-keep class com.vibecoder.versioning.VersionManager$* { *; }

# Keep design system VersionEntry
-keep class com.vibecoder.design.components.VersionEntry { *; }

# Keep storage GitRepository
-keep class com.vibecoder.storage.git.GitRepository { *; }
-keep class com.vibecoder.storage.git.GitCommit { *; }

# Keep coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }