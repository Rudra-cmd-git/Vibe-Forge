package com.vibecoder.preview

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.Path

/**
 * Preview manager - handles live preview with WebView
 */
class PreviewManager(
    private val context: Context,
    private val projectRoot: Path
) : LifecycleObserver {

    private var webView: WebView? = null
    private val _previewUrl = MutableStateFlow("about:blank")
    val previewUrl = _previewUrl.asStateFlow()

    private val _consoleLogs = MutableStateFlow<List<ConsoleLog>>(emptyList())
    val consoleLogs = _consoleLogs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var devServerPort = 5173
    private var devServerProcess: Process? = null

    /**
     * Initialize WebView
     */
    fun initialize(webView: WebView) {
        this.webView = webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    _isLoading.value = false
                }

                override fun onReceivedError(view: WebView?, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError?) {
                    _isLoading.value = false
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    val log = ConsoleLog(
                        timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                        level = when (consoleMessage.messageLevel()) {
                            ConsoleMessage.MessageLevel.ERROR -> ConsoleLevel.Error
                            ConsoleMessage.MessageLevel.WARNING -> ConsoleLevel.Warn
                            ConsoleMessage.MessageLevel.DEBUG -> ConsoleLevel.Debug
                            else -> ConsoleLevel.Log
                        },
                        message = consoleMessage.message()
                    )
                    _consoleLogs.value = _consoleLogs.value + log
                    return true
                }
            }
            loadUrl("http://localhost:$devServerPort")
        }
    }

    /**
     * Start dev server for the project
     */
    suspend fun startDevServer(): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Check if package.json exists
                val packageJson = projectRoot.resolve("package.json")
                if (!packageJson.toFile().exists()) {
                    return@withContext false
                }

                // Start Vite dev server
                val command = arrayOf("npx", "vite", "--port", devServerPort.toString(), "--host", "0.0.0.0")
                val process = Runtime.getRuntime().exec(command, null, projectRoot.toFile())
                devServerProcess = process

                // Wait a bit for server to start
                Thread.sleep(3000)

                // Check if server is running
                val url = "http://localhost:$devServerPort"
                _previewUrl.value = url
                webView?.loadUrl(url)
                true
            } catch (e: Exception) {
                Log.e("PreviewManager", "Failed to start dev server", e)
                false
            }
        }
    }

    /**
     * Stop dev server
     */
    fun stopDevServer() {
        devServerProcess?.destroyForcibly()
        devServerProcess = null
    }

    /**
     * Reload preview
     */
    fun reload() {
        _isLoading.value = true
        webView?.reload()
    }

    /**
     * Set device size for responsive preview
     */
    fun setDeviceSize(deviceSize: DeviceSize) {
        webView?.evaluateJavascript(
            "document.documentElement.style.width = '${deviceSize.width}px'; document.documentElement.style.height = '${deviceSize.height}px';",
            null
        )
    }

    /**
     * Open in external browser
     */
    fun openInBrowser() {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(_previewUrl.value))
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Clear console logs
     */
    fun clearConsole() {
        _consoleLogs.value = emptyList()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        stopDevServer()
        webView?.destroy()
        webView = null
    }
}

data class ConsoleLog(
    val timestamp: String,
    val level: ConsoleLevel,
    val message: String
)

enum class ConsoleLevel { Info, Warn, Error, Debug, Log }

enum class DeviceSize(val label: String, val width: Int, val height: Int) {
    Phone("Phone", 390, 844),
    Tablet("Tablet", 768, 1024),
    Desktop("Desktop", 1440, 900)
}