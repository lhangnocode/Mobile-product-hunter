package android.app.producthunt.ui.state

data class AgentManagementUiState(
    val repoId: String = "",
    val filename: String = "",
    val revision: String = "main",
    val modelPath: String? = null,
    val isDownloaded: Boolean = false,
    val isEngineInitialized: Boolean = false,
    val isDownloading: Boolean = false,
    val isInitializing: Boolean = false,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long? = null,
    val downloadPercent: Float? = null,
    val errorMessage: String? = null,
)
