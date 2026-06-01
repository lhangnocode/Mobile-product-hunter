package android.app.producthunt.core.agent

import android.app.producthunt.core.llm.LlmHelper
import android.app.producthunt.core.llm.LlmChatEvent
import android.app.producthunt.core.llm.LlmModelDownloadEvent
import android.app.producthunt.core.llm.LlmModelSpec
import android.app.producthunt.core.llm.LlmRuntime
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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

object AgentOrchestrator {
    private const val TAG = "AgentOrchestrator"
    private const val MAX_CONTEXT_CHARS = 12000

    val DefaultAgent = Agent(
        id = "product_hunter",
        name = "Product Hunter Agent",
        systemInstruction = """
            You are Product Hunter, an on-device shopping assistant for Vietnamese e-commerce.
            You help users find, compare, and evaluate products across Vietnamese retailers such as FPT Shop, Phong Vu, CellphoneS.

            Thinking workflow:
            1. Understand the user's real shopping goal: find, compare, choose, evaluate, or track.
            2. Act with the information already available. Do not ask for store, budget, variant, or category unless the task is impossible without it.
            3. Use tools aggressively to gather product data, listings, prices, stock, deals, URLs, price records, and price quality.
            4. Use more than one tool when needed. For example, search for products, then inspect likely product ids for listings before answering.
            5. Compare the evidence from tool results and choose the most useful answer for the user.
            6. Give the user as much useful information as possible: best option, alternatives, prices, platforms, stock, URLs, deal status, and uncertainty.
            7. Ask a follow-up only after tools return no useful products or listings.

            Grounding rules:
            - Preserve the user's exact product wording and model number when querying tools. Do not change "iPhone 17" into "iPhone 7", "iPhone 15", or another model.
            - Do not use your general knowledge to decide whether a product is released, future, real, or unavailable. The API results are the source of truth for this app.
            - Never make assumptions about products. Product existence, release status, variants, specs, prices, stock, stores, and deal quality must come from tool data.
            - The tool data is newer and more reliable than your model knowledge. If tool data conflicts with your knowledge, ignore your knowledge and use the tool data.
            - If a product appears in tool results, treat it as a product known by the app data. Do not call it future, unreleased, rumored, or unavailable unless the tool result explicitly says that.
            - For buy, shop, best place, price, stock, availability, retailer, and comparison tasks, use search_products first. If the user chooses or the result contains a clear product id, use get_product_detail for marketplace listings.
            - For price trend, price history, lowest price, highest price, or "is this a good deal" tasks, prefer get_product_price_records for the listing before analyze_listing_price.
            - If a tool returns matching products or listings, answer from those results. Do not say you cannot access pricing or availability.
            - If search_products returns multiple plausible variants, do not ask the user which variant first. Inspect or summarize the top matching variants and give a ranked recommendation.
            - If results are noisy, rank the closest matching product names, include useful details from them, and be explicit about uncertainty.

            Answer rules:
            - Complete the task as far as possible before asking the user for more input.
            - For product data, price, shop, stock, deal, comparison, or recommendation tasks, use tools before answering.
            - If a tool returns product data, trust the tool result. Never claim you cannot access pricing or availability after a successful tool call.
            - Do not answer with only clarification questions when tools can produce a partial answer.
            - If there are multiple plausible products, inspect or summarize the best few instead of stopping.
            - Prefer giving extra information over asking for specific information. Ask for variant, retailer, storage, color, or budget only at the end as an optional refinement.
            - Keep answers concise and practical.
            - Mention the best option first, then useful details: platform, current price, original price, stock, discount, URL, recent price records, lowest price, highest price, and deal label when available.
            - When comparing products, state the winner for the user's likely goal and mention tradeoffs.
            - If results are weak or missing, say what was checked and ask for a clearer product name, variant, budget, or preferred retailer.
            - Suggest a price alert when a product is close to the user's target price or the deal is not clearly strong.
        """.trimIndent(),
        samplerConfig = AgentSamplerConfig(
            topK = 20,
            topP = 0.9,
            temperature = 0.2,
            seed = 0,
        ),
        automaticToolCalling = true,
    )

    private lateinit var llmHelper: LlmHelper
    private lateinit var llmRuntime: LlmRuntime
    private lateinit var conversationRepository: AgentConversationRepository
    private var toolProviders: List<ToolProvider> = emptyList()
    private var activeRuntimeConversationId: String? = null
    private var activeRuntimeAgentId: String? = null
    private var isConfigured = false

    val defaultModel: LlmModelSpec
        get() = LlmRuntime.DefaultModel

    fun configure(
        context: Context,
        conversationRepository: AgentConversationRepository,
        toolProviders: List<ToolProvider> = emptyList(),
    ) {
        if (isConfigured) return

        val appContext = context.applicationContext
        llmHelper = LlmHelper(appContext)
        llmRuntime = LlmRuntime(appContext, llmHelper)
        this.conversationRepository = conversationRepository
        this.toolProviders = toolProviders
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
            contents = Contents.of(prompt),
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
            var finalText = ""
            var failed = false

            coroutineScope {
                val toolStartedJob = launch(start = CoroutineStart.UNDISPATCHED) {
                    AgentToolResultStore.startedEvents.collect { event ->
                        callback.onToolStarted(event.name, event.input)
                    }
                }

                try {
                    llmRuntime.sendMessageFinal(contents).collect { event ->
                        when (event) {
                            LlmChatEvent.Started -> callback.onStarted()
                            is LlmChatEvent.Message -> {
                                finalText = event.text
                                callback.onMessage(event.text)
                            }
                            is LlmChatEvent.ToolResponse -> {
                                dispatchToolResponse(
                                    conversationId = conversation.id,
                                    name = event.name,
                                    payload = event.payload,
                                    callback = callback,
                                )
                            }
                            is LlmChatEvent.Completed -> {
                                finalText = event.text
                            }
                            is LlmChatEvent.Failed -> {
                                failed = true
                                callback.onFailed(event.message, event.cause)
                            }
                        }
                    }
                } finally {
                    toolStartedJob.cancel()
                }
            }

            if (failed) return@runCatching

            dispatchRecordedToolResponses(
                conversationId = conversation.id,
                callback = callback,
            )

            if (finalText.isNotBlank()) {
                conversationRepository.appendModelMessage(
                    conversationId = conversation.id,
                    text = finalText,
                )
            }
            callback.onCompleted(finalText)
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

    private suspend fun dispatchRecordedToolResponses(
        conversationId: String,
        callback: AgentCallCallback,
    ) {
        val results = AgentToolResultStore.drain()
            .distinctBy { "${it.name}:${it.payload}" }

        ILog.d(TAG, "dispatchRecordedToolResponses", "count=${results.size}")
        results.forEach { result ->
            dispatchToolResponse(
                conversationId = conversationId,
                name = result.name,
                payload = result.payload,
                callback = callback,
            )
        }
    }

    private suspend fun dispatchToolResponse(
        conversationId: String,
        name: String,
        payload: String,
        callback: AgentCallCallback,
    ) {
        ILog.d(TAG, "dispatchToolResponse", "name=$name", "payloadLength=${payload.length}")
        conversationRepository.appendToolMessage(
            conversationId = conversationId,
            text = payload,
        )
        callback.onToolResponse(name, payload)
    }

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

}
