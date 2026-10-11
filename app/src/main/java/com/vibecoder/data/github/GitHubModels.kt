package com.vibecoder.data.github

import kotlinx.serialization.Serializable

@Serializable
data class GitHubUser(
    val login: String,
    val id: Long,
    val avatarUrl: String,
    val name: String?,
    val email: String?,
    val htmlUrl: String,
    val type: String,
    val siteAdmin: Boolean,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class GitHubRepository(
    val id: Long,
    val name: String,
    val fullName: String,
    val owner: GitHubUser,
    val description: String?,
    val private: Boolean,
    val htmlUrl: String,
    val cloneUrl: String,
    val sshUrl: String,
    val defaultBranch: String,
    val stargazersCount: Int,
    val forksCount: Int,
    val language: String?,
    val topics: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val pushedAt: String?,
    val size: Int,
    val hasIssues: Boolean,
    val hasProjects: Boolean,
    val hasWiki: Boolean,
    val archived: Boolean,
    val disabled: Boolean,
    val visibility: String
) {
    val displayName: String = fullName
    val isPrivate: Boolean = private
}

@Serializable
data class CreateRepositoryRequest(
    val name: String,
    val description: String? = null,
    val private: Boolean = false,
    val autoInit: Boolean = true,
    val gitignoreTemplate: String? = null,
    val licenseTemplate: String? = null
)

@Serializable
data class GitHubBranch(
    val name: String,
    val commit: GitHubCommit,
    val protected: Boolean
)

@Serializable
data class GitHubCommit(
    val sha: String,
    val url: String
)

@Serializable
data class GitHubCommitDetail(
    val sha: String,
    val commit: GitHubCommitInfo,
    val author: GitHubUser?,
    val committer: GitHubUser?,
    val parents: List<GitHubCommit>,
    val stats: GitHubCommitStats?,
    val files: List<GitHubCommitFile>?
)

@Serializable
data class GitHubCommitInfo(
    val author: GitHubCommitAuthor,
    val committer: GitHubCommitAuthor,
    val message: String,
    val tree: GitHubTree,
    val url: String,
    val commentCount: Int,
    val verification: GitHubVerification?
)

@Serializable
data class GitHubCommitAuthor(
    val name: String,
    val email: String,
    val date: String
)

@Serializable
data class GitHubTree(
    val sha: String,
    val url: String
)

@Serializable
data class GitHubCommitStats(
    val additions: Int,
    val deletions: Int,
    val total: Int
)

@Serializable
data class GitHubCommitFile(
    val sha: String,
    val filename: String,
    val status: String,
    val additions: Int,
    val deletions: Int,
    val changes: Int,
    val blobUrl: String,
    val rawUrl: String,
    val contentsUrl: String,
    val patch: String?
)

@Serializable
data class GitHubVerification(
    val verified: Boolean,
    val reason: String,
    val signature: String?,
    val payload: String?
)

@Serializable
data class GitHubOAuthDeviceCodeResponse(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresIn: Int,
    val interval: Int
)

@Serializable
data class GitHubOAuthTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val scope: String,
    val expiresIn: Int?,
    val refreshToken: String?
)

@Serializable
data class GitHubErrorResponse(
    val message: String,
    val documentationUrl: String?,
    val errors: List<GitHubErrorDetail>?
)

@Serializable
data class GitHubErrorDetail(
    val resource: String,
    val field: String,
    val code: String,
    val message: String
)

@Serializable
data class GitHubRef(
    val ref: String,
    val nodeId: String,
    val url: String,
    val object: GitHubObject
)

@Serializable
data class GitHubObject(
    val type: String,
    val sha: String,
    val url: String
)

@Serializable
data class CreateRefRequest(
    val ref: String,
    val sha: String
)

@Serializable
data class MergeRequest(
    val base: String,
    val head: String,
    val commitMessage: String?
)

@Serializable
data class GitHubMergeResult(
    val sha: String,
    val merged: Boolean,
    val message: String
)

@Serializable
data class GitHubContent(
    val type: String,
    val encoding: String?,
    val size: Long,
    val name: String,
    val path: String,
    val content: String?,
    val sha: String,
    val url: String,
    val gitUrl: String,
    val htmlUrl: String,
    val downloadUrl: String?,
    val links: GitHubContentLinks
)

@Serializable
data class GitHubContentLinks(
    val self: String,
    val git: String,
    val html: String
)

@Serializable
data class CreateFileRequest(
    val message: String,
    val content: String,
    val branch: String?,
    val committer: GitHubCommitAuthor?,
    val author: GitHubCommitAuthor?
)

@Serializable
data class UpdateFileRequest(
    val message: String,
    val content: String,
    val sha: String,
    val branch: String?,
    val committer: GitHubCommitAuthor?,
    val author: GitHubCommitAuthor?
)

@Serializable
data class DeleteFileRequest(
    val message: String,
    val sha: String,
    val branch: String?,
    val committer: GitHubCommitAuthor?,
    val author: GitHubCommitAuthor?
)

@Serializable
data class GitHubRateLimit(
    val limit: Int,
    val remaining: Int,
    val reset: Long,
    val used: Int
)

@Serializable
data class GitHubRateLimitResponse(
    val resources: GitHubRateLimitResources
)

@Serializable
data class GitHubRateLimitResources(
    val core: GitHubRateLimit,
    val search: GitHubRateLimit,
    val graphql: GitHubRateLimit
)