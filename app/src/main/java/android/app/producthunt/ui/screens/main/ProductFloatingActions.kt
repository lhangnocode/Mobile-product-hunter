package android.app.producthunt.ui.screens.main

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

@Composable
fun ProductFloatingActions(
    productId: String,
    isWishlisted: Boolean, // Thêm trạng thái này
    hasPriceAlert: Boolean,
    onWishlistClick: () -> Unit,
    onAlertClick: () -> Unit
) {
    val alertAnimation = rememberInfiniteTransition(label = "price_alert_action")
    val alertRotation = alertAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1800
                0f at 0
                -14f at 80
                12f at 160
                -10f at 240
                8f at 320
                -5f at 400
                0f at 520
                0f at 1800
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "price_alert_bell_rotation",
    )
    val activeAlertScale = alertAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "active_price_alert_scale",
    )
    val alertColorPulse = alertAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "price_alert_color_pulse",
    )
    val alertContentColor = if (hasPriceAlert) {
        lerp(
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
            MaterialTheme.colorScheme.onPrimary,
            alertColorPulse.value,
        )
    } else {
        lerp(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.primary,
            alertColorPulse.value,
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Nút Yêu thích - Đổi Icon và Màu dựa trên isWishlisted
        FloatingActionButton(
            onClick = onWishlistClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = if (isWishlisted) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
            shape = CircleShape,
            modifier = Modifier
                .size(56.dp)
                .shadow(6.dp, CircleShape)
        ) {
            Icon(
                imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, 
                contentDescription = "Wishlist", 
                modifier = Modifier.size(26.dp)
            )
        }

        // Nút Báo giá
        FloatingActionButton(
            onClick = onAlertClick,
            containerColor = if (hasPriceAlert) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = alertContentColor,
            shape = CircleShape,
            modifier = Modifier
                .size(56.dp)
                .shadow(6.dp, CircleShape)
        ) {
            Icon(
                imageVector = if (hasPriceAlert) Icons.Default.Notifications else Icons.Outlined.NotificationsNone,
                contentDescription = if (hasPriceAlert) "Price alert active" else "Set price alert",
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (hasPriceAlert) {
                            Modifier.scale(activeAlertScale.value)
                        } else {
                            Modifier.rotate(alertRotation.value)
                        },
                    )
            )
        }
    }
}
