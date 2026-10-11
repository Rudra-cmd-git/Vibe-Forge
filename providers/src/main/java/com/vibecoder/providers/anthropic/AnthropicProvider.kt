package com.vibecoder.providers.anthropic

import com.vibecoder.agent.tools.ToolCall
import com.vibecoder.agent.tools.ToolSchema
import com.vibecoder.providers.LlmProvider
import com.vibecoder.providers.ModelInfo
import com.vibecoder.providers.ProviderConfig
import com.vibecoder.providers.ProviderResult
import com.vibecoder.providers.Usage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString

/**
 * Anthropic (Claude) Provider
 */
class AnthropicProvider(
    private val config: ProviderConfig
) : LlmProvider {

    private val client = OkHttpClient.Builder().build()
    private val baseUrl = config.baseUrl ?: "https://api.anthropic.com"
    private val apiKey = config.apiKey
    private val defaultModel = config.defaultModel ?: "claude-3-5-sonnet-20241022"

    override val id = "anthropic"
    override val name = "Anthropic"
    override val requiresApiKey = true
    override val supportsStreaming = true
    override val supportsTools = true
    override val supportsVision = true

    override suspend fun complete(
        systemPrompt: String,
        userPrompt: String,
        tools: List<ToolSchema>
    ): ProviderResult {
        return withContext(Dispatchers.IO) {
            val messages = buildMessages(systemPrompt, userPrompt)
            val request = buildRequest(messages, tools, stream = false)
            val response = client.newCall(request).execute()
            parseResponse(response)
        }
    }

    override suspend fun getToolCalls(
        systemPrompt: String,
        userPrompt: String,
        tools: List<ToolSchema>
    ): List<ToolCall> {
        val result = complete(systemPrompt, userPrompt, tools)
        return result.toolCalls
    }

    override suspend fun getModels(): List<ModelInfo> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$baseUrl/v1/models")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return emptyList()

                val body = response.body?.string() ?: return emptyList()
                val modelsResponse = kotlinx.serialization.json.Json.decodeFromString<ModelsResponse>(body)
                modelsResponse.data.map { model ->
                    ModelInfo(
                        id = model.id,
                        name = model.displayName,
                        description = "Anthropic ${model.id}",
                        contextWindow = 200000,
                        pricing = Pricing(3.0, 15.0), // Default for Sonnet
                        capabilities = ModelCapabilities(true, true, true)
                    )
                }
            } catch (e: Exception) {
                // Return defaults
                defaultModels()
            }
        }
    }

    override suspend fun validateApiKey(apiKey: String): Boolean {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$baseUrl/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .post("""{"model":"claude-3-haiku-20240307","max_tokens":1,"messages":[{"role":"user","content":"test"}]}""".toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun buildMessages(systemPrompt: String, userPrompt: String): List<Message> {
        return listOf(
            Message("user", userPrompt)
        )
    }

    private fun buildRequest(
        messages: List<Message>,
        tools: List<ToolSchema>,
        stream: Boolean
    ): Request {
        val toolList = tools.map { schema ->
            ToolDefinition(
                name = schema.name,
                description = schema.description,
                input_schema = schema.parameters
            )
        }

        val requestBody = CompletionRequest(
            model = defaultModel,
            max_tokens = 4096,
            system = systemPrompt,
            messages = messages,
            tools = if (toolList.isNotEmpty()) toolList else null,
            tool_choice = if (toolList.isNotEmpty()) ToolChoice.Auto else null,
            stream = stream
        )

        val json = kotlinx.serialization.json.Json.encodeToString(requestBody)

        return Request.Builder()
            .url("$baseUrl/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("content-type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
    }

    private fun parseResponse(response: okhttp3.Response): ProviderResult {
        val body = response.body?.string() ?: return ProviderResult("Empty response")
        if (!response.isSuccessful) {
            return ProviderResult("API Error: ${response.code()} - $body")
        }

        val completionResponse = kotlinx.serialization.json.Json.decodeFromString<CompletionResponse>(body)

        val toolCalls = completionResponse.content
            .filter { it.type == "tool_use" }
            .map { content ->
                ToolCall(
                    id = content.id!!,
                    name = content.name!!,
                    arguments = content.input?.let { kotlinx.serialization.json.Json.decodeFromString<Map<String, Any>>(kotlinx.serialization.json.Json.encodeToString(it)) } ?: emptyMap()
                )
            }

        ProviderResult(
            content = completionResponse.content
                .filter { it.type == "text" }
                .map { it.text!! }
                .joinToString(""),
            toolCalls = toolCalls,
            usage = completionResponse.usage?.let { Usage(it.input_tokens, it.output_tokens, it.input_tokens + it.output_tokens) },
            model = completionResponse.model,
            finishReason = completionResponse.stop_reason
        )
    }

    private fun defaultModels(): List<ModelInfo> = listOf(
        ModelInfo("claude-3-5-sonnet-20241022", "Claude 3.5 Sonnet", "Best for complex coding tasks", 200000, Pricing(3.0, 15.0), ModelCapabilities(true, true, true)),
        ModelInfo("claude-3-5-haiku-20241022", "Claude 3.5 Haiku", "Fast, affordable coding", 200000, Pricing(1.0, 5.0), ModelCapabilities(true, true, true)),
        ModelInfo("claude-3-opus-20240229", "Claude 3 Opus", "Most capable model", 200000, Pricing(15.0, 75.0), ModelCapabilities(true, true, true))
    )

    // Data classes for Anthropic API
    @Serializable data class Message(val role: String, val content: String)
    @Serializable data class ToolDefinition(val name: String, val description: String, val input_schema: ToolSchema)
    @Serializable data class ToolChoice(val type: String = "auto") { companion object { val Auto = ToolChoice("auto") } }
    @Serializable data class CompletionRequest(
        val model: String,
        val max_tokens: Int,
        val system: String,
        val messages: List<Message>,
        val tools: List<ToolDefinition>? = null,
        val tool_choice: ToolChoice? = null,
        val stream: Boolean = false
    )
    @Serializable data class CompletionResponse(
        val id: String,
        val type: String,
        val role: String,
        val content: List<ContentBlock>,
        val model: String,
        val stop_reason: String?,
        val stop_sequence: String?,
        val usage: UsageInfo?
    )
    @Serializable data class ContentBlock(
        val type: String,
        val text: String? = null,
        val id: String? = null,
        val name: String? = null,
        val input: Any? = null
    )
    @Serializable data class UsageInfo(val input_tokens: Int, val output_tokens: Int)
    @Serializable data class ModelsResponse(val data: List<ModelData>)
    @Serializable data class ModelData(val id: String, val display_name: String, val type: String)
}