package android.app.producthunt.ui.screens.main

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
import androidx.compose.ui.unit.dp

@Composable
fun ProductFloatingActions(
    productId: String,
    isWishlisted: Boolean, // Thêm trạng thái này
    hasPriceAlert: Boolean,
    onWishlistClick: () -> Unit,
    onAlertClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Nút Yêu thích - Đổi Icon và Màu dựa trên isWishlisted
        FloatingActionButton(
            onClick = onWishlistClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = if (isWishlisted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
            contentColor = if (hasPriceAlert) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            shape = CircleShape,
            modifier = Modifier
                .size(56.dp)
                .shadow(6.dp, CircleShape)
        ) {
            Icon(
                imageVector = if (hasPriceAlert) Icons.Default.Notifications else Icons.Outlined.NotificationsNone,
                contentDescription = "Set Price Alert", 
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
