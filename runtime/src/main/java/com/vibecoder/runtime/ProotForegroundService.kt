package com.vibecoder.runtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.vibecoder.runtime.proot.ProotRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Foreground service to keep PRoot alive
 */
class ProotForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "vibecoder_runtime"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "START_RUNTIME"
        const val ACTION_STOP = "STOP_RUNTIME"
        const val EXTRA_PROJECT_PATH = "project_path"
    }

    private var prootRuntime: ProotRuntime? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val projectPath = intent?.getStringExtra(EXTRA_PROJECT_PATH)

        when (action) {
            ACTION_START -> {
                if (projectPath != null) {
                    startRuntime(projectPath)
                }
                showNotification("Runtime starting…", 0f)
            }
            ACTION_STOP -> {
                stopRuntime()
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun startRuntime(projectPath: String) {
        val projectRoot = okio.Path(projectPath)
        prootRuntime = ProotRuntime(this, projectRoot)

        scope.launch {
            val success = prootRuntime?.initialize { message, progress ->
                updateNotification(message, progress)
            } ?: false

            if (success) {
                updateNotification("Runtime ready", 1f)
            } else {
                updateNotification("Runtime failed", -1f)
            }
        }
    }

    private fun stopRuntime() {
        prootRuntime?.shutdown()
        prootRuntime = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vibe Coder Runtime",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background runtime for AI coding agent"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(text: String, progress: Float) {
        val notification = buildNotification(text, progress)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification(text: String, progress: Float) {
        val notification = buildNotification(text, progress)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(text: String, progress: Float): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("Vibe Coder Runtime")
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (progress >= 0 && progress <= 1) {
            builder.setProgress(100, (progress * 100).toInt(), false)
        }

        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopRuntime()
        super.onDestroy()
    }
}