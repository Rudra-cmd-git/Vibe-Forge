package com.vibecoder.providers

import com.vibecoder.agent.tools.ToolCall
import com.vibecoder.agent.tools.ToolSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Base provider interface
 */
interface LlmProvider {
    val id: String
    val name: String
    val requiresApiKey: Boolean
    val supportsStreaming: Boolean
    val supportsTools: Boolean
    val supportsVision: Boolean

    suspend fun complete(
        systemPrompt: String,
        userPrompt: String,
        tools: List<ToolSchema>
    ): ProviderResult

    suspend fun getToolCalls(
        systemPrompt: String,
        userPrompt: String,
        tools: List<ToolSchema>
    ): List<ToolCall>

    suspend fun getModels(): List<ModelInfo>

    suspend fun validateApiKey(apiKey: String): Boolean
}

/**
 * Provider result
 */
@Serializable
data class ProviderResult(
    val content: String,
    val toolCalls: List<ToolCall> = emptyList(),
    val usage: Usage? = null,
    val model: String? = null,
    val finishReason: String? = null
)

@Serializable
data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

/**
 * Model information
 */
@Serializable
data class ModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val contextWindow: Int,
    val pricing: Pricing,
    val capabilities: ModelCapabilities
)

@Serializable
data class Pricing(
    val inputPerMillion: Double,
    val outputPerMillion: Double
)

@Serializable
data class ModelCapabilities(
    val tools: Boolean,
    val vision: Boolean,
    val streaming: Boolean
)

/**
 * Provider configuration
 */
@Serializable
data class ProviderConfig(
    val apiKey: String,
    val baseUrl: String? = null,
    val defaultModel: String? = null,
    val customHeaders: Map<String, String> = emptyMap()
)

/**
 * Provider factory
 */
class ProviderFactory {
    private val providers = mapOf(
        "anthropic" to AnthropicProvider::class,
        "openrouter" to OpenRouterProvider::class,
        "deepseek" to DeepSeekProvider::class,
        "custom" to CustomProvider::class
    )

    fun create(id: String, config: ProviderConfig): LlmProvider {
        val clazz = providers[id] ?: throw IllegalArgumentException("Unknown provider: $id")
        return clazz.getDeclaredConstructor(ProviderConfig::class.java).newInstance(config)
    }

    fun availableProviders(): List<ProviderMetadata> = listOf(
        ProviderMetadata("anthropic", "Anthropic", "Claude models - best for coding", true, true, true),
        ProviderMetadata("openrouter", "OpenRouter", "Access 100+ models (GPT, Llama, etc.)", true, true, true),
        ProviderMetadata("deepseek", "DeepSeek", "Fast, affordable coding models", true, true, false),
        ProviderMetadata("custom", "Custom Endpoint", "OpenAI-compatible API", true, true, true)
    )
}

@Serializable
data class ProviderMetadata(
    val id: String,
    val name: String,
    val description: String,
    val requiresApiKey: Boolean,
    val supportsTools: Boolean,
    val supportsVision: Boolean
)

/**
 * Tool schema for LLM function calling (provider version)
 */
@Serializable
data class ToolSchema(
    val name: String,
    val description: String,
    val parameters: ToolParameters
)

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
    val enumValues: List<String>? = null
)

/**
 * Tool call from LLM (provider version)
 */
@Serializable
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: Map<String, Any>
)