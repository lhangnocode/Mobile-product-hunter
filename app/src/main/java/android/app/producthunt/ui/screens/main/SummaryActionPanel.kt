package android.app.producthunt.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SummaryActionPanel(
    title: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actions: List<SummaryPanelAction> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (actions.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actions.forEach { action ->
                        SummaryPanelActionButton(action = action)
                    }
                }
            }
        }
    }
}

internal data class SummaryPanelAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val destructive: Boolean = false,
)

internal fun clearSummaryAction(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean,
) = SummaryPanelAction(
    label = label.replace("\n", " "),
    icon = Icons.Filled.Delete,
    onClick = onClick,
    enabled = enabled,
    loading = loading,
    destructive = true,
)

internal fun checkSummaryAction(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean,
) = SummaryPanelAction(
    label = label.replace("\n", " "),
    icon = Icons.Filled.PlayArrow,
    onClick = onClick,
    enabled = enabled,
    loading = loading,
)

@Composable
private fun SummaryPanelActionButton(action: SummaryPanelAction) {
    val containerColor =
        if (action.destructive) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
        }
    val contentColor =
        if (action.destructive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.tertiary
        }
    val enabled = action.enabled && !action.loading

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .alpha(if (enabled || action.loading) 1f else 0.45f)
            .clickable(enabled = enabled, onClick = action.onClick),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier
                .width(70.dp)
                .padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (action.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = contentColor,
                )
            } else {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = action.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}
