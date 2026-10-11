package com.vibecoder.data.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository

data class GitRepository(
    val git: Git,
    val workDir: java.io.File
) {
    val repository: Repository = git.repository
    val repoName: String = workDir.name
}

data class GitCommit(
    val id: String,
    val shortId: String,
    val message: String,
    val shortMessage: String,
    val authorName: String,
    val authorEmail: String,
    val authorTime: Long,
    val committerName: String,
    val committerEmail: String,
    val committerTime: Long,
    val parents: List<String>,
    val treeId: String
)

data class GitStatus(
    val added: List<String>,
    val modified: List<String>,
    val removed: List<String>,
    val missing: List<String>,
    val untracked: List<String>,
    val changed: List<String>,
    val conflicting: List<String>,
    val hasChanges: Boolean
) {
    val totalChanges: Int = added.size + modified.size + removed.size + missing.size + untracked.size + conflicting.size
}

data class GitBranch(
    val name: String,
    val isCurrent: Boolean,
    val isRemote: Boolean,
    val upstream: String?,
    val lastCommitId: String?,
    val lastCommitMessage: String?
)

data class GitRemote(
    val name: String,
    val url: String,
    val fetchRefSpecs: List<String>,
    val pushRefSpecs: List<String>
)

sealed class GitException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data class NotFound(path: String) : GitException("Not found: $path")
    data class AlreadyExists(path: String) : GitException("Already exists: $path")
    data class NotARepository(path: String) : GitException("Not a git repository: $path")
    data class AuthenticationFailed(reason: String) : GitException("Authentication failed: $reason")
    data class Conflict(message: String) : GitException("Merge conflict: $message")
    data class UncommittedChanges(message: String) : GitException("Uncommitted changes: $message")
    data class InvalidBranch(name: String) : GitException("Invalid branch: $name")
    data class PushRejected(reason: String) : GitException("Push rejected: $reason")
    data class LargeFile(path: String, size: Long) : GitException("Large file: $path (${size} bytes)")
    data class SecretDetected(path: String, secretType: String) : GitException("Secret detected in $path: $secretType")
    data class Unknown(cause: Throwable) : GitException("Unknown error: ${cause.message}", cause)

    companion object {
        fun fromJGit(e: org.eclipse.jgit.api.errors.GitAPIException): GitException {
            return when (e) {
                is org.eclipse.jgit.api.errors.TransportException -> {
                    val msg = e.message ?: ""
                    if (msg.contains("Authentication") || msg.contains("401") || msg.contains("403")) {
                        AuthenticationFailed(msg)
                    } else if (msg.contains("rejected") || msg.contains("non-fast-forward")) {
                        PushRejected(msg)
                    } else {
                        Unknown(e)
                    }
                }
                is org.eclipse.jgit.api.errors.RefNotFoundException -> InvalidBranch(e.message ?: "")
                is org.eclipse.jgit.api.errors.RefAlreadyExistsException -> AlreadyExists(e.message ?: "")
                is org.eclipse.jgit.api.errors.NoHeadException -> NotFound("HEAD")
                is org.eclipse.jgit.api.errors.WrongRepositoryStateException -> Unknown(e)
                is org.eclipse.jgit.api.errors.ConcurrentRefUpdateException -> Conflict(e.message ?: "")
                is org.eclipse.jgit.api.errors.JGitInternalException -> Unknown(e)
                else -> Unknown(e)
            }
        }
    }
}