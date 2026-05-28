package android.app.producthunt.ui.viewmodel

import android.app.producthunt.core.agent.AgentOrchestrator
import android.app.producthunt.core.llm.LlmModelDownloadEvent
import android.app.producthunt.ui.state.AgentManagementUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgentManagementViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AgentManagementUiState())
    val uiState: StateFlow<AgentManagementUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val model = AgentOrchestrator.defaultModel
            _uiState.update {
                it.copy(
                    repoId = model.repoId,
                    filename = model.filename,
                    revision = model.revision,
                    modelPath = AgentOrchestrator.getModelPath(),
                    isDownloaded = AgentOrchestrator.isModelDownloaded(),
                    isEngineInitialized = AgentOrchestrator.isRuntimeInitialized(),
                    errorMessage = null,
                )
            }
        }
    }

    fun downloadModel() {
        viewModelScope.launch {
            AgentOrchestrator.downloadModel().collect { state ->
                when (state) {
                    LlmModelDownloadEvent.Idle -> Unit
                    is LlmModelDownloadEvent.Starting -> {
                        _uiState.update {
                            it.copy(
                                isDownloading = true,
                                downloadedBytes = 0L,
                                totalBytes = null,
                                downloadPercent = null,
                                errorMessage = null,
                            )
                        }
                    }
                    is LlmModelDownloadEvent.Progress -> {
                        _uiState.update {
                            it.copy(
                                isDownloading = true,
                                downloadedBytes = state.downloadedBytes,
                                totalBytes = state.totalBytes,
                                downloadPercent = state.percent,
                            )
                        }
                    }
                    is LlmModelDownloadEvent.Completed -> {
                        _uiState.update {
                            it.copy(
                                modelPath = state.modelPath,
                                isDownloaded = true,
                                isDownloading = false,
                                downloadedBytes = it.totalBytes ?: it.downloadedBytes,
                                totalBytes = it.totalBytes,
                                downloadPercent = 1f,
                                errorMessage = null,
                            )
                        }
                    }
                    is LlmModelDownloadEvent.Failed -> {
                        _uiState.update {
                            it.copy(
                                isDownloading = false,
                                errorMessage = state.message,
                            )
                        }
                    }
                }
            }
            refresh()
        }
    }

    fun initializeEngine() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitializing = true, errorMessage = null) }

            val result = AgentOrchestrator.initializeRuntime()
            _uiState.update {
                it.copy(
                    isInitializing = false,
                    isEngineInitialized = AgentOrchestrator.isRuntimeInitialized(),
                    errorMessage = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun deleteModel() {
        viewModelScope.launch {
            val deleted = AgentOrchestrator.deleteModel()
            _uiState.update {
                it.copy(
                    modelPath = null,
                    isDownloaded = false,
                    isEngineInitialized = false,
                    isDownloading = false,
                    downloadedBytes = 0L,
                    totalBytes = null,
                    downloadPercent = null,
                    errorMessage = if (deleted) null else "Failed to delete model",
                )
            }
            refresh()
        }
    }
}
