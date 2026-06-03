package android.app.producthunt.ui.components.card

import android.app.producthunt.model.PriceAlert
import android.app.producthunt.ui.i18n.LocalAppStrings
import android.app.producthunt.ui.i18n.LocalLanguageMode
import android.app.producthunt.ui.i18n.formatPriceFromVnd
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun AlertCard(
    alert: PriceAlert,
    onDeleteClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val strings = LocalAppStrings.current
    val currentPrice = alert.currentPrice
    val progress = if (currentPrice != null && currentPrice > 0.0) {
        ((currentPrice - alert.targetPrice) / currentPrice)
    } else {
        0.0
    }
        .toFloat()
        .coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
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
            PriceProgressBar(progress = progress)

            Spacer(Modifier.height(8.dp))

            Text(
                text = alert.statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (alert.targetReached) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
            )
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
private fun PriceProgressBar(progress: Float) {
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "price_progress",
    )
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
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
                    ),
                ),
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
