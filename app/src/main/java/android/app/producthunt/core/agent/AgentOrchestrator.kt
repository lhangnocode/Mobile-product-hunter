package android.app.producthunt.core.agent

import android.app.producthunt.core.llm.LlmHelper
import android.app.producthunt.core.llm.LlmChatEvent
import android.app.producthunt.core.llm.LlmModelDownloadEvent
import android.app.producthunt.core.llm.LlmModelSpec
import android.app.producthunt.core.llm.LlmRuntime
import android.app.producthunt.core.llm.LlmToolResponse
import android.app.producthunt.core.log.ILog
import android.app.producthunt.data.local.db.entity.AgentConversationEntity
import android.app.producthunt.data.local.db.entity.AgentMessageEntity
import android.app.producthunt.data.local.db.entity.AgentMessageRole
import android.app.producthunt.data.repository.AgentConversationRepository
import android.content.Context
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.ToolProvider
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow

object AgentOrchestrator {
    private const val TAG = "AgentOrchestrator"
    private const val MAX_CONTEXT_CHARS = 12000
    private const val MAX_AGENT_STEPS = 10

    val DefaultAgent = Agent(
        id = "product_hunter",
        name = "Product Hunter Agent",
        systemInstruction = """
            You are Product Hunter, an on-device shopping assistant for Vietnamese e-commerce.
            You help users find, compare, and evaluate products across Vietnamese retailers such as FPT Shop, Phong Vu, CellphoneS, Shopee, Lazada, and Tiki.

            Workflow:
            1. For greetings or app questions, answer directly without tools.
            2. For broad discovery requests, call searchProducts first, then summarize the most relevant products.
            3. For "best price", "where to buy", "compare", "vs", or platform questions, call compareProductPrices.
            4. For "deal", "discount", "hot", "trending", or "today" requests, call getTrendingDeals.
            5. When the user asks about a specific product id or wants marketplace listings, call inspectProductDeals.
            6. If the user asks whether a specific listing price is good, call analyzeListingPrice.

            Answer rules:
            - Use tools before answering any question that depends on product data, price, platform availability, or deal quality.
            - If a tool returns product data, trust the tool result. Never claim you cannot access pricing or availability after a successful tool call.
            - Keep answers concise and practical.
            - Mention platform, current price, stock, discount, and deal label when available.
            - If results are weak or missing, say so and ask for a clearer product name, budget, or category.
            - Suggest a price alert when a product is close to the user's target price or the deal is not clearly strong.

            Tool-call protocol:
            If you need product data and a native tool call is not executed automatically, return exactly one JSON object and no prose:
            {"tool":"compareProductPrices","args":{"query":"iPhone 16"}}
            Available tool names: searchProducts, compareProductPrices, getTrendingDeals, inspectProductDeals, analyzeListingPrice.
            After a tool result is provided, answer the user normally using that result.
        """.trimIndent(),
        automaticToolCalling = true,
    )

    private lateinit var llmHelper: LlmHelper
    private lateinit var llmRuntime: LlmRuntime
    private lateinit var conversationRepository: AgentConversationRepository
    private var toolProviders: List<ToolProvider> = emptyList()
    private var toolExecutor: AgentToolSet? = null
    private var activeRuntimeConversationId: String? = null
    private var activeRuntimeAgentId: String? = null
    private var isConfigured = false

    val defaultModel: LlmModelSpec
        get() = LlmRuntime.DefaultModel

    fun configure(
        context: Context,
        conversationRepository: AgentConversationRepository,
        toolProviders: List<ToolProvider> = emptyList(),
        toolExecutor: AgentToolSet? = null,
    ) {
        if (isConfigured) return

        val appContext = context.applicationContext
        llmHelper = LlmHelper(appContext)
        llmRuntime = LlmRuntime(appContext, llmHelper)
        this.conversationRepository = conversationRepository
        this.toolProviders = toolProviders
        this.toolExecutor = toolExecutor
        isConfigured = true

        ILog.i(TAG, "configure", "configured", "tools=${toolProviders.size}")
    }

    suspend fun init(): Result<Unit> {
        ensureConfigured()

        return if (llmRuntime.isDefaultModelDownloaded()) {
            ILog.i(TAG, "init", "default model found")
            llmRuntime.init()
        } else {
            ILog.i(TAG, "init", "default model missing, skipping runtime init")
            Result.success(Unit)
        }
    }

    fun isModelDownloaded(): Boolean {
        ensureConfigured()
        return llmHelper.isModelDownloaded(defaultModel)
    }

    suspend fun getModelPath(): String? {
        ensureConfigured()
        return llmHelper.getModelPath(defaultModel)
    }

    fun downloadModel(force: Boolean = false): Flow<LlmModelDownloadEvent> {
        ensureConfigured()
        return llmHelper.downloadModel(defaultModel, force)
    }

    suspend fun deleteModel(): Boolean {
        ensureConfigured()
        llmRuntime.close()
        clearActiveRuntimeConversation()
        return llmHelper.deleteModel(defaultModel)
    }

    suspend fun initializeRuntime(): Result<Unit> {
        ensureConfigured()
        return llmRuntime.`init`()
    }

    fun isRuntimeInitialized(): Boolean {
        ensureConfigured()
        return llmRuntime.isInitialized()
    }

    suspend fun performAgentCall(
        prompt: String,
        conversationId: String? = null,
        agent: Agent = DefaultAgent,
        callback: AgentCallCallback,
    ) {
        performAgentCall(
            contents = Contents.of(buildAgentTurnPrompt(prompt)),
            persistedUserText = prompt,
            conversationId = conversationId,
            agent = agent,
            callback = callback,
        )
    }

    suspend fun performAgentCall(
        contents: Contents,
        persistedUserText: String,
        conversationId: String? = null,
        agent: Agent = DefaultAgent,
        callback: AgentCallCallback,
    ) {
        ensureConfigured()

        runCatching {
            val conversation = resolveConversation(conversationId, agent)
            callback.onConversationReady(conversation.id)

            ensureRuntimeConversation(conversation, agent)

            conversationRepository.appendUserMessage(
                conversationId = conversation.id,
                text = persistedUserText,
            )

            AgentToolResultStore.clear()
            val observedToolPayloads = mutableSetOf<String>()

            var nextInput: AgentLoopInput = AgentLoopInput.User(contents)
            var started = false

            for (step in 1..MAX_AGENT_STEPS) {
                ILog.d(TAG, "performAgentCall", "agent step=$step")
                val stepResult = runAgentStep(
                    input = nextInput,
                    emitStarted = !started,
                    callback = callback,
                )
                started = true

                val toolResults = collectToolResults(
                    nativeToolResults = stepResult.toolResults,
                    conversationId = conversation.id,
                    callback = callback,
                    observedToolPayloads = observedToolPayloads,
                )

                if (toolResults.isNotEmpty()) {
                    ILog.d(TAG, "performAgentCall", "feeding tool results to model", "count=${toolResults.size}")
                    nextInput = AgentLoopInput.ToolResults(toolResults)
                    continue
                }

                val plannedToolCall = parsePlannedToolCall(stepResult.finalText)
                if (plannedToolCall != null) {
                    val plannedToolResult = executePlannedToolCall(plannedToolCall)
                    if (plannedToolResult != null) {
                        ILog.d(TAG, "performAgentCall", "executed planned tool", plannedToolCall.name)
                        val plannedResults = collectToolResults(
                            nativeToolResults = listOf(plannedToolResult),
                            conversationId = conversation.id,
                            callback = callback,
                            observedToolPayloads = observedToolPayloads,
                        )
                        nextInput = AgentLoopInput.ToolResults(plannedResults.ifEmpty { listOf(plannedToolResult) })
                        continue
                    }
                }

                if (stepResult.finalText.isNotBlank()) {
                    callback.onMessage(stepResult.finalText)
                    conversationRepository.appendModelMessage(
                        conversationId = conversation.id,
                        text = stepResult.finalText,
                    )
                }
                callback.onCompleted(stepResult.finalText)
                return@runCatching
            }

            callback.onFailed("Agent reached max tool steps ($MAX_AGENT_STEPS)")
        }.onFailure { error ->
            ILog.e(TAG, "performAgentCall", "failed", throwable = error)
            callback.onFailed(error.message ?: "Agent call failed", error)
        }
    }

    fun close() {
        if (!isConfigured) return
        llmRuntime.close()
        clearActiveRuntimeConversation()
    }

    private suspend fun resolveConversation(
        conversationId: String?,
        agent: Agent,
    ): AgentConversationEntity {
        if (conversationId != null) {
            conversationRepository.getConversation(conversationId)?.let { return it }
        }

        return conversationRepository.createConversation(
            agentId = agent.id,
            title = null,
        )
    }

    private suspend fun ensureRuntimeConversation(
        conversation: AgentConversationEntity,
        agent: Agent,
    ) {
        if (
            activeRuntimeConversationId == conversation.id &&
            activeRuntimeAgentId == agent.id
        ) {
            ILog.d(TAG, "ensureRuntimeConversation", "reusing active conversation", conversation.id)
            return
        }

        val history = conversationRepository.getMessages(conversation.id)
        val config = buildConversationConfig(agent, history)
        llmRuntime.switchConversation(config).getOrThrow()
        activeRuntimeConversationId = conversation.id
        activeRuntimeAgentId = agent.id
    }

    private suspend fun runAgentStep(
        input: AgentLoopInput,
        emitStarted: Boolean,
        callback: AgentCallCallback,
    ): AgentStepResult {
        var finalText = ""
        val toolResults = mutableListOf<AgentToolResult>()

        val events = when (input) {
            is AgentLoopInput.User -> llmRuntime.sendMessage(input.contents)
            is AgentLoopInput.ToolResults -> llmRuntime.sendToolResponses(
                input.results.map { LlmToolResponse(it.name, it.payload) }
            )
        }

        events.collect { event ->
            when (event) {
                LlmChatEvent.Started -> {
                    if (emitStarted) callback.onStarted()
                }
                is LlmChatEvent.Message -> {
                    finalText = event.text
                }
                is LlmChatEvent.ToolResponse -> {
                    toolResults += AgentToolResult(event.name, event.payload)
                }
                is LlmChatEvent.Completed -> {
                    finalText = event.text
                }
                is LlmChatEvent.Failed -> {
                    callback.onFailed(event.message, event.cause)
                }
            }
        }

        return AgentStepResult(
            finalText = finalText,
            toolResults = toolResults,
        )
    }

    private suspend fun collectToolResults(
        nativeToolResults: List<AgentToolResult>,
        conversationId: String,
        callback: AgentCallCallback,
        observedToolPayloads: MutableSet<String>,
    ): List<AgentToolResult> {
        val results = (nativeToolResults + AgentToolResultStore.drain())
            .distinctBy { "${it.name}:${it.payload}" }
            .filterNot { "${it.name}:${it.payload}" in observedToolPayloads }

        ILog.d(TAG, "collectToolResults", "count=${results.size}")
        results.forEach { result ->
            dispatchToolResponse(
                conversationId = conversationId,
                name = result.name,
                payload = result.payload,
                callback = callback,
                observedToolPayloads = observedToolPayloads,
            )
        }

        return results
    }

    private suspend fun dispatchToolResponse(
        conversationId: String,
        name: String,
        payload: String,
        callback: AgentCallCallback,
        observedToolPayloads: MutableSet<String>,
    ) {
        if (!observedToolPayloads.add("$name:$payload")) return

        ILog.d(TAG, "dispatchToolResponse", "name=$name", "payloadLength=${payload.length}")
        conversationRepository.appendToolMessage(
            conversationId = conversationId,
            text = payload,
        )
        callback.onToolResponse(name, payload)
    }

    private fun executePlannedToolCall(toolCall: PlannedToolCall): AgentToolResult? {
        val executor = toolExecutor ?: return null
        AgentToolResultStore.clear()

        val payload = when (toolCall.name) {
            "searchProducts" -> executor.searchProducts(
                query = toolCall.args.string("query") ?: toolCall.args.string("q") ?: return null,
                page = toolCall.args.int("page") ?: 1,
                limit = toolCall.args.int("limit") ?: 6,
            )
            "compareProductPrices" -> executor.compareProductPrices(
                query = toolCall.args.string("query") ?: toolCall.args.string("q") ?: return null,
            )
            "getTrendingDeals" -> executor.getTrendingDeals(
                limit = toolCall.args.int("limit") ?: 6,
            )
            "inspectProductDeals" -> executor.inspectProductDeals(
                productId = toolCall.args.string("productId")
                    ?: toolCall.args.string("product_id")
                    ?: return null,
                limit = toolCall.args.int("limit") ?: 6,
            )
            "analyzeListingPrice" -> executor.analyzeListingPrice(
                platformProductId = toolCall.args.string("platformProductId")
                    ?: toolCall.args.string("platform_product_id")
                    ?: return null,
                currentPrice = toolCall.args.double("currentPrice")
                    ?: toolCall.args.double("current_price")
                    ?: return null,
                originalPrice = toolCall.args.double("originalPrice")
                    ?: toolCall.args.double("original_price")
                    ?: toolCall.args.double("currentPrice")
                    ?: toolCall.args.double("current_price")
                    ?: return null,
            )
            else -> return null
        }

        return AgentToolResult(toolCall.name, payload)
    }

    private fun parsePlannedToolCall(text: String): PlannedToolCall? {
        val jsonText = text.extractJsonObject() ?: return null
        val root = runCatching { JsonParser.parseString(jsonText).asJsonObject }.getOrNull() ?: return null
        val name = root.string("tool") ?: root.string("name") ?: return null
        val args = root.getAsJsonObject("args")
            ?: root.getAsJsonObject("arguments")
            ?: JsonObject()

        return PlannedToolCall(name = name, args = args)
    }

    private fun String.extractJsonObject(): String? {
        val trimmed = trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return trimmed.substring(start, end + 1)
    }

    private fun buildAgentTurnPrompt(prompt: String): String =
        """
            User request:
            $prompt

            Decide the next action.
            If product data, pricing, availability, store listings, deals, or comparison is needed, return only this JSON shape:
            {"tool":"compareProductPrices","args":{"query":"$prompt"}}

            Choose one tool:
            - searchProducts: broad product discovery by query.
            - compareProductPrices: price comparison, best place to buy, where to buy, vs/comparison.
            - getTrendingDeals: hot deals, discounts, sale, trending deals.
            - inspectProductDeals: listings for a known product id.
            - analyzeListingPrice: deal quality for a known platform listing id and price.

            If no tool is needed, answer normally.
        """.trimIndent()

    private fun JsonObject.string(name: String): String? =
        get(name)?.takeIf { !it.isJsonNull }?.asString

    private fun JsonObject.int(name: String): Int? =
        get(name)?.takeIf { !it.isJsonNull }?.runCatching { asInt }?.getOrNull()

    private fun JsonObject.double(name: String): Double? =
        get(name)?.takeIf { !it.isJsonNull }?.runCatching { asDouble }?.getOrNull()

    private fun buildConversationConfig(
        agent: Agent,
        history: List<AgentMessageEntity>,
    ): ConversationConfig {
        val resolvedToolProviders = resolveToolProviders(agent)
        ILog.d(
            TAG,
            "buildConversationConfig",
            "tools=${resolvedToolProviders.size}",
            "automaticToolCalling=${agent.automaticToolCalling}",
        )

        return ConversationConfig(
            systemInstruction = Contents.of(agent.systemInstruction),
            initialMessages = trimHistoryForContext(history)
                .mapNotNull { it.toLiteRtMessage() },
            tools = resolvedToolProviders,
            samplerConfig = agent.samplerConfig.toLiteRtSamplerConfig(),
            automaticToolCalling = agent.automaticToolCalling,
        )
    }

    private fun resolveToolProviders(agent: Agent): List<ToolProvider> =
        toolProviders + agent.toolProviders

    private fun trimHistoryForContext(
        history: List<AgentMessageEntity>,
        maxChars: Int = MAX_CONTEXT_CHARS,
    ): List<AgentMessageEntity> {
        if (history.isEmpty()) return history

        val filtered = history.filter { it.role != AgentMessageRole.TOOL }
        if (filtered.isEmpty()) return emptyList()

        val trimmed = ArrayDeque<AgentMessageEntity>()
        var remaining = maxChars

        for (message in filtered.asReversed()) {
            if (remaining <= 0) break
            val text = message.text
            if (text.length <= remaining) {
                trimmed.addFirst(message)
                remaining -= text.length
            } else if (trimmed.isEmpty()) {
                val truncated = text.takeLast(remaining)
                trimmed.addFirst(message.copy(text = truncated))
                remaining = 0
            }
        }

        return trimmed.toList()
    }

    private fun AgentMessageEntity.toLiteRtMessage(): Message? =
        when (role) {
            AgentMessageRole.USER -> Message.user(text)
            AgentMessageRole.MODEL -> Message.model(text)
            AgentMessageRole.TOOL -> Message.tool(Contents.of(text))
        }

    private fun AgentSamplerConfig.toLiteRtSamplerConfig(): SamplerConfig =
        SamplerConfig(
            topK,
            topP,
            temperature,
            seed,
        )

    private fun ensureConfigured() {
        check(isConfigured) { "AgentOrchestrator is not configured" }
    }

    private fun clearActiveRuntimeConversation() {
        activeRuntimeConversationId = null
        activeRuntimeAgentId = null
    }

    private sealed interface AgentLoopInput {
        data class User(val contents: Contents) : AgentLoopInput
        data class ToolResults(val results: List<AgentToolResult>) : AgentLoopInput
    }

    private data class AgentStepResult(
        val finalText: String,
        val toolResults: List<AgentToolResult>,
    )

    private data class PlannedToolCall(
        val name: String,
        val args: JsonObject,
    )
}
