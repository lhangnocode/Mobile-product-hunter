package android.app.producthunt.ui.screens.main

import android.app.producthunt.ui.state.AgentManagementUiState
import android.app.producthunt.ui.theme.PH_Primary
import android.app.producthunt.ui.viewmodel.AgentManagementViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun AgentManagementScreen(
    viewModel: AgentManagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    AgentManagementContent(
        state = uiState,
        onDownload = viewModel::downloadModel,
        onInitialize = viewModel::initializeEngine,
        onDelete = viewModel::deleteModel,
    )
}

@Composable
private fun AgentManagementContent(
    state: AgentManagementUiState,
    onDownload: () -> Unit,
    onInitialize: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Agent",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "LiteRT-LM on-device model",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        StatusCard(state = state)

        if (state.isDownloading || state.downloadPercent != null) {
            DownloadProgressCard(state = state)
        }

        state.errorMessage?.let { message ->
            ErrorCard(message = message)
        }

        ActionCard(
            state = state,
            onDownload = onDownload,
            onInitialize = onInitialize,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun StatusCard(state: AgentManagementUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = PH_Primary)
                Spacer(Modifier.width(8.dp))
                Text("Model Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            InfoRow("Repository", state.repoId)
            InfoRow("File", state.filename)
            InfoRow("Revision", state.revision)
            InfoRow("Downloaded", if (state.isDownloaded) "Yes" else "No")
            InfoRow("Engine", if (state.isEngineInitialized) "Initialized" else "Not initialized")
            InfoRow("Path", state.modelPath ?: "Not available")
        }
    }
}

@Composable
private fun DownloadProgressCard(state: AgentManagementUiState) {
    val percent = state.downloadPercent?.coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Download", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (percent != null) {
                LinearProgressIndicator(
                    progress = { percent },
                    modifier = Modifier.fillMaxWidth(),
                    color = PH_Primary,
                )
                Text(
                    text = "${(percent * 100).toInt()}% • ${formatBytes(state.downloadedBytes)} / ${state.totalBytes?.let(::formatBytes) ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PH_Primary)
                Text(
                    text = "${formatBytes(state.downloadedBytes)} downloaded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ActionCard(
    state: AgentManagementUiState,
    onDownload: () -> Unit,
    onInitialize: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Button(
                onClick = onDownload,
                enabled = !state.isDownloaded && !state.isDownloading && !state.isInitializing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PH_Primary),
            ) {
                if (state.isDownloading) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text(if (state.isDownloading) "Downloading" else "Download Model")
            }

            Button(
                onClick = onInitialize,
                enabled = state.isDownloaded && !state.isEngineInitialized && !state.isDownloading && !state.isInitializing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isInitializing) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text(if (state.isInitializing) "Initializing" else "Initialize Engine")
            }

            OutlinedButton(
                onClick = onDelete,
                enabled = state.isDownloaded && !state.isDownloading && !state.isInitializing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete Model")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.38f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.62f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024L) return "$bytes B"

    val units = listOf("KB", "MB", "GB")
    var value = bytes.toDouble() / 1024.0
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) {
        value /= 1024.0
        index++
    }

    return "%.1f %s".format(value, units[index])
}
