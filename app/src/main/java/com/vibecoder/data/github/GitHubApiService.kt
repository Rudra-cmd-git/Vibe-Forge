package com.vibecoder.data.github

import retrofit2.Response
import retrofit2.http.*

interface GitHubApiService {

    // OAuth Device Flow
    @POST("login/device/code")
    @FormUrlEncoded
    suspend fun requestDeviceCode(
        @Field("client_id") clientId: String,
        @Field("scope") scope: String
    ): Response<GitHubOAuthDeviceCodeResponse>

    @POST("login/oauth/access_token")
    @FormUrlEncoded
    suspend fun pollForToken(
        @Field("client_id") clientId: String,
        @Field("device_code") deviceCode: String,
        @Field("grant_type") grantType: String = "urn:ietf:params:oauth:grant-type:device_code"
    ): Response<GitHubOAuthTokenResponse>

    // User
    @GET("user")
    suspend fun getAuthenticatedUser(
        @Header("Authorization") authHeader: String
    ): Response<GitHubUser>

    // Repositories
    @GET("user/repos")
    suspend fun listUserRepositories(
        @Header("Authorization") authHeader: String,
        @Query("type") type: String = "all",
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): Response<List<GitHubRepository>>

    @GET("users/{username}/repos")
    suspend fun listPublicRepositories(
        @Header("Authorization") authHeader: String,
        @Path("username") username: String,
        @Query("type") type: String = "all",
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): Response<List<GitHubRepository>>

    @POST("user/repos")
    suspend fun createRepository(
        @Header("Authorization") authHeader: String,
        @Body request: CreateRepositoryRequest
    ): Response<GitHubRepository>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GitHubRepository>

    @DELETE("repos/{owner}/{repo}")
    suspend fun deleteRepository(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<Unit>

    // Branches
    @GET("repos/{owner}/{repo}/branches")
    suspend fun listBranches(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): Response<List<GitHubBranch>>

    @GET("repos/{owner}/{repo}/branches/{branch}")
    suspend fun getBranch(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("branch") branch: String
    ): Response<GitHubBranch>

    @POST("repos/{owner}/{repo}/git/refs")
    suspend fun createBranch(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body request: CreateRefRequest
    ): Response<GitHubRef>

    @DELETE("repos/{owner}/{repo}/git/refs/heads/{branch}")
    suspend fun deleteBranch(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("branch") branch: String
    ): Response<Unit>

    // Commits
    @GET("repos/{owner}/{repo}/commits")
    suspend fun listCommits(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("sha") sha: String? = null,
        @Query("path") path: String? = null,
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1
    ): Response<List<GitHubCommitDetail>>

    @GET("repos/{owner}/{repo}/commits/{sha}")
    suspend fun getCommit(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String
    ): Response<GitHubCommitDetail>

    // Merging
    @POST("repos/{owner}/{repo}/merges")
    suspend fun mergeBranches(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body request: MergeRequest
    ): Response<GitHubMergeResult>

    // Contents
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContents(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Query("ref") ref: String? = null
    ): Response<Any> // Can be List<GitHubContent> or GitHubContent

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun createFile(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body request: CreateFileRequest
    ): Response<GitHubContentResponse>

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun updateFile(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body request: UpdateFileRequest
    ): Response<GitHubContentResponse>

    @DELETE("repos/{owner}/{repo}/contents/{path}")
    suspend fun deleteFile(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Query("message") message: String,
        @Query("sha") sha: String,
        @Query("branch") branch: String? = null
    ): Response<GitHubContentResponse>

    // Rate Limit
    @GET("rate_limit")
    suspend fun getRateLimit(
        @Header("Authorization") authHeader: String
    ): Response<GitHubRateLimitResponse>

    @HEAD
    suspend fun checkRateLimit(
        @Header("Authorization") authHeader: String
    ): Response<Unit>
}

@Serializable
data class GitHubContentResponse(
    val content: GitHubContent?,
    val commit: GitHubCommitDetail?
)