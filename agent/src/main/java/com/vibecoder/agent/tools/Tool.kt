package com.vibecoder.agent.tools

import com.vibecoder.design.components.ToolAction
import com.vibecoder.design.components.ToolActionType
import com.vibecoder.design.components.ToolFile
import com.vibecoder.design.components.FileAction
import com.vibecoder.design.components.ToolStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Base tool interface - all tools implement this
 */
interface Tool {
    val name: String
    val description: String
    val parameters: ToolParameters
    val type: ToolActionType

    suspend fun execute(args: Map<String, Any>): ToolResult
}

/**
 * Tool parameters schema for LLM function calling
 */
@Serializable
data class ToolParameters(
    val type: String = "object",
    val properties: Map<String, ToolParameter> = emptyMap(),
    val required: List<String> = emptyList()
)

@Serializable
data class ToolParameter(
    val type: String,
    val description: String,
    val enum: List<String>? = null
)

/**
 * Tool execution result
 */
@Serializable
data class ToolResult(
    val success: Boolean,
    val output: String,
    val files: List<ToolFile> = emptyList(),
    val error: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun success(output: String, files: List<ToolFile> = emptyList()) = ToolResult(true, output, files)
        fun error(error: String) = ToolResult(false, "", error = error)
    }
}

/**
 * Tool call from LLM
 */
@Serializable
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: Map<String, Any>
)

/**
 * Tool registry - manages all available tools
 */
class ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()

    fun register(tool: Tool) {
        tools[tool.name] = tool
    }

    fun get(name: String): Tool? = tools[name]

    fun all(): List<Tool> = tools.values.toList()

    fun schemas(): List<ToolSchema> = tools.values.map { it.toSchema() }
}

/**
 * Tool schema for LLM function calling
 */
@Serializable
data class ToolSchema(
    val name: String,
    val description: String,
    val parameters: ToolParameters
)

fun Tool.toSchema(): ToolSchema = ToolSchema(name, description, parameters)

/**
 * Tool executor - handles tool execution with safety checks
 */
class ToolExecutor(
    private val registry: ToolRegistry,
    private val safetyChecker: SafetyChecker
) {
    private val _actionState = MutableStateFlow<ToolAction?>(null)
    val actionState = _actionState.asStateFlow()

    suspend fun execute(toolCall: ToolCall): ToolResult {
        val tool = registry.get(toolCall.name)
            ?: return ToolResult.error("Unknown tool: ${toolCall.name}")

        // Create action for UI tracking
        val action = ToolAction(
            type = tool.type,
            summary = toolCall.name,
            detail = toolCall.arguments.toString(),
            status = ToolStatus.Running
        )
        _actionState.value = action

        // Safety check
        val safetyResult = safetyChecker.check(toolCall)
        if (!safetyResult.allowed) {
            val result = ToolResult.error(safetyResult.reason ?: "Blocked by safety checker")
            _actionState.value = action.copy(status = ToolStatus.Failed, output = result.error)
            return result
        }

        // Execute with timeout
        return try {
            val startTime = System.currentTimeMillis()
            val result = tool.execute(toolCall.arguments)
            val duration = System.currentTimeMillis() - startTime

            _actionState.value = action.copy(
                status = if (result.success) ToolStatus.Completed else ToolStatus.Failed,
                output = result.output,
                files = result.files,
                duration = duration
            )
            result
        } catch (e: Exception) {
            val result = ToolResult.error(e.message ?: "Execution failed")
            _actionState.value = action.copy(status = ToolStatus.Failed, output = result.error)
            result
        }
    }
}

/**
 * Safety checker - prevents dangerous operations
 */
class SafetyChecker {
    private val dangerousCommands = setOf("rm -rf", "format", "mkfs", "dd", "shutdown", "reboot")
    private val dangerousPaths = setOf("/system", "/vendor", "/boot", "/etc/passwd", "/etc/shadow")

    data class SafetyResult(val allowed: Boolean, val reason: String? = null)

    fun check(toolCall: ToolCall): SafetyResult {
        return when (toolCall.name) {
            "bash" -> checkBash(toolCall.arguments["command"] as? String ?: "")
            "write_file", "edit_file" -> checkWrite(toolCall.arguments["path"] as? String ?: "")
            "delete_file" -> checkDelete(toolCall.arguments["path"] as? String ?: "")
            "install_package" -> checkPackageInstall(toolCall.arguments["package"] as? String ?: "")
            "network_request" -> checkNetwork(toolCall.arguments["url"] as? String ?: "")
            else -> SafetyResult(true)
        }
    }

    private fun checkBash(command: String): SafetyResult {
        val lower = command.lowercase()
        dangerousCommands.forEach { dangerous ->
            if (lower.contains(dangerous)) {
                return SafetyResult(false, "Potentially dangerous command: $dangerous")
            }
        }
        return SafetyResult(true)
    }

    private fun checkWrite(path: String): SafetyResult {
        dangerousPaths.forEach { dangerous ->
            if (path.startsWith(dangerous)) {
                return SafetyResult(false, "Cannot write to protected path: $dangerous")
            }
        }
        return SafetyResult(true)
    }

    private fun checkDelete(path: String): SafetyResult {
        dangerousPaths.forEach { dangerous ->
            if (path.startsWith(dangerous)) {
                return SafetyResult(false, "Cannot delete protected path: $dangerous")
            }
        }
        return SafetyResult(true)
    }

    private fun checkPackageInstall(packageName: String): SafetyResult {
        // In real implementation, check against allowlist
        return SafetyResult(true)
    }

    private fun checkNetwork(url: String): SafetyResult {
        // In real implementation, check against allowlist
        return SafetyResult(true)
    }
}