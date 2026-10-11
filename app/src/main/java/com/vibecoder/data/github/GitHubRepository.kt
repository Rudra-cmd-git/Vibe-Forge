package com.vibecoder.data.github

import android.content.Context
import com.vibecoder.data.git.JGitRepositoryManager
import com.vibecoder.data.git.GitRepository
import com.vibecoder.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

class GitHubRepository @Inject constructor(
    private val apiService: GitHubApiService,
    private val tokenManager: GitHubTokenManager,
    private val gitManager: JGitRepositoryManager,
    private val context: Context
) {

    private val clientId = "Ov23limk8qV7QbVFaH4O" // GitHub OAuth App Client ID for device flow
    private val scope = "repo user"

    suspend fun startDeviceFlow(): Result<GitHubOAuthDeviceCodeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.requestDeviceCode(clientId, scope)
            if (response.isSuccessful) {
                response.body()?.let { Result.Success(it) } ?: Result.Failure(Exception("Empty response"))
            } else {
                Result.Failure(Exception("Failed to start device flow: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun pollForToken(deviceCode: String): Result<GitHubOAuthTokenResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.pollForToken(clientId, deviceCode)
            if (response.isSuccessful) {
                response.body()?.let { Result.Success(it) } ?: Result.Failure(Exception("Empty response"))
            } else if (response.code() == 400) {
                val error = response.errorBody()?.string()
                if (error?.contains("authorization_pending") == true) {
                    Result.Failure(GitHubAuthException.Pending)
                } else if (error?.contains("slow_down") == true) {
                    Result.Failure(GitHubAuthException.SlowDown)
                } else if (error?.contains("expired_token") == true) {
                    Result.Failure(GitHubAuthException.Expired)
                } else {
                    Result.Failure(GitHubAuthException.Denied)
                }
            } else {
                Result.Failure(Exception("Failed to get token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun saveTokenFromResponse(tokenResponse: GitHubOAuthTokenResponse): Result<GitHubTokenManager.GitHubTokenData> = withContext(Dispatchers.IO) {
        try {
            val tokenData = GitHubTokenManager.GitHubTokenData(
                accessToken = tokenResponse.accessToken,
                tokenType = tokenResponse.tokenType,
                scope = tokenResponse.scope,
                expiresIn = tokenResponse.expiresIn?.toLong() ?: 0,
                refreshToken = tokenResponse.refreshToken
            )
            tokenManager.saveToken(tokenData)
            Result.Success(tokenData)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun savePersonalAccessToken(token: String): Result<GitHubTokenManager.GitHubTokenData> = withContext(Dispatchers.IO) {
        try {
            val tokenData = GitHubTokenManager.GitHubTokenData(
                accessToken = token,
                tokenType = "Bearer",
                scope = "repo",
                expiresIn = 0
            )
            tokenManager.saveToken(tokenData)
            Result.Success(tokenData)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getAuthenticatedUser(): Result<GitHubUser> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val response = apiService.getAuthenticatedUser(tokenManager.getAuthHeader(tokenData))
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Failure(Exception("Empty user response"))
                } else {
                    Result.Failure(handleApiError(response))
                }
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun listUserRepositories(): Result<List<GitHubRepository>> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val response = apiService.listUserRepositories(tokenManager.getAuthHeader(tokenData))
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Success(emptyList())
                } else {
                    Result.Failure(handleApiError(response))
                }
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun createRepository(request: CreateRepositoryRequest): Result<GitHubRepository> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val response = apiService.createRepository(tokenManager.getAuthHeader(tokenData), request)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Failure(Exception("Empty response"))
                } else {
                    Result.Failure(handleApiError(response))
                }
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun getRepository(owner: String, repo: String): Result<GitHubRepository> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val response = apiService.getRepository(tokenManager.getAuthHeader(tokenData), owner, repo)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Failure(Exception("Empty response"))
                } else {
                    Result.Failure(handleApiError(response))
                }
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun listBranches(owner: String, repo: String): Result<List<GitHubBranch>> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val response = apiService.listBranches(tokenManager.getAuthHeader(tokenData), owner, repo)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Success(emptyList())
                } else {
                    Result.Failure(handleApiError(response))
                }
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun createBranch(owner: String, repo: String, branchName: String, fromSha: String): Result<GitHubRef> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val request = CreateRefRequest("refs/heads/$branchName", fromSha)
                val response = apiService.createBranch(tokenManager.getAuthHeader(tokenData), owner, repo, request)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Failure(Exception("Empty response"))
                } else {
                    Result.Failure(handleApiError(response))
                }
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun listCommits(owner: String, repo: String, sha: String? = null, path: String? = null): Result<List<GitHubCommitDetail>> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val response = apiService.listCommits(tokenManager.getAuthHeader(tokenData), owner, repo, sha, path)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Success(emptyList())
                } else {
                    Result.Failure(handleApiError(response))
                }
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun mergeBranches(owner: String, repo: String, base: String, head: String, message: String?): Result<GitHubMergeResult> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val request = MergeRequest(base, head, message)
                val response = apiService.mergeBranches(tokenManager.getAuthHeader(tokenData), owner, repo, request)
                if (response.isSuccessful) {
                    response.body()?.let { Result.Success(it) } ?: Result.Failure(Exception("Empty response"))
                } else {
                    Result.Failure(handleApiError(response))
                }
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    // Local Git Operations (JGit)
    suspend fun cloneRepository(repoUrl: String, localPath: File, branch: String? = null): Result<GitRepository> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val credentials = UsernamePasswordCredentialsProvider(tokenData.accessToken, "")
                val git = gitManager.clone(repoUrl, localPath, credentials, branch)
                Result.Success(GitRepository(git, localPath))
            } catch (e: GitAPIException) {
                Result.Failure(GitException.fromJGit(e))
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun openRepository(localPath: File): Result<GitRepository> = withContext(Dispatchers.IO) {
        try {
            val git = gitManager.open(localPath)
            Result.Success(GitRepository(git, localPath))
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun commitAndPush(
        repo: GitRepository,
        message: String,
        authorName: String,
        authorEmail: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                gitManager.commit(repo.git, message, authorName, authorEmail)
                val credentials = UsernamePasswordCredentialsProvider(tokenData.accessToken, "")
                gitManager.push(repo.git, credentials)
                Result.Success(Unit)
            } catch (e: GitAPIException) {
                Result.Failure(GitException.fromJGit(e))
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun pull(repo: GitRepository): Result<Unit> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val credentials = UsernamePasswordCredentialsProvider(tokenData.accessToken, "")
                gitManager.pull(repo.git, credentials)
                Result.Success(Unit)
            } catch (e: GitAPIException) {
                Result.Failure(GitException.fromJGit(e))
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun fetch(repo: GitRepository): Result<Unit> = withContext(Dispatchers.IO) {
        tokenManager.getToken()?.let { tokenData ->
            try {
                val credentials = UsernamePasswordCredentialsProvider(tokenData.accessToken, "")
                gitManager.fetch(repo.git, credentials)
                Result.Success(Unit)
            } catch (e: GitAPIException) {
                Result.Failure(GitException.fromJGit(e))
            } catch (e: Exception) {
                Result.Failure(e)
            }
        } ?: Result.Failure(Exception("Not authenticated"))
    }

    suspend fun switchBranch(repo: GitRepository, branchName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            gitManager.checkout(repo.git, branchName)
            Result.Success(Unit)
        } catch (e: GitAPIException) {
            Result.Failure(GitException.fromJGit(e))
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun createLocalBranch(repo: GitRepository, branchName: String, startPoint: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            gitManager.createBranch(repo.git, branchName, startPoint)
            Result.Success(Unit)
        } catch (e: GitAPIException) {
            Result.Failure(GitException.fromJGit(e))
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun mergeBranch(repo: GitRepository, branchName: String, message: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            gitManager.merge(repo.git, branchName, message)
            Result.Success(Unit)
        } catch (e: GitAPIException) {
            Result.Failure(GitException.fromJGit(e))
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getCommitHistory(repo: GitRepository, maxCount: Int = 50): Result<List<GitCommit>> = withContext(Dispatchers.IO) {
        try {
            val commits = gitManager.getLog(repo.git, maxCount)
            Result.Success(commits)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getStatus(repo: GitRepository): Result<GitStatus> = withContext(Dispatchers.IO) {
        try {
            val status = gitManager.getStatus(repo.git)
            Result.Success(status)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun addAll(repo: GitRepository): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            gitManager.addAll(repo.git)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        tokenManager.clearToken()
        Result.Success(Unit)
    }

    private fun handleApiError(response: Response<*>): Exception {
        return try {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            GitHubApiException(response.code(), errorBody)
        } catch (e: Exception) {
            Exception("API error: ${response.code()}")
        }
    }

    sealed class GitHubAuthException(message: String) : Exception(message) {
        object Pending : GitHubAuthException("Authorization pending")
        object SlowDown : GitHubAuthException("Polling too fast")
        object Expired : GitHubAuthException("Device code expired")
        object Denied : GitHubAuthException("Authorization denied")
    }

    class GitHubApiException(code: Int, message: String) : Exception("GitHub API error ($code): $message")
}