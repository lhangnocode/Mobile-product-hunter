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
import kotlinx.coroutines.flow.Flow

object AgentOrchestrator {
    private const val TAG = "AgentOrchestrator"

    val DefaultAgent = Agent(
        id = "product_hunter",
        name = "Product Hunter Agent",
        systemInstruction = """
            You are Product Hunter, an on-device shopping assistant for Vietnamese e-commerce.
            Help users search, compare, and track products across Shopee, Lazada, and Tiki.
            Prefer concise answers, mention uncertainty clearly, and suggest price alerts when useful.
        """.trimIndent(),
    )

    private lateinit var llmHelper: LlmHelper
    private lateinit var llmRuntime: LlmRuntime
    private lateinit var conversationRepository: AgentConversationRepository
    private var isConfigured = false

    val defaultModel: LlmModelSpec
        get() = LlmRuntime.DefaultModel

    fun configure(
        context: Context,
        conversationRepository: AgentConversationRepository,
    ) {
        if (isConfigured) return

        val appContext = context.applicationContext
        llmHelper = LlmHelper(appContext)
        llmRuntime = LlmRuntime(appContext, llmHelper)
        this.conversationRepository = conversationRepository
        isConfigured = true

        ILog.i(TAG, "configure", "configured")
    }

    suspend fun init(): Result<Unit> {
        ensureConfigured()

        return if (llmRuntime.isDefaultModelDownloaded()) {
            ILog.i(TAG, "init", "default model found")
            llmRuntime.`init`()
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

            conversationRepository.appendUserMessage(
                conversationId = conversation.id,
                text = persistedUserText,
            )

            val history = conversationRepository.getMessages(conversation.id)
            val config = buildConversationConfig(agent, history)
            llmRuntime.switchConversation(config).getOrThrow()

            var finalText = ""
            llmRuntime.sendMessage(contents).collect { event ->
                when (event) {
                    LlmChatEvent.Started -> {
                        callback.onStarted()
                    }
                    is LlmChatEvent.Message -> {
                        finalText = event.text
                        callback.onMessage(event.text)
                    }
                    is LlmChatEvent.Completed -> {
                        finalText = event.text
                        if (finalText.isNotBlank()) {
                            conversationRepository.appendModelMessage(
                                conversationId = conversation.id,
                                text = finalText,
                            )
                        }
                        callback.onCompleted(finalText)
                    }
                    is LlmChatEvent.Failed -> {
                        callback.onFailed(event.message, event.cause)
                    }
                }
            }
        }.onFailure { error ->
            ILog.e(TAG, "performAgentCall", "failed", throwable = error)
            callback.onFailed(error.message ?: "Agent call failed", error)
        }
    }

    fun close() {
        if (!isConfigured) return
        llmRuntime.close()
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

    private fun buildConversationConfig(
        agent: Agent,
        history: List<AgentMessageEntity>,
    ): ConversationConfig =
        ConversationConfig(
            systemInstruction = Contents.of(agent.systemInstruction),
            initialMessages = history.mapNotNull { it.toLiteRtMessage() },
            tools = emptyList(),
            samplerConfig = agent.samplerConfig.toLiteRtSamplerConfig(),
            automaticToolCalling = agent.automaticToolCalling,
        )

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
}
