package com.vibecoder.data.storage

import androidx.room.*
import kotlinx.serialization.Serializable
import java.util.concurrent.Executors

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val localPath: String,
    val githubRepoId: Long?,
    val githubOwner: String?,
    val githubRepoName: String?,
    val defaultBranch: String,
    val projectType: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSyncedAt: Long?,
    val autoCommit: Boolean,
    val autoPush: Boolean,
    val isFavorite: Boolean
)

@Entity(tableName = "commits")
data class CommitEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val sha: String,
    val shortSha: String,
    val message: String,
    val shortMessage: String,
    val authorName: String,
    val authorEmail: String,
    val authorTime: Long,
    val committerName: String,
    val committerEmail: String,
    val committerTime: Long,
    val parents: String, // JSON array
    val treeId: String,
    val isLocal: Boolean,
    val isPushed: Boolean
)

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey val id: String, // projectId + ":" + branchName
    val projectId: String,
    val name: String,
    val isCurrent: Boolean,
    val isRemote: Boolean,
    val upstream: String?,
    val lastCommitId: String?,
    val lastCommitMessage: String?,
    val updatedAt: Long
)

@Entity(tableName = "remotes")
data class RemoteEntity(
    @PrimaryKey val id: String, // projectId + ":" + remoteName
    val projectId: String,
    val name: String,
    val url: String,
    val fetchRefSpecs: String, // JSON array
    val pushRefSpecs: String // JSON array
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val projectId: String?,
    val title: String,
    val model: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messageCount: Int
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String, // user, assistant, system, tool
    val content: String,
    val toolCalls: String?, // JSON
    val toolResults: String?, // JSON
    val createdAt: Long,
    val tokenCount: Int
)

@Entity(tableName = "agent_tasks")
data class AgentTaskEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val sessionId: String?,
    val prompt: String,
    val status: String, // pending, running, completed, failed, cancelled
    val result: String?,
    val error: String?,
    val filesChanged: String, // JSON array
    val commitSha: String?,
    val createdAt: Long,
    val startedAt: Long?,
    val completedAt: Long?
)

@Entity(tableName = "github_repositories")
data class GitHubRepositoryEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val fullName: String,
    val ownerLogin: String,
    val ownerAvatarUrl: String,
    val description: String?,
    val isPrivate: Boolean,
    val htmlUrl: String,
    val cloneUrl: String,
    val sshUrl: String,
    val defaultBranch: String,
    val stargazersCount: Int,
    val forksCount: Int,
    val language: String?,
    val topics: String, // JSON array
    val createdAt: String,
    val updatedAt: String,
    val pushedAt: String?,
    val size: Int,
    val visibility: String,
    val lastFetchedAt: Long
)

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): androidx.lifecycle.LiveData<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE githubRepoId = :repoId")
    suspend fun getProjectByGitHubRepoId(repoId: Long): ProjectEntity?

    @Query("SELECT * FROM projects WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteProjects(): androidx.lifecycle.LiveData<List<ProjectEntity>>
}

@Dao
interface CommitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(commits: List<CommitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(commit: CommitEntity)

    @Query("SELECT * FROM commits WHERE projectId = :projectId ORDER BY authorTime DESC LIMIT :limit OFFSET :offset")
    suspend fun getCommits(projectId: String, limit: Int, offset: Int): List<CommitEntity>

    @Query("SELECT * FROM commits WHERE projectId = :projectId AND sha = :sha")
    suspend fun getCommitBySha(projectId: String, sha: String): CommitEntity?

    @Query("SELECT COUNT(*) FROM commits WHERE projectId = :projectId")
    suspend fun getCommitCount(projectId: String): Int

    @Query("DELETE FROM commits WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface BranchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(branches: List<BranchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(branch: BranchEntity)

    @Query("SELECT * FROM branches WHERE projectId = :projectId ORDER BY isCurrent DESC, updatedAt DESC")
    suspend fun getBranches(projectId: String): List<BranchEntity>

    @Query("SELECT * FROM branches WHERE projectId = :projectId AND name = :name")
    suspend fun getBranch(projectId: String, name: String): BranchEntity?

    @Query("DELETE FROM branches WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface RemoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remotes: List<RemoteEntity>)

    @Query("SELECT * FROM remotes WHERE projectId = :projectId")
    suspend fun getRemotes(projectId: String): List<RemoteEntity>

    @Query("DELETE FROM remotes WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: String)
}

@Dao
interface ChatSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ChatSessionEntity)

    @Update
    suspend fun update(session: ChatSessionEntity)

    @Delete
    suspend fun delete(session: ChatSessionEntity)

    @Query("SELECT * FROM chat_sessions WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getSessionsForProject(projectId: String): androidx.lifecycle.LiveData<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE projectId IS NULL ORDER BY updatedAt DESC")
    fun getGlobalSessions(): androidx.lifecycle.LiveData<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): ChatSessionEntity?
}

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getMessagesForSession(sessionId: String): androidx.lifecycle.LiveData<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteAllForSession(sessionId: String)
}

@Dao
interface AgentTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: AgentTaskEntity)

    @Update
    suspend fun update(task: AgentTaskEntity)

    @Query("SELECT * FROM agent_tasks WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getTasksForProject(projectId: String): androidx.lifecycle.LiveData<List<AgentTaskEntity>>

    @Query("SELECT * FROM agent_tasks WHERE id = :id")
    suspend fun getTaskById(id: String): AgentTaskEntity?

    @Query("SELECT * FROM agent_tasks WHERE status IN ('pending', 'running') ORDER BY createdAt ASC")
    suspend fun getActiveTasks(): List<AgentTaskEntity>
}

@Dao
interface GitHubRepositoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repos: List<GitHubRepositoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repo: GitHubRepositoryEntity)

    @Query("SELECT * FROM github_repositories ORDER BY updatedAt DESC")
    fun getAllRepositories(): androidx.lifecycle.LiveData<List<GitHubRepositoryEntity>>

    @Query("SELECT * FROM github_repositories WHERE id = :id")
    suspend fun getRepositoryById(id: Long): GitHubRepositoryEntity?

    @Query("DELETE FROM github_repositories")
    suspend fun deleteAll()
}

@Database(
    entities = [
        ProjectEntity::class,
        CommitEntity::class,
        BranchEntity::class,
        RemoteEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        AgentTaskEntity::class,
        GitHubRepositoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VibeCoderDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun commitDao(): CommitDao
    abstract fun branchDao(): BranchDao
    abstract fun remoteDao(): RemoteDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun agentTaskDao(): AgentTaskDao
    abstract fun gitHubRepositoryDao(): GitHubRepositoryDao

    companion object {
        @Volatile
        private var INSTANCE: VibeCoderDatabase? = null

        fun getInstance(context: android.content.Context): VibeCoderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VibeCoderDatabase::class.java,
                    "vibecoder.db"
                )
                    .setQueryExecutor(Executors.newSingleThreadExecutor())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return value?.let { kotlinx.serialization.json.Json.decodeFromString<List<String>>(it) } ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.let { kotlinx.serialization.json.Json.encodeToString(it) }
    }
}