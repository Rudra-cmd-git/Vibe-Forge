package com.vibecoder.agent

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AgentTask(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val sessionId: String?,
    val prompt: String,
    val status: Status = Status.PENDING,
    val result: String? = null,
    val error: String? = null,
    val filesChanged: List<String> = emptyList(),
    val commitSha: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val steps: List<AgentStep> = emptyList()
) {
    enum class Status {
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    }
}

@Serializable
data class AgentStep(
    val id: String = UUID.randomUUID().toString(),
    val type: StepType,
    val description: String,
    val input: String? = null,
    val output: String? = null,
    val status: StepStatus = StepStatus.PENDING,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val error: String? = null,
    val filesAffected: List<String> = emptyList()
) {
    enum class StepType {
        ANALYZE,        // Analyze the request and codebase
        PLAN,           // Create a plan
        WRITE_FILE,     // Write a new file
        EDIT_FILE,      // Edit an existing file
        DELETE_FILE,    // Delete a file
        RUN_COMMAND,    // Run a shell command
        READ_FILE,      // Read a file
        LIST_FILES,     // List directory contents
        SEARCH,         // Search codebase
        GIT_OPERATION,  // Git commit, push, etc.
        TEST,           // Run tests
        BUILD,          // Build project
        DEPLOY          // Deploy
    }

    enum class StepStatus {
        PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
    }
}

@Serializable
data class AgentContext(
    val projectId: String,
    val projectPath: String,
    val projectType: String,
    val currentFiles: Map<String, String> = emptyMap(), // path -> content (for small files)
    val fileTree: FileTree? = null,
    val gitStatus: GitStatusInfo? = null,
    val recentCommits: List<CommitInfo> = emptyList(),
    val openFiles: List<String> = emptyList(),
    val cursorPosition: CursorPosition? = null
)

@Serializable
data class FileTree(
    val root: FileNode
)

@Serializable
data class FileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val children: List<FileNode> = emptyList(),
    val size: Long = 0,
    val extension: String? = null
)

@Serializable
data class GitStatusInfo(
    val branch: String,
    val ahead: Int,
    val behind: Int,
    val hasUncommittedChanges: Boolean,
    val stagedFiles: List<String>,
    val unstagedFiles: List<String>,
    val untrackedFiles: List<String>
)

@Serializable
data class CommitInfo(
    val sha: String,
    val shortSha: String,
    val message: String,
    val author: String,
    val time: Long
)

@Serializable
data class CursorPosition(
    val file: String,
    val line: Int,
    val column: Int
)

@Serializable
data class AgentPlan(
    val steps: List<PlanStep>,
    val estimatedFiles: Int,
    val estimatedDurationSeconds: Int,
    val requiresGit: Boolean,
    val requiresBuild: Boolean,
    val requiresTest: Boolean
)

@Serializable
data class PlanStep(
    val id: String,
    val type: AgentStep.StepType,
    val description: String,
    val filePath: String?,
    val dependsOn: List<String> = emptyList()
)

@Serializable
data class AgentResult(
    val success: Boolean,
    val message: String,
    val filesCreated: List<String> = emptyList(),
    val filesModified: List<String> = emptyList(),
    val filesDeleted: List<String> = emptyList(),
    val commitMessage: String? = null,
    val nextSteps: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)

@Serializable
data class FileOperation(
    val type: OperationType,
    val path: String,
    val content: String? = null,
    val oldContent: String? = null,
    val newContent: String? = null
) {
    enum class OperationType {
        CREATE, UPDATE, DELETE, MOVE, COPY
    }
}

@Serializable
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: String // JSON
)

@Serializable
data class ToolResult(
    val callId: String,
    val result: String?,
    val error: String?
)

@Serializable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
    val toolCalls: List<ToolCall> = emptyList(),
    val toolResults: List<ToolResult> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val tokenCount: Int = 0
) {
    enum class Role {
        SYSTEM, USER, ASSISTANT, TOOL
    }
}

@Serializable
data class AgentConfig(
    val model: String = "claude-3-5-sonnet-20241022",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 8192,
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val maxSteps: Int = 50,
    val maxFileSize: Long = 100_000, // 100KB
    val maxContextFiles: Int = 20,
    val enableAutoCommit: Boolean = true,
    val enableAutoPush: Boolean = false,
    val allowedCommands: List<String> = listOf(
        "ls", "cat", "head", "tail", "grep", "find", "wc",
        "npm", "yarn", "pnpm", "gradle", "mvn", "cargo", "go",
        "python", "python3", "pip", "pip3",
        "git", "docker", "kubectl"
    ),
    val blockedPaths: List<String> = listOf(
        ".git/", "node_modules/", "build/", "dist/", "target/",
        ".gradle/", ".idea/", ".vscode/", "*.class", "*.jar"
    )
)

companion object {
    const val DEFAULT_SYSTEM_PROMPT = """
        You are VibeCoder, an AI coding agent running directly on an Android device.
        You help users build software projects by writing code, creating files, and managing git repositories.
        
        CORE PRINCIPLES:
        1. Write clean, maintainable, production-ready code
        2. Follow best practices for the detected language/framework
        3. Use the tools provided to read, write, and modify files
        4. Commit changes with meaningful messages after logical units of work
        5. Never expose secrets, API keys, or credentials in code
        6. Generate appropriate .gitignore files for the project type
        7. Handle errors gracefully and provide clear feedback
        
        WORKFLOW:
        1. ANALYZE - Understand the user's request and explore the codebase
        2. PLAN - Create a step-by-step plan
        3. EXECUTE - Implement the plan using tools
        4. VERIFY - Test and build if possible
        5. COMMIT - Commit changes with a descriptive message
        
        TOOLS AVAILABLE:
        - read_file: Read file contents
        - write_file: Create or overwrite a file
        - edit_file: Modify an existing file (use sparingly, prefer write_file)
        - delete_file: Delete a file
        - list_files: List directory contents
        - search_code: Search for patterns in codebase
        - run_command: Execute shell commands (limited to allowed list)
        - git_commit: Commit changes
        - git_push: Push to remote
        - git_status: Check git status
        
        OUTPUT FORMAT:
        Respond with a JSON object containing:
        {
          "thinking": "Your reasoning",
          "plan": [...], // Only in PLAN phase
          "actions": [...], // Tool calls to execute
          "response": "User-facing response"
        }
    """.trimIndent()
}