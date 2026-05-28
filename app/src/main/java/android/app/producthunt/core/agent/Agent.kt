package android.app.producthunt.core.agent

data class Agent(
    val id: String,
    val name: String,
    val systemInstruction: String,
    val tools: List<AgentTool> = emptyList(),
    val samplerConfig: AgentSamplerConfig = AgentSamplerConfig(),
    val automaticToolCalling: Boolean = true,
)
