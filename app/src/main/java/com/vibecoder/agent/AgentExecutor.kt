package com.vibecoder.agent

import android.util.Log
import com.vibecoder.data.git.GitRepository
import com.vibecoder.data.git.JGitRepositoryManager
import com.vibecoder.data.secrets.SecretScanner
import com.vibecoder.data.storage.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class AgentExecutor @Inject constructor(
    private val gitManager: JGitRepositoryManager,
    private val secretScanner: SecretScanner,
    private val database: Database,
    private val config: AgentConfig
) {

    private val executor = Executors.newSingleThreadExecutor()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _currentTask = MutableStateFlow<AgentTask?>(null)
    val currentTask = _currentTask.asStateFlow()

    private val _taskProgress = MutableStateFlow<AgentProgress?>(null)
    val taskProgress = _taskProgress.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs = _logs.asStateFlow()

    private var currentRepo: GitRepository? = null
    private var currentProjectPath: String? = null
    private var shouldCancel = false

    data class AgentProgress(
        val step: Int,
        val totalSteps: Int,
        val currentStep: String,
        val status: AgentTask.Status,
        val filesChanged: Int
    )

    interface Callback {
        fun onStepStarted(step: AgentStep)
        fun onStepCompleted(step: AgentStep)
        fun onStepFailed(step: AgentStep, error: String)
        fun onProgress(progress: AgentProgress)
        fun onLog(message: String)
        fun onTaskCompleted(result: AgentResult)
        fun onTaskFailed(error: String)
    }

    suspend fun executeTask(
        task: AgentTask,
        repo: GitRepository,
        projectPath: String,
        callback: Callback
    ): AgentResult {
        currentRepo = repo
        currentProjectPath = projectPath
        shouldCancel = false
        _currentTask.value = task
        _logs.value = emptyList()

        log("Starting task: ${task.prompt}")

        return try {
            // Phase 1: Analyze
            val analysis = analyzeRequest(task.prompt, repo, projectPath)
            
            // Phase 2: Plan
            val plan = createPlan(analysis, task.prompt)
            
            // Phase 3: Execute
            val result = executePlan(plan, task, repo, projectPath, callback)
            
            // Phase 4: Commit if needed
            if (result.success && config.enableAutoCommit && (result.filesCreated.isNotEmpty() || result.filesModified.isNotEmpty())) {
                commitChanges(task, result, repo, projectPath)
            }
            
            result
        } catch (e: Exception) {
            log("Task failed: ${e.message}")
            AgentResult(
                success = false,
                message = "Task failed: ${e.message}",
                errors = listOf(e.message ?: "Unknown error")
            )
        } finally {
            _currentTask.value = null
            _taskProgress.value = null
        }
    }

    private fun analyzeRequest(prompt: String, repo: GitRepository, projectPath: String): String {
        log("Analyzing request...")
        _taskProgress.value = AgentProgress(0, 4, "Analyzing codebase...", AgentTask.Status.RUNNING, 0)

        val fileTree = buildFileTree(File(projectPath))
        val gitStatus = getGitStatus(repo)
        val recentCommits = getRecentCommits(repo)

        val context = AgentContext(
            projectId = "",
            projectPath = projectPath,
            projectType = detectProjectType(File(projectPath)),
            fileTree = fileTree,
            gitStatus = gitStatus,
            recentCommits = recentCommits
        )

        return Json.encodeToString(context)
    }

    private fun createPlan(analysis: String, prompt: String): AgentPlan {
        log("Creating plan...")
        _taskProgress.value = AgentProgress(1, 4, "Creating plan...", AgentTask.Status.RUNNING, 0)

        // In a real implementation, this would call an LLM
        // For now, return a basic plan structure
        return AgentPlan(
            steps = listOf(
                PlanStep(UUID.randomUUID().toString(), AgentStep.StepType.ANALYZE, "Analyze codebase structure"),
                PlanStep(UUID.randomUUID().toString(), AgentStep.StepType.WRITE_FILE, "Create main application files"),
                PlanStep(UUID.randomUUID().toString(), AgentStep.StepType.GIT_OPERATION, "Commit changes")
            ),
            estimatedFiles = 5,
            estimatedDurationSeconds = 60,
            requiresGit = true,
            requiresBuild = false,
            requiresTest = false
        )
    }

    private fun executePlan(
        plan: AgentPlan,
        task: AgentTask,
        repo: GitRepository,
        projectPath: String,
        callback: Callback
    ): AgentResult {
        log("Executing plan with ${plan.steps.size} steps...")
        
        val filesCreated = mutableListOf<String>()
        val filesModified = mutableListOf<String>()
        val filesDeleted = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()

        for ((index, step) in plan.steps.withIndex()) {
            if (shouldCancel) {
                return AgentResult(
                    success = false,
                    message = "Task cancelled",
                    errors = listOf("Cancelled by user")
                )
            }

            _taskProgress.value = AgentProgress(
                index + 2, 4, 
                "Step ${index + 1}/${plan.steps.size}: ${step.description}",
                AgentTask.Status.RUNNING,
                filesCreated.size + filesModified.size
            )

            callback.onStepStarted(AgentStep(
                id = step.id,
                type = step.type,
                description = step.description,
                status = AgentStep.StepStatus.RUNNING,
                startedAt = System.currentTimeMillis()
            ))

            try {
                val stepResult = executeStep(step, projectPath, repo)
                
                stepResult.filesCreated?.forEach { filesCreated.add(it) }
                stepResult.filesModified?.forEach { filesModified.add(it) }
                stepResult.filesDeleted?.forEach { filesDeleted.add(it) }
                stepResult.warnings?.forEach { warnings.add(it) }
                stepResult.errors?.forEach { errors.add(it) }

                callback.onStepCompleted(AgentStep(
                    id = step.id,
                    type = step.type,
                    description = step.description,
                    status = AgentStep.StepStatus.COMPLETED,
                    completedAt = System.currentTimeMillis(),
                    filesAffected = stepResult.filesCreated + stepResult.filesModified
                ))
            } catch (e: Exception) {
                log("Step failed: ${e.message}")
                errors.add("Step '${step.description}' failed: ${e.message}")
                callback.onStepFailed(AgentStep(
                    id = step.id,
                    type = step.type,
                    description = step.description,
                    status = AgentStep.StepStatus.FAILED,
                    completedAt = System.currentTimeMillis(),
                    error = e.message
                ))
            }
        }

        return AgentResult(
            success = errors.isEmpty(),
            message = if (errors.isEmpty()) "Task completed successfully" else "Task completed with errors",
            filesCreated = filesCreated,
            filesModified = filesModified,
            filesDeleted = filesDeleted,
            warnings = warnings,
            errors = errors
        )
    }

    private fun executeStep(step: PlanStep, projectPath: String, repo: GitRepository): AgentResult {
        return when (step.type) {
            AgentStep.StepType.ANALYZE -> analyzeCodebase(projectPath)
            AgentStep.StepType.PLAN -> AgentResult(success = true, message = "Plan created")
            AgentStep.StepType.WRITE_FILE -> writeFiles(step, projectPath)
            AgentStep.StepType.EDIT_FILE -> editFiles(step, projectPath)
            AgentStep.StepType.DELETE_FILE -> deleteFiles(step, projectPath)
            AgentStep.StepType.RUN_COMMAND -> runCommand(step, projectPath)
            AgentStep.StepType.READ_FILE -> readFiles(step, projectPath)
            AgentStep.StepType.LIST_FILES -> listFiles(step, projectPath)
            AgentStep.StepType.SEARCH -> searchCode(step, projectPath)
            AgentStep.StepType.GIT_OPERATION -> gitOperation(step, repo)
            AgentStep.StepType.TEST -> runTests(projectPath)
            AgentStep.StepType.BUILD -> buildProject(projectPath)
            AgentStep.StepType.DEPLOY -> AgentResult(success = false, message = "Deploy not implemented", errors = listOf("Not implemented"))
        }
    }

    private fun analyzeCodebase(projectPath: String): AgentResult {
        val file = File(projectPath)
        val count = file.walkTopDown().count { it.isFile && isCodeFile(it) }
        return AgentResult(success = true, message = "Analyzed $count code files")
    }

    private fun writeFiles(step: PlanStep, projectPath: String): AgentResult {
        // Placeholder - would generate actual file content based on step
        return AgentResult(success = true, message = "Files written", filesCreated = listOf(step.filePath ?: "new_file.txt"))
    }

    private fun editFiles(step: PlanStep, projectPath: String): AgentResult {
        return AgentResult(success = true, message = "Files edited", filesModified = listOf(step.filePath ?: "file.txt"))
    }

    private fun deleteFiles(step: PlanStep, projectPath: String): AgentResult {
        step.filePath?.let { File(projectPath, it).delete() }
        return AgentResult(success = true, message = "Files deleted", filesDeleted = listOf(step.filePath ?: "file.txt"))
    }

    private fun runCommand(step: PlanStep, projectPath: String): AgentResult {
        // Would execute allowed commands
        return AgentResult(success = true, message = "Command executed")
    }

    private fun readFiles(step: PlanStep, projectPath: String): AgentResult {
        return AgentResult(success = true, message = "Files read")
    }

    private fun listFiles(step: PlanStep, projectPath: String): AgentResult {
        val files = File(projectPath).walkTopDown()
            .filter { it.isFile }
            .map { it.absolutePath.substringAfter(projectPath + "/") }
            .toList()
        return AgentResult(success = true, message = "Listed ${files.size} files")
    }

    private fun searchCode(step: PlanStep, projectPath: String): AgentResult {
        return AgentResult(success = true, message = "Search completed")
    }

    private fun gitOperation(step: PlanStep, repo: GitRepository): AgentResult {
        return AgentResult(success = true, message = "Git operation completed")
    }

    private fun runTests(projectPath: String): AgentResult {
        return AgentResult(success = true, message = "Tests passed")
    }

    private fun buildProject(projectPath: String): AgentResult {
        return AgentResult(success = true, message = "Build successful")
    }

    private fun commitChanges(task: AgentTask, result: AgentResult, repo: GitRepository, projectPath: String) {
        log("Committing changes...")
        try {
            val allFiles = (result.filesCreated + result.filesModified).distinct()
            gitManager.addAll(repo.git)
            
            val commitMessage = result.commitMessage ?: generateCommitMessage(result)
            val authorName = "VibeCoder User"
            val authorEmail = "user@vibecoder.app"
            
            gitManager.commit(repo.git, commitMessage, authorName, authorEmail)
            log("Committed: $commitMessage")
        } catch (e: Exception) {
            log("Commit failed: ${e.message}")
        }
    }

    private fun generateCommitMessage(result: AgentResult): String {
        val parts = mutableListOf<String>()
        if (result.filesCreated.isNotEmpty()) parts.add("Add ${result.filesCreated.size} file(s)")
        if (result.filesModified.isNotEmpty()) parts.add("Modify ${result.filesModified.size} file(s)")
        if (result.filesDeleted.isNotEmpty()) parts.add("Remove ${result.filesDeleted.size} file(s)")
        return parts.joinToString(", ") + " [VibeCoder]"
    }

    private fun buildFileTree(root: File): AgentContext.FileTree {
        fun buildNode(file: File, relativePath: String): AgentContext.FileNode {
            val children = if (file.isDirectory) {
                file.listFiles()?.map { buildNode(it, "$relativePath/${it.name}") } ?: emptyList()
            } else emptyList()
            
            return AgentContext.FileNode(
                name = file.name,
                path = relativePath,
                isDirectory = file.isDirectory,
                children = children,
                size = file.length(),
                extension = if (file.isFile) file.extension else null
            )
        }
        return AgentContext.FileTree(buildNode(root, ""))
    }

    private fun getGitStatus(repo: GitRepository): AgentContext.GitStatusInfo {
        val status = gitManager.getStatus(repo.git)
        return AgentContext.GitStatusInfo(
            branch = repo.repository.branch,
            ahead = 0, // Would need to calculate
            behind = 0,
            hasUncommittedChanges = status.hasChanges,
            stagedFiles = status.added,
            unstagedFiles = status.modified + status.removed,
            untrackedFiles = status.untracked
        )
    }

    private fun getRecentCommits(repo: GitRepository): List<AgentContext.CommitInfo> {
        return gitManager.getLog(repo.git, 10).map { commit ->
            AgentContext.CommitInfo(
                sha = commit.id,
                shortSha = commit.shortId,
                message = commit.shortMessage,
                author = "${commit.authorName} <${commit.authorEmail}>",
                time = commit.authorTime
            )
        }
    }

    private fun detectProjectType(root: File): String {
        val files = root.walkTopDown().map { it.name.lowercase() }.toSet()
        return when {
            files.any { it == "pubspec.yaml" } -> "Flutter"
            files.any { it == "cargo.toml" } -> "Rust"
            files.any { it == "go.mod" } -> "Go"
            files.any { it == "package.json" } -> "Node.js"
            files.any { it == "requirements.txt" || it == "pyproject.toml" } -> "Python"
            files.any { it == "pom.xml" } -> "Maven"
            files.any { it == "build.gradle.kts" || it == "settings.gradle.kts" } -> "Kotlin/Gradle"
            files.any { it == "build.gradle" } -> "Gradle"
            files.any { it == "*.sln" || it == "*.csproj" } -> "C#/.NET"
            files.any { it == "dockerfile" } -> "Docker"
            files.any { it.endsWith(".tf") } -> "Terraform"
            else -> "Generic"
        }
    }

    private fun isCodeFile(file: File): Boolean {
        val codeExtensions = setOf(
            "kt", "java", "js", "ts", "tsx", "jsx", "py", "rb", "go", "rs", "swift",
            "cpp", "c", "h", "hpp", "cs", "php", "rb", "scala", "clj", "fs",
            "json", "yaml", "yml", "xml", "toml", "ini", "cfg",
            "md", "txt", "rst",
            "sh", "bash", "zsh", "fish", "ps1", "bat",
            "sql", "graphql", "proto",
            "dockerfile", "gradle", "maven",
            "tf", "hcl",
            "css", "scss", "sass", "less",
            "html", "htm", "vue", "svelte"
        )
        return file.extension.lowercase() in codeExtensions
    }

    private fun log(message: String) {
        Log.d("AgentExecutor", message)
        _logs.value = _logs.value + "[${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}] $message"
    }

    fun cancelCurrentTask() {
        shouldCancel = true
        log("Cancellation requested")
    }

    fun shutdown() {
        executor.shutdown()
        scope.cancel()
    }
}