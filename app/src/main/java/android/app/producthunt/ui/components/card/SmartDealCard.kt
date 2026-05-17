package android.app.producthunt.ui.components.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.producthunt.ui.theme.*
import android.app.producthunt.R
import coil.compose.AsyncImage

@Composable
fun SmartDealCard(
    title: String,
    currentPrice: String,
    originalPrice: String? = null,
    targetPrice: String,
    imageUrl: String? = null,
    badgeText: String? = null,
    statusLabel: String? = null,
    statusColor: Color = PH_Status_Warning_Text,
    statusBgColor: Color = PH_Status_Warning_Bg,
    isMatched: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PH_Background)
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.product_logo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(12.dp).alpha(0.3f),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = PH_OnSurface,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentPrice,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PH_Primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Theo dõi giá",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    IconButton(onClick = onRemoveClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = PHIcons.Delete,
                            contentDescription = "Remove",
                            modifier = Modifier.size(18.dp),
                            tint = PH_Status_Error_Text.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
