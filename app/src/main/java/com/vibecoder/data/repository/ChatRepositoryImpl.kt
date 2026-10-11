package com.vibecoder.data.repository

import com.vibecoder.data.storage.ChatMessageEntity
import com.vibecoder.data.storage.ChatSessionEntity
import com.vibecoder.data.storage.VibeCoderDatabase
import com.vibecoder.design.components.ChatMessageData
import com.vibecoder.design.components.ChatRole
import com.vibecoder.design.components.VersionEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ChatRepository implementation using Room database
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val database: VibeCoderDatabase
) : ChatRepository {

    override suspend fun loadMessages(projectId: String): List<ChatMessageData> {
        // Find or create a chat session for this project
        val session = database.chatSessionDao().getSessionsForProject(projectId).firstOrNull()?.await()
            ?: createSession(projectId)

        return database.chatMessageDao().getMessagesForSession(session.id).firstOrNull()?.await()?.map { entity ->
            toChatMessageData(entity)
        } ?: emptyList()
    }

    override suspend fun saveMessage(projectId: String, message: ChatMessageData) {
        val session = database.chatSessionDao().getSessionsForProject(projectId).firstOrNull()?.await()
            ?: createSession(projectId)

        val entity = ChatMessageEntity(
            id = message.id,
            sessionId = session.id,
            role = message.role.name.lowercase(),
            content = message.content,
            toolCalls = null,
            toolResults = null,
            createdAt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).parse(message.timestamp)?.time ?: System.currentTimeMillis(),
            tokenCount = 0
        )
        database.chatMessageDao().insert(entity)
    }

    override suspend fun loadVersions(projectId: String): List<VersionEntry> {
        val commits = database.commitDao().getCommits(projectId, 50, 0)
        return commits.map { commit ->
            VersionEntry(
                id = commit.sha,
                summary = commit.shortMessage,
                timestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(commit.authorTime)),
                success = true,
                filesChanged = null,
                duration = null,
                commitHash = commit.sha
            )
        }
    }

    override suspend fun saveVersion(projectId: String, version: VersionEntry) {
        // Version is saved via git commit, which is already tracked in commits table
    }

    private fun createSession(projectId: String): ChatSessionEntity {
        val sessionId = java.util.UUID.randomUUID().toString()
        val session = ChatSessionEntity(
            id = sessionId,
            projectId = projectId,
            title = "Chat",
            model = "Claude 3.5 Sonnet",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            messageCount = 0
        )
        database.chatSessionDao().insert(session)
        return session
    }

    private fun toChatMessageData(entity: ChatMessageEntity): ChatMessageData {
        return ChatMessageData(
            id = entity.id,
            role = ChatRole.valueOf(entity.role.uppercase()),
            content = entity.content,
            timestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(entity.createdAt)),
            metadata = null
        )
    }
}

/**
 * ChatRepository interface
 */
interface ChatRepository {
    suspend fun loadMessages(projectId: String): List<ChatMessageData>
    suspend fun saveMessage(projectId: String, message: ChatMessageData)
    suspend fun loadVersions(projectId: String): List<VersionEntry>
    suspend fun saveVersion(projectId: String, version: VersionEntry)
}