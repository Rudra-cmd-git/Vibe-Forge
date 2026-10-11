package com.vibecoder.agent.loop

import com.vibecoder.agent.memory.ProjectMemory
import com.vibecoder.agent.tools.Tool
import com.vibecoder.agent.tools.ToolCall
import com.vibecoder.agent.tools.ToolExecutor
import com.vibecoder.agent.tools.ToolFactory
import com.vibecoder.agent.tools.ToolRegistry
import com.vibecoder.agent.tools.ToolResult
import com.vibecoder.agent.tools.ToolSchema
import com.vibecoder.runtime.proot.ProotRuntime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.Path

/**
 * Main agent loop - sends messages + tool schemas, executes tool calls, returns results
 */
class AgentLoop(
    private val projectId: String,
    private val projectRoot: Path,
    private val runtime: ProotRuntime,
    private val provider: LlmProvider
) {
    private val memory = ProjectMemory(projectRoot)
    private val toolFactory = ToolFactory(runtime, projectRoot)
    private val registry = ToolRegistry().apply { toolFactory.createAll().forEach(::register) }
    private val executor = ToolExecutor(registry, SafetyChecker())

    private val _state = MutableStateFlow(AgentState.Idle)
    val state = _state.asStateFlow()

    private val _currentPlan = MutableStateFlow<String?>(null)
    val currentPlan = _currentPlan.asStateFlow()

    private val _iterations = 0
    private const val MAX_ITERATIONS = 20

    /**
     * Run the agent with a user prompt
     */
    suspend fun run(prompt: String): AgentResult {
        _state.value = AgentState.Planning
        _iterations = 0

        // 1. Read project memory
        val projectContext = memory.readProjectMd()
        val todoContext = memory.readTodoMd()

        // 2. Create plan for big requests
        val plan = createPlan(prompt, projectContext, todoContext)
        _currentPlan.value = plan

        // 3. Execute plan in milestones
        return executePlan(plan, prompt)
    }

    /**
     * Create a plan for the request
     */
    private suspend fun createPlan(prompt: String, projectContext: String, todoContext: String): String {
        val systemPrompt = buildSystemPrompt(projectContext, todoContext)
        val userPrompt = """User request: $prompt

Please create a step-by-step plan to accomplish this. Be specific about:
1. What files need to be created/modified
2. What commands need to be run
3. What the milestones are

Return as plain text with numbered steps."""

        return provider.complete(systemPrompt, userPrompt, registry.schemas())
    }

    /**
     * Execute the plan step by step
     */
    private suspend fun executePlan(plan: String, originalPrompt: String): AgentResult {
        _state.value = AgentState.Executing

        val steps = parsePlan(plan)
        val results = mutableListOf<StepResult>()
        var filesChanged = 0
        var success = true

        for (step in steps) {
            if (_iterations >= MAX_ITERATIONS) {
                success = false
                break
            }

            _state.value = AgentState.Executing
            _iterations++

            // Execute step
            val stepResult = executeStep(step, originalPrompt)
            results.add(stepResult)
            filesChanged += stepResult.filesChanged

            // Update TODO.md
            memory.completeTask(results.size - 1)

            // Self-healing: if step failed, try to fix
            if (!stepResult.success) {
                val fixed = tryFixStep(step, stepResult.error!!)
                if (!fixed) {
                    success = false
                    break
                }
            }

            // Auto-commit after successful step
            if (stepResult.success) {
                commitChanges("Agent: $step")
            }
        }

        _state.value = AgentState.Completed
        _currentPlan.value = null

        return AgentResult(
            success = success,
            summary = if (success) "Completed: $plan" else "Failed at step ${results.size}: ${results.last().error}",
            filesChanged = filesChanged,
            duration = 0 // TODO: track actual duration
        )
    }

    /**
     * Execute a single step
     */
    private suspend fun executeStep(step: String, originalPrompt: String): StepResult {
        _state.value = AgentState.Executing

        val systemPrompt = buildSystemPrompt(memory.readProjectMd(), memory.readTodoMd())
        val userPrompt = """Current step: $step
Original request: $originalPrompt

Execute this step using the available tools. Return the tool calls needed."""

        // Get tool calls from LLM
        val toolCalls = provider.getToolCalls(systemPrompt, userPrompt, registry.schemas())

        var stepSuccess = true
        var stepError: String? = null
        var totalFilesChanged = 0

        for (toolCall in toolCalls) {
            val result = executor.execute(toolCall)
            if (!result.success) {
                stepSuccess = false
                stepError = result.error
                break
            }
            totalFilesChanged += result.files.size
        }

        return StepResult(stepSuccess, stepError, totalFilesChanged)
    }

    /**
     * Try to fix a failed step
     */
    private suspend fun tryFixStep(step: String, error: String): Boolean {
        _state.value = AgentState.Fixing

        val systemPrompt = buildSystemPrompt(memory.readProjectMd(), memory.readTodoMd())
        val userPrompt = """Step failed: $step
Error: $error

Analyze the error and provide tool calls to fix it."""

        val toolCalls = provider.getToolCalls(systemPrompt, userPrompt, registry.schemas())

        for (toolCall in toolCalls) {
            val result = executor.execute(toolCall)
            if (!result.success) return false
        }

        return true
    }

    /**
     * Auto-commit changes
     */
    private fun commitChanges(message: String) {
        try {
            runtime.execute("git add -A && git commit -m \"$message\"", ".", 30000)
        } catch (e: Exception) {
            // Ignore commit failures
        }
    }

    private fun buildSystemPrompt(projectContext: String, todoContext: String): String {
        return """You are an expert software engineer working in a mobile development environment.
You have access to a full Linux environment with Node, Python, Git, and all web development tools.

PROJECT CONTEXT:
$projectContext

CURRENT TODO:
$todoContext

AVAILABLE TOOLS:
${registry.all().map { "- ${it.name}: ${it.description}" }.joinToString("\n")}

INSTRUCTIONS:
- Read only the files you need
- Summarize old conversation context
- Cap tool output size
- Never load the whole codebase at once
- Plan first for big requests
- Run tests/build checks before saying done
- Keep responses concise and actionable"""
    }

    private fun parsePlan(plan: String): List<String> {
        return plan.lines()
            .filter { it.trim().matches("""\d+\..*""".toRegex()) }
            .map { it.trim().substringAfter(".").trim() }
    }

    enum class AgentState { Idle, Planning, Executing, Fixing, Completed }

    data class AgentResult(
        val success: Boolean,
        val summary: String,
        val filesChanged: Int,
        val duration: Long
    )

    data class StepResult(
        val success: Boolean,
        val error: String?,
        val filesChanged: Int
    )
}

/**
 * LLM Provider interface
 */
interface LlmProvider {
    fun complete(systemPrompt: String, userPrompt: String, tools: List<ToolSchema>): String
    fun getToolCalls(systemPrompt: String, userPrompt: String, tools: List<ToolSchema>): List<ToolCall>
}