package com.vibecoder.providers.custom

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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Custom Provider - OpenAI-compatible endpoints (local, proxy, etc.)
 */
class CustomProvider(
    private val config: ProviderConfig
) : LlmProvider {

    private val client = OkHttpClient.Builder().build()
    private val baseUrl = config.baseUrl ?: throw IllegalArgumentException("Custom provider requires baseUrl")
    private val apiKey = config.apiKey
    private val defaultModel = config.defaultModel ?: "default"
    private val customHeaders = config.customHeaders

    override val id = "custom"
    override val name = "Custom Endpoint"
    override val requiresApiKey = true
    override val supportsStreaming = true
    override val supportsTools = true
    override val supportsVision = false

    override suspend fun complete(
        systemPrompt: String,
        userPrompt: String,
        tools: List<ToolSchema>
    ): ProviderResult {
        return withContext(Dispatchers.IO) {
            val messages = listOf(
                ChatMessage("system", systemPrompt),
                ChatMessage("user", userPrompt)
            )
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
                .url("$baseUrl/models")
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return defaultModels()

                val body = response.body?.string() ?: return defaultModels()
                val modelsResponse = kotlinx.serialization.json.Json.decodeFromString<ModelsResponse>(body)
                modelsResponse.data.map { model ->
                    ModelInfo(
                        id = model.id,
                        name = model.id,
                        description = "Custom model: ${model.id}",
                        contextWindow = 4096,
                        pricing = Pricing(0.0, 0.0),
                        capabilities = ModelCapabilities(true, false, true)
                    )
                }
            } catch (e: Exception) {
                defaultModels()
            }
        }
    }

    override suspend fun validateApiKey(apiKey: String): Boolean {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$baseUrl/models")
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun buildRequest(
        messages: List<ChatMessage>,
        tools: List<ToolSchema>,
        stream: Boolean
    ): Request {
        val toolList = tools.map { schema ->
            CustomTool(
                type = "function",
                function = CustomFunction(
                    name = schema.name,
                    description = schema.description,
                    parameters = schema.parameters
                )
            )
        }

        val requestBody = CustomRequest(
            model = defaultModel,
            messages = messages,
            tools = if (toolList.isNotEmpty()) toolList else null,
            tool_choice = if (toolList.isNotEmpty()) "auto" else null,
            stream = stream,
            max_tokens = 4096
        )

        val json = kotlinx.serialization.json.Json.encodeToString(requestBody)

        val builder = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType()))

        customHeaders.forEach { (key, value) ->
            builder.header(key, value)
        }

        return builder.build()
    }

    private fun parseResponse(response: okhttp3.Response): ProviderResult {
        val body = response.body?.string() ?: return ProviderResult("Empty response")
        if (!response.isSuccessful) {
            return ProviderResult("API Error: ${response.code()} - $body")
        }

        val completionResponse = kotlinx.serialization.json.Json.decodeFromString<CustomResponse>(body)
        val choice = completionResponse.choices.firstOrNull() ?: return ProviderResult("No choices")

        val toolCalls = choice.message.tool_calls?.map { call ->
            ToolCall(
                id = call.id,
                name = call.function.name,
                arguments = kotlinx.serialization.json.Json.decodeFromString<Map<String, Any>>(call.function.arguments)
            )
        } ?: emptyList()

        ProviderResult(
            content = choice.message.content ?: "",
            toolCalls = toolCalls,
            usage = completionResponse.usage?.let { Usage(it.prompt_tokens, it.completion_tokens, it.total_tokens) },
            model = completionResponse.model,
            finishReason = choice.finish_reason
        )
    }

    private fun defaultModels(): List<ModelInfo> = listOf(
        ModelInfo(defaultModel, defaultModel, "Custom model", 4096, Pricing(0.0, 0.0), ModelCapabilities(true, false, true))
    )

    @Serializable data class ChatMessage(val role: String, val content: String)
    @Serializable data class CustomFunction(val name: String, val description: String, val parameters: ToolSchema)
    @Serializable data class CustomTool(val type: String, val function: CustomFunction)
    @Serializable data class CustomRequest(
        val model: String,
        val messages: List<ChatMessage>,
        val tools: List<CustomTool>? = null,
        val tool_choice: String? = null,
        val stream: Boolean = false,
        val max_tokens: Int = 4096
    )
    @Serializable data class CustomResponse(
        val id: String,
        val choices: List<Choice>,
        val model: String,
        val usage: UsageInfo?
    )
    @Serializable data class Choice(
        val index: Int,
        val message: Message,
        val finish_reason: String?
    )
    @Serializable data class Message(
        val role: String,
        val content: String?,
        val tool_calls: List<ToolCallData>? = null
    )
    @Serializable data class ToolCallData(
        val id: String,
        val type: String,
        val function: FunctionCall
    )
    @Serializable data class FunctionCall(
        val name: String,
        val arguments: String
    )
    @Serializable data class UsageInfo(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )
    @Serializable data class ModelsResponse(val data: List<ModelData>)
    @Serializable data class ModelData(val id: String)
}