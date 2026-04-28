package android.app.producthunt.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.producthunt.ui.theme.*

@Composable
fun SmartDealCard(
    title: String,
    currentPrice: String,
    originalPrice: String? = null,
    targetPrice: String,
    badgeText: String? = null,
    statusLabel: String? = null,
    statusColor: Color = PH_Status_Warning_Text,
    statusBgColor: Color = PH_Status_Warning_Bg,
    isMatched: Boolean = false,
    modifier: Modifier = Modifier,
    onRemoveClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PH_Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image with Badge
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                // Placeholder for image
                Text(
                    text = "Img",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
                
                if (badgeText != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = PH_Status_Error_Text,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
                        color = PH_OnSurface,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (statusLabel != null) {
                        Surface(
                            color = statusBgColor,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentPrice,
                        style = PriceLarge,
                        color = PH_Price_Current
                    )
                    if (originalPrice != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = originalPrice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PH_OnSurface.copy(alpha = 0.4f),
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Target: $targetPrice",
                            style = MaterialTheme.typography.bodySmall,
                            color = PH_OnSurface.copy(alpha = 0.6f)
                        )
                        if (isMatched) {
                            Text(
                                text = "Matched Target!",
                                style = MaterialTheme.typography.labelSmall,
                                color = PH_Status_Success_Text,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    TextButton(
                        onClick = onRemoveClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PHIcons.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = PH_Status_Error_Text.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Remove",
                                style = MaterialTheme.typography.labelSmall,
                                color = PH_Status_Error_Text.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SmartDealCardPreview() {
    AndroidAppProductHuntTheme {
        Box(modifier = Modifier.padding(16.dp).background(PH_Background)) {
            SmartDealCard(
                title = "Nike Air Zoom",
                currentPrice = "$120.00",
                originalPrice = "$150.00",
                targetPrice = "$115.00",
                badgeText = "-15%",
                statusLabel = "PRICE DROPPED",
                statusColor = PH_Status_Error_Text,
                statusBgColor = PH_Status_Error_Bg
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SmartDealCardMatchedPreview() {
    AndroidAppProductHuntTheme {
        Box(modifier = Modifier.padding(16.dp).background(PH_Background)) {
            SmartDealCard(
                title = "Yeezy Boost",
                currentPrice = "$310.00",
                originalPrice = "$380.00",
                targetPrice = "$310.00",
                badgeText = "NEW LOW",
                statusLabel = "BEST PRICE EVER",
                statusColor = PH_Status_Success_Text,
                statusBgColor = PH_Status_Success_Bg,
                isMatched = true
            )
        }
    }
}
