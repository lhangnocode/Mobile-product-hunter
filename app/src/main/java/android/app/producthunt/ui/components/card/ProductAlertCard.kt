package android.app.producthunt.ui.components.card

import android.app.producthunt.model.PriceAlert
import android.app.producthunt.ui.i18n.LocalAppStrings
import android.app.producthunt.ui.i18n.LocalLanguageMode
import android.app.producthunt.ui.i18n.formatPriceFromVnd
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun AlertCard(
    alert: PriceAlert,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val strings = LocalAppStrings.current
    val currentPrice = alert.currentPrice
    val isTargetReached = alert.targetReached
    val cardBorder = if (isTargetReached) {
        BorderStroke(1.25.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.72f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    }
    val progress = if (currentPrice != null && currentPrice > 0.0) {
        ((currentPrice - alert.targetPrice) / currentPrice)
    } else {
        0.0
    }
        .toFloat()
        .coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTargetReached) 4.dp else 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Product row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProductVisual(
                    imageUrl = alert.imageUrl,
                    color = alert.placeholderColor,
                    icon = alert.placeholderIcon,
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = alert.subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Prices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PriceColumn(label = strings.current, price = currentPrice, highlight = false)
                PriceColumn(label = strings.target, price = alert.targetPrice, highlight = true)
            }

            Spacer(Modifier.height(10.dp))

            // Progress bar
            PriceProgressBar(progress = progress, targetReached = isTargetReached)

            Spacer(Modifier.height(8.dp))

            AlertStatusIndicator(
                statusText = alert.statusText,
                targetReached = isTargetReached,
            )
        }
    }
}

@Composable
fun SwipeToRevealAlertCard(
    alert: PriceAlert,
    onDeleteClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val revealWidth = 72.dp
    val revealWidthPx = with(LocalDensity.current) { revealWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun snapClose() = scope.launch {
        offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
    }
    fun snapOpen() = scope.launch {
        offsetX.animateTo(
            -revealWidthPx,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        )
    }

    Box(modifier = Modifier.fillMaxWidth()) {

        // Outer container: padding + clip chung cho cả card lẫn nút xóa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp)),
        ) {
            // Nút xóa — matchParentSize lấy chiều cao từ card
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = Modifier
                        .width(revealWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.error)
                        .clickable { snapClose(); onDeleteClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Xóa",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            // Card trượt — modifier = Modifier vì outer Box đã xử lý padding
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            scope.launch {
                                offsetX.snapTo((offsetX.value + delta).coerceIn(-revealWidthPx, 0f))
                            }
                        },
                        onDragStopped = {
                            if (offsetX.value < -revealWidthPx * 0.4f) snapOpen() else snapClose()
                        },
                    ),
            ) {
                AlertCard(
                    alert = alert,
                    modifier = Modifier, // padding do outer Box xử lý
                    onClick = {
                        if (offsetX.value != 0f) snapClose() else onClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun ProductVisual(imageUrl: String?, color: Color, icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                contentScale = ContentScale.Crop,
            )
        } else {
            val iconTint = if (color == Color(0xFF1E1E2E)) Color.White else Color(0xFF555555)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun PriceColumn(label: String, price: Double?, highlight: Boolean) {
    val strings = LocalAppStrings.current
    val languageMode = LocalLanguageMode.current
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = price?.let { formatPriceFromVnd(it, languageMode) } ?: strings.checking,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            letterSpacing = 0.sp,
        )
    }
}

@Composable
private fun PriceProgressBar(progress: Float, targetReached: Boolean) {
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "price_progress",
    )
    val activeColors = if (targetReached) {
        listOf(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.82f),
            MaterialTheme.colorScheme.tertiary,
        )
    } else {
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = activeColors,
                    ),
                ),
        )
    }
}

@Composable
private fun AlertStatusIndicator(statusText: String, targetReached: Boolean) {
    if (targetReached) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.48f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    } else {
        Text(
            text = statusText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductAlertCardPreview() {
    AndroidAppProductHuntTheme {
        Box(modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.background)) {
            AlertCard(
                alert = PriceAlert(
                    id = 1,
                    name = "Sony WH-1000XM5",
                    subtitle = "Headphones",
                    imageUrl = null,
                    currentPrice = 348.00,
                    targetPrice = 299.0,
                    statusText = "Waiting for price drop",
                    targetReached = false,
                    placeholderColor = Color(0xFF1E1E2E),
                    placeholderIcon = Icons.Outlined.Headphones,
                ),
            )
        }
    }
}
