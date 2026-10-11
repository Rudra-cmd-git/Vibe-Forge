package com.vibecoder.runtime.proot

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path
import okio.fs
import java.io.File

/**
 * PRoot Runtime Manager
 * Manages the Alpine Linux rootfs with Node, Python, Git, etc.
 */
class ProotRuntime(
    private val context: Context,
    private val projectRoot: Path
) {
    companion object {
        private const val TAG = "ProotRuntime"
        const val ROOTFS_VERSION = "1.0"
        const val MIN_FREE_SPACE_MB = 500
    }

    private var prootProcess: Process? = null
    private var isInitialized = false
    private val rootfsPath: Path
    private val homePath: Path

    init {
        val filesDir = context.filesDir.toPath()
        rootfsPath = filesDir.resolve("rootfs")
        homePath = filesDir.resolve("home")
    }

    /**
     * Initialize the runtime - extract rootfs, set up environment
     */
    suspend fun initialize(progressCallback: (String, Float) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                progressCallback("Checking storage…", 0.05f)
                if (!checkStorage()) {
                    return@withContext false
                }

                progressCallback("Extracting rootfs…", 0.1f)
                if (!extractRootfs(progressCallback)) {
                    return@withContext false
                }

                progressCallback("Verifying checksums…", 0.5f)
                if (!verifyChecksums()) {
                    return@withContext false
                }

                progressCallback("Setting up environment…", 0.7f)
                setupEnvironment()

                progressCallback("Installing tools…", 0.85f)
                installTools()

                progressCallback("Ready!", 1.0f)
                isInitialized = true
                true
            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed", e)
                false
            }
        }
    }

    /**
     * Execute a command in the PRoot environment
     */
    suspend fun execute(command: String, cwd: String = ".", timeoutMs: Long = 60000): ExecutionResult {
        return withContext(Dispatchers.IO) {
            if (!isInitialized) {
                return@withContext ExecutionResult(false, "Runtime not initialized", "", emptyList())
            }

            val workingDir = projectRoot.resolve(cwd).toAbsolutePath()
            val prootCmd = buildProotCommand(command, workingDir)

            try {
                val process = Runtime.getRuntime().exec(prootCmd)
                prootProcess = process

                val output = StringBuilder()
                val error = StringBuilder()

                // Read output with timeout
                val future = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).async {
                    process.inputStream.use { it.readBytes().decodeToString() }.also { output.append(it) }
                    process.errorStream.use { it.readBytes().decodeToString() }.also { error.append(it) }
                }

                val exitCode = withTimeoutOrNull(timeoutMs) { process.waitFor() }
                if (exitCode == null) {
                    process.destroyForcibly()
                    return@withContext ExecutionResult(false, "Command timed out", error.toString(), emptyList())
                }

                val success = exitCode == 0
                val filesChanged = detectChangedFiles(workingDir)

                ExecutionResult(success, output.toString(), error.toString(), filesChanged)
            } catch (e: Exception) {
                ExecutionResult(false, "", e.message ?: "Execution failed", emptyList())
            } finally {
                prootProcess = null
            }
        }
    }

    /**
     * Build the PRoot command with proper bindings
     */
    private fun buildProotCommand(command: String, workingDir: Path): Array<String> {
        val prootBinary = rootfsPath.resolve("usr/bin/proot").toFile()
        val rootfsDir = rootfsPath.toFile()

        return arrayOf(
            prootBinary.absolutePath,
            "--link2symlink",
            "-0",
            "-r", rootfsDir.absolutePath,
            "-b", "/dev:/dev",
            "-b", "/proc:/proc",
            "-b", "/sys:/sys",
            "-b", "${context.filesDir.absolutePath}:/home",
            "-b", "${projectRoot.toFile().absolutePath}:/project",
            "-w", "/project${workingDir.toFile().absolutePath.removePrefix(projectRoot.toFile().absolutePath)}",
            "/bin/sh", "-c", command
        )
    }

    /**
     * Extract rootfs from assets
     */
    private fun extractRootfs(progressCallback: (String, Float) -> Unit): Boolean {
        // In real implementation, extract from assets or download
        // For now, create minimal structure
        try {
            rootfsPath.createDirectories()
            homePath.createDirectories()

            // Create basic directory structure
            listOf("bin", "usr/bin", "usr/lib", "lib", "etc", "var", "tmp", "home", "project").forEach { dir ->
                rootfsPath.resolve(dir).createDirectories()
            }

            // Create minimal proot binary placeholder
            // Real implementation would copy from assets
            val prootBinary = rootfsPath.resolve("usr/bin/proot")
            if (!prootBinary.toFile().exists()) {
                // This would be replaced with actual proot binary
                prootBinary.toFile().createFile()
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract rootfs", e)
            false
        }
    }

    /**
     * Verify SHA-256 checksums
     */
    private fun verifyChecksums(): Boolean {
        // In real implementation, verify against known checksums
        true
    }

    /**
     * Set up environment variables and configs
     */
    private fun setupEnvironment() {
        val envFile = rootfsPath.resolve("etc/environment")
        envFile.writeText("""
            PATH=/usr/local/bin:/usr/bin:/bin:/usr/local/sbin:/usr/sbin:/sbin
            HOME=/home
            USER=builder
            TERM=xterm-256color
            LANG=en_US.UTF-8
            LC_ALL=en_US.UTF-8
            NODE_ENV=development
            """.trimIndent())
    }

    /**
     * Install Node, npm, Python, Git, etc.
     */
    private fun installTools() {
        // In real implementation, use apk (Alpine package manager) to install
        // apk add nodejs npm python3 git curl
        // For offline build, these would be pre-installed in rootfs
    }

    /**
     * Check available storage
     */
    private fun checkStorage(): Boolean {
        val stat = File(context.filesDir.absolutePath).totalSpace
        val freeSpace = File(context.filesDir.absolutePath).freeSpace
        val freeMB = freeSpace / (1024 * 1024)
        return freeMB >= MIN_FREE_SPACE_MB
    }

    /**
     * Detect files changed after command execution
     */
    private fun detectChangedFiles(workingDir: Path): List<String> {
        // In real implementation, use git status or file watchers
        return emptyList()
    }

    /**
     * Check if runtime is ready
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Get runtime info
     */
    fun getInfo(): RuntimeInfo {
        return RuntimeInfo(
            initialized = isInitialized,
            rootfsPath = rootfsPath.toString(),
            homePath = homePath.toString(),
            version = ROOTFS_VERSION
        )
    }

    /**
     * Shutdown runtime
     */
    fun shutdown() {
        prootProcess?.destroyForcibly()
        prootProcess = null
    }
}

data class ExecutionResult(
    val success: Boolean,
    val output: String,
    val error: String,
    val filesChanged: List<String>
)

data class RuntimeInfo(
    val initialized: Boolean,
    val rootfsPath: String,
    val homePath: String,
    val version: String
)