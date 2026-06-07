package android.app.producthunt.widget

import android.app.producthunt.MainActivity
import android.app.producthunt.R
import android.app.producthunt.core.notification.PriceAlertNotificationPayload
import android.app.producthunt.core.notification.putPriceAlertPayload
import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.dto.TrendingDealResponse
import android.app.producthunt.data.remote.dto.detailPlatformProductId
import android.app.producthunt.data.remote.dto.detailProductId
import android.app.producthunt.data.remote.dto.discountLabel
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.android.EntryPointAccessors
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

class DealsWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadDeals(context.applicationContext)

        provideContent {
            DealsWidgetContent(
                state = state,
                context = context.applicationContext,
            )
        }
    }

    private suspend fun loadDeals(context: Context): DealsWidgetState {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            DealsWidgetEntryPoint::class.java,
        )

        return when (val result = entryPoint.platformProductRepository().getTrending(WIDGET_DEAL_LIMIT)) {
            is UiState.Success -> DealsWidgetState.Loaded(result.data.take(WIDGET_DEAL_LIMIT))
            is UiState.Error -> DealsWidgetState.Error(result.message)
            else -> DealsWidgetState.Error("Deals are not ready yet.")
        }
    }

    private companion object {
        private const val WIDGET_DEAL_LIMIT = 5
    }
}

private sealed interface DealsWidgetState {
    data class Loaded(val deals: List<TrendingDealResponse>) : DealsWidgetState
    data class Error(val message: String) : DealsWidgetState
}

@Composable
private fun DealsWidgetContent(
    state: DealsWidgetState,
    context: Context,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.Background)
            .clickable(actionStartActivity(openAppIntent(context)))
            .padding(14.dp),
    ) {
        WidgetHeader()
        Spacer(modifier = GlanceModifier.height(10.dp))

        when (state) {
            is DealsWidgetState.Error -> WidgetMessage(state.message)
            is DealsWidgetState.Loaded -> {
                if (state.deals.isEmpty()) {
                    WidgetMessage("No deals right now. Check back soon.")
                } else {
                    LazyColumn(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight(),
                    ) {
                        itemsIndexed(state.deals) { index, deal ->
                            DealRow(
                                rank = index + 1,
                                deal = deal,
                                context = context,
                            )
                            if (index != state.deals.lastIndex) {
                                Spacer(modifier = GlanceModifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetHeader() {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "Deals Today",
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TextPrimary),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
            Text(
                text = "Trending price drops",
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TextSecondary),
                    fontSize = 14.sp,
                ),
                maxLines = 1,
            )
        }
        Image(
            provider = ImageProvider(R.drawable.ic_widget_reload),
            contentDescription = "Reload deals",
            modifier = GlanceModifier
                .background(ImageProvider(R.drawable.deals_widget_reload_background))
                .clickable(actionRunCallback<DealsWidgetRefreshAction>())
                .padding(8.dp)
                .width(34.dp)
                .height(34.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun DealRow(
    rank: Int,
    deal: TrendingDealResponse,
    context: Context,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ImageProvider(R.drawable.deals_widget_product_background))
            .clickable(actionStartActivity(openDealIntent(context, deal)))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .width(32.dp)
                .height(32.dp)
                .background(ImageProvider(R.drawable.deals_widget_rank_background)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rank.toString(),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.Primary),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = deal.productName,
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TextPrimary),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            Text(
                text = deal.platformName(),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TextSecondary),
                    fontSize = 12.sp,
                ),
                maxLines = 1,
            )
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = deal.currentPrice.toVndPrice(),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.Primary),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
            Text(
                text = deal.discountLabel() ?: deal.originalPrice?.discountFrom(deal.currentPrice) ?: "View",
                style = TextStyle(
                    color = ColorProvider(WidgetColors.TextSecondary),
                    fontSize = 12.sp,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun WidgetMessage(message: String) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(86.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = TextStyle(
                color = ColorProvider(WidgetColors.TextSecondary),
                fontSize = 14.sp,
            ),
            maxLines = 3,
        )
    }
}

private fun openAppIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

private fun openDealIntent(
    context: Context,
    deal: TrendingDealResponse,
): Intent =
    openAppIntent(context).putPriceAlertPayload(
        PriceAlertNotificationPayload(
            productId = deal.detailProductId,
            platformProductId = deal.detailPlatformProductId,
            imageUrl = deal.mainImageUrl,
            productName = deal.productName,
        )
    )

private fun TrendingDealResponse.platformName(): String =
    when (platformId) {
        1 -> "Shopee"
        2 -> "Lazada"
        3 -> "Tiki"
        7 -> "FPT Shop"
        8 -> "Phong Vu"
        9 -> "CellphoneS"
        null -> "Marketplace"
        else -> "Platform $platformId"
    }

private fun Double.toVndPrice(): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(this)

private fun Double.discountFrom(currentPrice: Double): String {
    if (this <= 0.0 || this <= currentPrice) return "View"

    val discount = ((this - currentPrice) / this * 100).roundToInt()
    return if (discount > 0) "-$discount%" else "View"
}

private object WidgetColors {
    val Background = Color(0xFFFFF8F1)
    val Primary = Color(0xFFFF8A50)
    val TextPrimary = Color(0xFF2D2D2D)
    val TextSecondary = Color(0xFF76716D)
}
