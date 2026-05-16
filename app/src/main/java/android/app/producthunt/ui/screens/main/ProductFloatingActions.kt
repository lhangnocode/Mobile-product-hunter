package android.app.producthunt.ui.screens.main

import android.app.producthunt.ui.theme.PH_OnSurface
import android.app.producthunt.ui.theme.PH_Primary
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProductFloatingActions(
    productId: String,
    onWishlistClick: () -> Unit,
    onAlertClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Nút Yêu thích - Giao diện sáng, đổ bóng nhẹ
        FloatingActionButton(
            onClick = onWishlistClick,
            containerColor = Color.White,
            contentColor = Color(0xFFE91E63), // Màu hồng đỏ cho nút yêu thích
            shape = CircleShape,
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite, 
                contentDescription = "Add to Wishlist", 
                modifier = Modifier.size(24.dp)
            )
        }

        // Nút Báo giá - Sử dụng màu Primary rực rỡ
        FloatingActionButton(
            onClick = onAlertClick,
            containerColor = PH_Primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications, 
                contentDescription = "Set Price Alert", 
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
