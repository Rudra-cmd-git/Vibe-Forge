package com.vibecoder.agent

import com.vibecoder.agent.loop.AgentLoop
import com.vibecoder.providers.LlmProvider
import com.vibecoder.providers.ProviderConfig
import com.vibecoder.providers.ProviderFactory
import com.vibecoder.providers.ToolCall
import com.vibecoder.providers.ToolParameter
import com.vibecoder.providers.ToolParameters
import com.vibecoder.providers.ToolSchema
import com.vibecoder.runtime.proot.ProotRuntime
import com.vibecoder.storage.prefs.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Agent implementation that wraps the agent module's AgentLoop
 */
@Singleton
class AgentImpl @Inject constructor(
    private val prootRuntime: ProotRuntime,
    private val projectRoot: Path,
    private val preferencesRepository: PreferencesRepository,
    private val providerFactory: ProviderFactory
) : Agent {

    private var agentLoop: AgentLoop? = null
    private var currentProjectId: String? = null

    override suspend fun createPlan(prompt: String, projectId: String): Plan {
        val loop = getOrCreateAgentLoop(projectId)
        val systemPrompt = buildSystemPrompt(projectId)
        val userPrompt = """User request: $prompt

Please create a step-by-step plan to accomplish this. Be specific about:
1. What files need to be created/modified
2. What commands need to be run
3. What the milestones are

Return as plain text with numbered steps."""

        val planText = withContext(Dispatchers.IO) {
            loop.provider.complete(systemPrompt, userPrompt, loop.registry.schemas())
        }

        return Plan(planText.lines()
            .filter { it.trim().matches("""\d+\..*""".toRegex()) }
            .map { it.trim().substringAfter(".").trim() }
        )
    }

    override suspend fun executePlan(plan: Plan, projectId: String): Result {
        val loop = getOrCreateAgentLoop(projectId)
        
        return withContext(Dispatchers.IO) {
            val result = loop.run(plan.steps.joinToString("\n"))
            Result(
                summary = result.summary,
                success = result.success,
                filesChanged = result.filesChanged,
                duration = result.duration
            )
        }
    }

    override suspend fun runTests(projectId: String) {
        val loop = getOrCreateAgentLoop(projectId)
        withContext(Dispatchers.IO) {
            // Run tests in the project
            loop.runtime.execute("npm test 2>/dev/null || python -m pytest 2>/dev/null || echo 'No test framework configured'", ".", 60000)
        }
    }

    private fun getOrCreateAgentLoop(projectId: String): AgentLoop {
        if (agentLoop == null || currentProjectId != projectId) {
            currentProjectId = projectId
            val projectPath = projectRoot.resolve(projectId)
            
            // Get provider from preferences
            val providerId = preferencesRepository.getSelectedProvider() ?: "anthropic"
            val apiKey = preferencesRepository.getApiKey() ?: ""
            val model = preferencesRepository.getSelectedModel()
            
            val config = ProviderConfig(
                apiKey = apiKey,
                defaultModel = model
            )
            val provider = providerFactory.create(providerId, config)
            
            agentLoop = AgentLoop(
                projectId = projectId,
                projectRoot = projectPath,
                runtime = prootRuntime,
                provider = ProviderAdapter(provider)
            )
        }
        return agentLoop!!
    }

    private fun buildSystemPrompt(projectId: String): String {
        return """You are an expert software engineer working in a mobile development environment.
You have access to a full Linux environment with Node, Python, Git, and all web development tools.

AVAILABLE TOOLS:
- bash: Execute bash commands
- read_file: Read file contents
- write_file: Write content to a file
- edit_file: Replace a string in a file
- list_dir: List directory contents
- search_code: Search for text across files
- web_fetch: Fetch content from a URL
- git_commit: Commit changes to git

INSTRUCTIONS:
- Read only the files you need
- Summarize old conversation context
- Cap tool output size
- Never load the whole codebase at once
- Plan first for big requests
- Run tests/build checks before saying done
- Keep responses concise and actionable"""
    }
}

/**
 * Adapter to convert providers.LlmProvider to agent.loop.LlmProvider
 */
private class ProviderAdapter(
    private val provider: com.vibecoder.providers.LlmProvider
) : com.vibecoder.agent.loop.LlmProvider {
    
    override suspend fun complete(systemPrompt: String, userPrompt: String, tools: List<com.vibecoder.agent.tools.ToolSchema>): String {
        val providerTools = tools.map { it.toProviderSchema() }
        val result = provider.complete(systemPrompt, userPrompt, providerTools)
        return result.content
    }

    override suspend fun getToolCalls(systemPrompt: String, userPrompt: String, tools: List<com.vibecoder.agent.tools.ToolSchema>): List<com.vibecoder.agent.tools.ToolCall> {
        val providerTools = tools.map { it.toProviderSchema() }
        return provider.getToolCalls(systemPrompt, userPrompt, providerTools)
            .map { it.toAgentToolCall() }
    }
}

private fun com.vibecoder.agent.tools.ToolSchema.toProviderSchema(): ToolSchema {
    return ToolSchema(
        name = name,
        description = description,
        parameters = ToolParameters(
            type = parameters.type,
            properties = parameters.properties.mapValues { (k, v) ->
                k to ToolParameter(v.type, v.description, v.enumValues)
            },
            required = parameters.required
        )
    )
}

private fun ToolCall.toAgentToolCall(): com.vibecoder.agent.tools.ToolCall {
    return com.vibecoder.agent.tools.ToolCall(
        id = id,
        name = name,
        arguments = arguments
    )
}

/**
 * Agent interface
 */
interface Agent {
    data class Plan(val steps: List<String>)
    data class Result(val summary: String, val success: Boolean, val filesChanged: Int, val duration: Long)

    suspend fun createPlan(prompt: String, projectId: String): Plan
    suspend fun executePlan(plan: Plan, projectId: String): Result
    suspend fun runTests(projectId: String)
}