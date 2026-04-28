package android.app.producthunt.ui.components.card

import android.app.producthunt.model.PriceAlert
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme
import android.app.producthunt.ui.theme.ColorBackground
import android.app.producthunt.ui.theme.ColorBorder
import android.app.producthunt.ui.theme.ColorGreen
import android.app.producthunt.ui.theme.ColorOrange
import android.app.producthunt.ui.theme.ColorOrangeDark
import android.app.producthunt.ui.theme.ColorSurface
import android.app.producthunt.ui.theme.ColorText
import android.app.producthunt.ui.theme.ColorTextSub
import android.app.producthunt.ui.theme.ColorTrackBg
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AlertCard(alert: PriceAlert) {
    val distancePct = ((alert.currentPrice - alert.targetPrice) / alert.targetPrice * 100).toInt()
    val progress = ((alert.currentPrice - alert.targetPrice) / alert.currentPrice)
        .toFloat()
        .coerceIn(0f, 1f)
    val isNearTarget = distancePct <= 15

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Product row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProductPlaceholder(
                    color = alert.placeholderColor,
                    icon = alert.placeholderIcon,
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorText,
                    )
                    Text(
                        text = alert.subtitle,
                        fontSize = 13.sp,
                        color = ColorTextSub,
                    )
                }
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = ColorTextSub,
                        modifier = Modifier.size(16.dp),
                    )
                }
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = ColorTextSub,
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
                PriceColumn(label = "CURRENT", price = alert.currentPrice, highlight = false)
                PriceColumn(label = "TARGET", price = alert.targetPrice, highlight = true)
            }

            Spacer(Modifier.height(10.dp))

            // Progress bar
            PriceProgressBar(progress = progress)

            Spacer(Modifier.height(8.dp))

            // Status
            val statusColor = if (isNearTarget) ColorGreen else ColorOrange
            val statusText = if (isNearTarget)
                "↗ Near target (${distancePct}% remaining)"
            else
                "↗ ${distancePct}% away from target price"

            Text(
                text = statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor,
            )
        }
    }
}

@Composable
private fun ProductPlaceholder(color: Color, icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .border(1.dp, ColorBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        val iconTint = if (color == Color(0xFF1E1E2E)) Color.White else Color(0xFF555555)
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun PriceColumn(label: String, price: Double, highlight: Boolean) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextSub,
            letterSpacing = 0.8.sp,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "$${"%.2f".format(price)}",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (highlight) ColorOrange else ColorText,
            letterSpacing = (-0.5).sp,
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
            .background(ColorTrackBg),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(ColorOrange, ColorOrangeDark),
                    ),
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductAlertCardPreview() {
    AndroidAppProductHuntTheme {
        Box(modifier = Modifier.padding(16.dp).background(ColorBackground)) {
            AlertCard(
                alert = PriceAlert(
                    id = 1,
                    name = "Sony WH-1000XM5",
                    subtitle = "Headphones",
                    currentPrice = 348.00,
                    targetPrice = 299.0,
                    placeholderColor = Color(0xFF1E1E2E),
                    placeholderIcon = Icons.Outlined.Headphones,
                ),
            )
        }
    }
}
