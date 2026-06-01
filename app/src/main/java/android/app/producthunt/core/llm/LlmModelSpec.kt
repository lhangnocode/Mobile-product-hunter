package android.app.producthunt.core.llm

data class LlmModelSpec(
    val repoId: String,
    val filename: String,
    val revision: String = "main",
    val expectedSha256: String? = null,
)