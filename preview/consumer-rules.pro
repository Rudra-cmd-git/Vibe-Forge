# Preview module consumer ProGuard rules

# Keep preview classes
-keep class com.vibecoder.preview.PreviewManager { *; }
-keep class com.vibecoder.preview.PreviewManager$* { *; }
-keep class com.vibecoder.preview.ConsoleLog { *; }
-keep class com.vibecoder.preview.DeviceSize { *; }
-keep class com.vibecoder.preview.ConsoleLevel { *; }

# Keep WebView
-keep class android.webkit.WebView { *; }
-keep class android.webkit.WebViewClient { *; }
-keep class android.webkit.WebChromeClient { *; }
-keep class android.webkit.ConsoleMessage { *; }

# Keep coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep Kotlinx HTML
-keep class kotlinx.html.** { *; }

# Keep Okio
-keep class okio.** { *; }