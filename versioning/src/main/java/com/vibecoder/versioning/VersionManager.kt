package com.vibecoder.versioning

import com.vibecoder.design.components.VersionEntry
import com.vibecoder.storage.git.GitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okio.Path

/**
 * Versioning manager - handles git-based version timeline with undo/restore
 */
class VersionManager(
    private val projectRoot: Path
) {
    private val git = GitRepository(projectRoot)
    private val _versions = MutableStateFlow<List<VersionEntry>>(emptyList())
    val versions = _versions.asStateFlow()
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    init {
        loadVersions()
    }

    /**
     * Load version history from git
     */
    private fun loadVersions() {
        if (git.isInitialized()) {
            val commits = git.getHistory(100)
            val entries = commits.map { commit ->
                VersionEntry(
                    id = commit.hash,
                    summary = commit.message,
                    timestamp = commit.date,
                    success = true,
                    commitHash = commit.hash
                )
            }
            _versions.value = entries
            _currentIndex.value = entries.lastIndex
        }
    }

    /**
     * Record a new version after agent changes
     */
    fun recordVersion(summary: String, filesChanged: Int, success: Boolean = true) {
        val commitHash = git.getCurrentCommit() ?: java.util.UUID.randomUUID().toString()
        val entry = VersionEntry(
            id = commitHash,
            summary = summary,
            timestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            success = success,
            filesChanged = filesChanged,
            commitHash = commitHash
        )

        _versions.value = _versions.value + entry
        _currentIndex.value = _versions.value.lastIndex
    }

    /**
     * Undo last change - checkout previous commit
     */
    suspend fun undo(): Boolean {
        return withContext(Dispatchers.IO) {
            val currentIdx = _currentIndex.value
            if (currentIdx > 0) {
                val targetCommit = _versions.value[currentIdx - 1].commitHash
                if (targetCommit != null && git.checkout(targetCommit)) {
                    _currentIndex.value = currentIdx - 1
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    /**
     * Restore to a specific version
     */
    suspend fun restoreVersion(index: Int): Boolean {
        return withContext(Dispatchers.IO) {
            if (index in _versions.value.indices) {
                val targetCommit = _versions.value[index].commitHash
                if (targetCommit != null && git.checkout(targetCommit)) {
                    _currentIndex.value = index
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    /**
     * Get current version
     */
    fun getCurrentVersion(): VersionEntry? {
        val versions = _versions.value
        val idx = _currentIndex.value
        return if (idx in versions.indices) versions[idx] else null
    }

    /**
     * Create a named tag for important milestones
     */
    fun createMilestone(name: String, message: String): Boolean {
        return git.tag(name, message)
    }
}