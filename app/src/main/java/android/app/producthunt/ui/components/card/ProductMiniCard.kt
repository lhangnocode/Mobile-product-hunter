package android.app.producthunt.ui.components.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.producthunt.ui.theme.*

@Composable
fun ProductMiniCard(
    title: String,
    currentPrice: String,
    targetPrice: String,
    progress: Float,
    statusText: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    isWishlisted: Boolean = false,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Product Image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Placeholder for image
                    Text(
                        text = "Img",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Product Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onWishlistClick,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isWishlisted) PHIcons.Wishlist else PHIcons.WishlistOutlined,
                                    contentDescription = "Wishlist",
                                    tint = if (isWishlisted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = PHIcons.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = PHIcons.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "CURRENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentPrice,
                                style = PriceMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TARGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = PH_Price_Target.copy(alpha = 0.8f)
                            )
                            Text(
                                text = targetPrice,
                                style = PriceMedium,
                                color = PH_Price_Target
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PH_Progress_Bar,
                trackColor = PH_Progress_Bg
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Text
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Curved arrow icon could be added here
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = PH_Progress_Bar,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductMiniCardPreview() {
    AndroidAppProductHuntTheme {
        Box(modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.background)) {
            ProductMiniCard(
                title = "Sony WH-1000XM5 Headphones",
                currentPrice = "$348.00",
                targetPrice = "$299.00",
                progress = 0.7f,
                statusText = "↘ 14% away from target price"
            )
        }
    }
}
