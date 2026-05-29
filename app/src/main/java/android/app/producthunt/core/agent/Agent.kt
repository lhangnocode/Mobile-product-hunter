package android.app.producthunt.core.agent

import com.google.ai.edge.litertlm.ToolProvider

data class Agent(
    val id: String,
    val name: String,
    val systemInstruction: String,
    val toolProviders: List<ToolProvider> = emptyList(),
    val samplerConfig: AgentSamplerConfig = AgentSamplerConfig(),
    val automaticToolCalling: Boolean = true,
)
