package android.app.producthunt.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class PriceAlert(
    val id: Int,
    val name: String,
    val subtitle: String,
    val imageUrl: String?,
    val currentPrice: Double?,
    val targetPrice: Double,
    val statusText: String,
    val targetReached: Boolean,
    val placeholderColor: Color,
    val placeholderIcon: ImageVector,
)
