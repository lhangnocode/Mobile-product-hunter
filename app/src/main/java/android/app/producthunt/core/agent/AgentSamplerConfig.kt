package android.app.producthunt.core.agent

data class AgentSamplerConfig(
    val topK: Int = 40,
    val topP: Double = 0.95,
    val temperature: Double = 0.7,
    val seed: Int = 0,
)
