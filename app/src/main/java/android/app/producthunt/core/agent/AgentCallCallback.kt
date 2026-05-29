package android.app.producthunt.core.agent

interface AgentCallCallback {
    fun onConversationReady(conversationId: String) = Unit
    fun onStarted() = Unit
    fun onMessage(text: String) = Unit
    fun onToolResponse(name: String, payload: String) = Unit
    fun onCompleted(text: String) = Unit
    fun onFailed(message: String, cause: Throwable? = null) = Unit
}
