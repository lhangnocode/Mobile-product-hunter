package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.AgentChatRequest
import android.app.producthunt.data.remote.dto.AgentChatResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

interface AgentApiService {
    @POST("api/v1/agent/chat")
    suspend fun chat(@Body request: AgentChatRequest): AgentChatResponse

    @Streaming
    @POST("api/v1/agent/chat/stream")
    suspend fun chatStream(@Body request: AgentChatRequest): ResponseBody
}
