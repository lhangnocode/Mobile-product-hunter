package android.app.producthunt.core.llm

import android.app.producthunt.core.log.ILog
import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class LlmRuntime(
    private val context: Context,
    private val llmHelper: LlmHelper,
) : AutoCloseable {
    companion object {
        private const val TAG = "LlmRuntime"

        val DefaultModel = LlmModelSpec(
            repoId = "litert-community/gemma-4-E2B-it-litert-lm",
            filename = "gemma-4-E2B-it.litertlm",
        )
    }

    private val mutex = Mutex()
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    fun isDefaultModelDownloaded(): Boolean {
        val isDownloaded = llmHelper.isModelDownloaded(DefaultModel)
        ILog.d(TAG, "isDefaultModelDownloaded", isDownloaded)
        return isDownloaded
    }

    suspend fun init(): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {
            runCatching {
                if (engine?.isInitialized() == true) {
                    ILog.d(TAG, "init", "engine already initialized")
                    return@runCatching
                }

                if (!isDefaultModelDownloaded()) {
                    ILog.w(TAG, "init", "model missing", DefaultModel.filename)
                    error("LiteRT-LM model is not downloaded: ${DefaultModel.filename}")
                }

                val modelPath = llmHelper.getModelPath(DefaultModel)
                    ?: error("LiteRT-LM model is not downloaded: ${DefaultModel.filename}")

                ILog.i(TAG, "init", "initializing engine", modelPath)
                val cacheDir = File(context.cacheDir, "litertlm").apply { mkdirs() }
                val config = EngineConfig(
                    modelPath = modelPath,
                    backend = Backend.GPU(),
                    visionBackend = Backend.CPU(),
                    audioBackend = Backend.CPU(),
                    cacheDir = cacheDir.absolutePath,
                )

                engine?.close()
                engine = Engine(config).also { it.initialize() }
                ILog.i(TAG, "init", "engine initialized")
            }
                .onFailure { ILog.e(TAG, "init", "engine initialization failed", throwable = it) }
        }
    }

    suspend fun switchConversation(config: ConversationConfig): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {
            runCatching {
                ensureEngineInitialized()

                conversation?.cancelProcess()
                conversation?.close()
                conversation = requireNotNull(engine).createConversation(config)

                ILog.i(TAG, "switchConversation", "conversation switched")
            }
                .onFailure { ILog.e(TAG, "switchConversation", "failed", throwable = it) }
        }
    }

    fun sendMessage(contents: Contents): Flow<LlmChatEvent> = flow {
        emit(LlmChatEvent.Started)

        val activeConversation = mutex.withLock {
            conversation ?: error("No active LLM conversation")
        }

        var latestText = ""
        try {
            activeConversation.sendMessageAsync(contents).collect { response ->
                response.extractToolResponses().forEach { toolEvent ->
                    emit(toolEvent)
                }
                latestText = mergeStreamText(latestText, response.extractText())
                if (latestText.isNotEmpty()) {
                    emit(LlmChatEvent.Message(latestText))
                }
            }
            emit(LlmChatEvent.Completed(latestText))
        } catch (e: Exception) {
            ILog.e(TAG, "sendMessage", "failed", throwable = e)
            emit(LlmChatEvent.Failed(e.message ?: "LLM message failed", e))
        }
    }.flowOn(Dispatchers.IO)

    fun sendToolResponses(toolResponses: List<LlmToolResponse>): Flow<LlmChatEvent> =
        sendMessage(
            Contents.of(
                toolResponses.map { response ->
                    Content.ToolResponse(
                        name = response.name,
                        response = response.payload.toJsonPayload(),
                    )
                }
            )
        )

    suspend fun cancel() = mutex.withLock {
        ILog.i(TAG, "cancel", "active conversation")
        conversation?.cancelProcess()
    }

    fun isInitialized(): Boolean = engine?.isInitialized() == true

    override fun close() {
        ILog.i(TAG, "close", "closing runtime")
        conversation?.cancelProcess()
        conversation?.close()
        conversation = null
        engine?.close()
        engine = null
    }

    private suspend fun ensureEngineInitialized() {
        if (engine?.isInitialized() == true) return
        `init`().getOrThrow()
    }

    private fun Message.extractText(): String =
        contents.contents
            .filterIsInstance<Content.Text>()
            .joinToString(separator = "") { it.text }

    private fun mergeStreamText(current: String, incoming: String): String =
        when {
            incoming.isEmpty() -> current
            incoming.startsWith(current) -> incoming
            else -> current + incoming
        }

    private fun String.toJsonPayload(): Any =
        runCatching { JsonParser.parseString(this) }
            .getOrElse { this }

    private fun Message.extractToolResponses(): List<LlmChatEvent.ToolResponse> {
        val toolResponses = contents.contents.filterIsInstance<Content.ToolResponse>()
        if (toolResponses.isEmpty()) return emptyList()

        return toolResponses.map { response ->
            val payload = when (val toolPayload = response.response) {
                is String -> toolPayload
                else -> com.google.gson.Gson().toJson(toolPayload)
            }
            ILog.d(TAG, "extractToolResponses", "name=${response.name}", "payloadLength=${payload.length}")
            LlmChatEvent.ToolResponse(response.name, payload)
        }
    }
}
