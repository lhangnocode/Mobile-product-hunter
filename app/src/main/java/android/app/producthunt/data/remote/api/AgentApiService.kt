package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.AgentChatRequest
import android.app.producthunt.data.remote.dto.AgentChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AgentApiService {
    @POST("api/v1/agent/chat")
    suspend fun chat(@Body request: AgentChatRequest): AgentChatResponse
}
