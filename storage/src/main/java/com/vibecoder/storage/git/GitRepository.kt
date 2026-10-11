package com.vibecoder.storage.git

import okio.Path
import okio.fs
import java.io.File

/**
 * Git repository manager for project versioning
 */
class GitRepository(
    private val projectRoot: Path
) {
    private val gitDir = projectRoot.resolve(".git")

    /**
     * Initialize git repository
     */
    fun init(): Boolean {
        return try {
            if (!gitDir.toFile().exists()) {
                val process = Runtime.getRuntime().exec(arrayOf("git", "init"), null, projectRoot.toFile())
                process.waitFor() == 0
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if repository is initialized
     */
    fun isInitialized(): Boolean = gitDir.toFile().exists()

    /**
     * Stage all changes
     */
    fun addAll(): Boolean {
        return executeGit("add", "-A")
    }

    /**
     * Commit changes
     */
    fun commit(message: String): Boolean {
        return executeGit("commit", "-m", message)
    }

    /**
     * Get commit history
     */
    fun getHistory(limit: Int = 50): List<GitCommit> {
        val output = executeGitCapture("log", "--oneline", "-n", limit.toString(), "--pretty=format:%H|%s|%an|%ad", "--date=short")
        return output.lines().mapNotNull { line ->
            val parts = line.split("|", limit = 4)
            if (parts.size == 4) {
                GitCommit(parts[0], parts[1], parts[2], parts[3])
            } else null
        }
    }

    /**
     * Get current commit hash
     */
    fun getCurrentCommit(): String? {
        val output = executeGitCapture("rev-parse", "HEAD")
        return output.trim().takeIf { it.isNotEmpty() }
    }

    /**
     * Checkout a specific commit
     */
    fun checkout(commitHash: String): Boolean {
        return executeGit("checkout", commitHash)
    }

    /**
     * Get status
     */
    fun getStatus(): GitStatus {
        val output = executeGitCapture("status", "--porcelain")
        val lines = output.lines().filter { it.isNotEmpty() }
        val staged = lines.count { it[1] != ' ' }
        val unstaged = lines.count { it[2] != ' ' }
        val untracked = lines.count { it.startsWith("??") }
        return GitStatus(staged, unstaged, untracked)
    }

    /**
     * Get diff for a file
     */
    fun getDiff(filePath: String? = null): String {
        val args = if (filePath != null) arrayOf("diff", filePath) else arrayOf("diff")
        return executeGitCapture(*args)
    }

    /**
     * Create a tag
     */
    fun tag(tagName: String, message: String): Boolean {
        return executeGit("tag", "-a", tagName, "-m", message)
    }

    private fun executeGit(vararg args: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("git") + args, null, projectRoot.toFile())
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun executeGitCapture(vararg args: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("git") + args, null, projectRoot.toFile())
            val output = process.inputStream.readBytes().decodeToString()
            process.waitFor()
            output
        } catch (e: Exception) {
            ""
        }
    }
}

data class GitCommit(
    val hash: String,
    val message: String,
    val author: String,
    val date: String
)

data class GitStatus(
    val staged: Int,
    val unstaged: Int,
    val untracked: Int
)