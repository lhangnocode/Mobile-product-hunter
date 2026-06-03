package android.app.producthunt.ui.i18n

import android.app.producthunt.data.local.LanguageMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

private const val VND_PER_USD = 25_000.0

val LocalLanguageMode = staticCompositionLocalOf { LanguageMode.VIETNAMESE }
val LocalAppStrings = staticCompositionLocalOf { AppStrings.vi }

@Composable
fun ProductHunterLocale(
    languageMode: LanguageMode,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLanguageMode provides languageMode,
        LocalAppStrings provides AppStrings.forMode(languageMode),
        content = content,
    )
}

@Immutable
data class AppStrings(
    val feed: String,
    val search: String,
    val wishlist: String,
    val alerts: String,
    val priceAlerts: String,
    val trendingDeals: String,
    val seeAll: String,
    val profile: String,
    val history: String,
    val details: String,
    val appInformation: String,
    val aiAgent: String,
    val fallbackUserName: String,
    val greeting: (String) -> String,
    val searchLandingSubtitle: String,
    val normalSearchPlaceholder: String,
    val aiAgentPlaceholder: String,
    val searchResultsFound: (Int) -> String,
    val searchError: (String) -> String,
    val suggestionCompareTitle: String,
    val suggestionCompareDescription: String,
    val suggestionCompareQuery: String,
    val suggestionGamingTitle: String,
    val suggestionGamingDescription: String,
    val suggestionGamingQuery: String,
    val suggestionPhoneTitle: String,
    val suggestionPhoneDescription: String,
    val suggestionPhoneQuery: String,
    val suggestionDealsTitle: String,
    val suggestionDealsDescription: String,
    val suggestionDealsQuery: String,
    val todayHotDeals: String,
    val discountPercent: (Int) -> String,
    val compareProducts: (Int) -> String,
    val viewPriceNow: String,
    val viewDetails: String,
    val productDetails: String,
    val priceHistory30Days: String,
    val productInfo: (String) -> String,
    val followPrice: String,
    val goToStore: String,
    val saveComparison: String,
    val officialProductInfo: String,
    val newSearch: String,
    val appearance: String,
    val darkMode: String,
    val language: String,
    val english: String,
    val vietnamese: String,
    val productHunterUser: String,
    val freePlan: String,
    val bestPrice: String,
    val marketplacePrices: String,
    val priceHistory: String,
    val preparingPriceData: String,
    val notEnoughChartData: String,
    val priceAlertSaved: String,
    val priceAlertTitle: String,
    val priceAlertDescription: (String) -> String,
    val confirmPriceAlert: String,
    val close: String,
    val inStock: String,
    val outOfStock: String,
    val platforms: (Int) -> String,
    val lowest: String,
    val averagePrice: String,
    val bestPriceInStock: String,
    val goToShop: String,
    val current: String,
    val target: String,
    val checking: String,
    val trackedProduct: String,
    val activeAlert: String,
    val paused: String,
    val productFallback: (String) -> String,
    val targetReached: String,
    val waitingForPriceDrop: String,
    val priceAlertSaveSuccess: String,
    val priceCheckStarted: String,
    val clearedAlerts: (Int) -> String,
    val noAlertsToClear: String,
    val clearAll: String,
    val runCheck: String,
    val activePrecisionTracking: String,
    val noActivePriceAlerts: String,
    val emptyWishlist: String,
    val upcomingAlerts: String,
    val noScheduledDrops: String,
    val noScheduledDropsDescription: String,
    val browsePriceHistoryTrends: String,
) {
    companion object {
        val en = AppStrings(
            feed = "Feed",
            search = "Search",
            wishlist = "Wishlist",
            alerts = "Alerts",
            priceAlerts = "Price Alerts",
            trendingDeals = "Trending Deals",
            seeAll = "See all",
            profile = "Profile",
            history = "History",
            details = "Details",
            appInformation = "App Information",
            aiAgent = "AI Agent",
            fallbackUserName = "there",
            greeting = { name -> "Hello, $name" },
            searchLandingSubtitle = "Search for tech products or ask the AI agent\nto help you find the best deals and\ncomparisons.",
            normalSearchPlaceholder = "Search products...",
            aiAgentPlaceholder = "Ask anything about products...",
            searchResultsFound = { count -> "I found $count products for you:" },
            searchError = { message -> "Search error: $message" },
            suggestionCompareTitle = "Compare",
            suggestionCompareDescription = "Samsung Tab S10 vs iPad Mini",
            suggestionCompareQuery = "Compare Samsung Tab S10 vs iPad Mini",
            suggestionGamingTitle = "Gaming laptop",
            suggestionGamingDescription = "under $800",
            suggestionGamingQuery = "Gaming laptop under $800",
            suggestionPhoneTitle = "iPhone 15",
            suggestionPhoneDescription = "vs Galaxy S24",
            suggestionPhoneQuery = "iPhone 15 vs Galaxy S24",
            suggestionDealsTitle = "Hot deals",
            suggestionDealsDescription = "today",
            suggestionDealsQuery = "Hot deals today",
            todayHotDeals = "Today hot deals",
            discountPercent = { percent -> "$percent% off" },
            compareProducts = { count -> "Compare $count products" },
            viewPriceNow = "View prices",
            viewDetails = "View details",
            productDetails = "Product details",
            priceHistory30Days = "Price history (30 days)",
            productInfo = { brand -> "Info: $brand. This product has good prices at Shopee and Lazada." },
            followPrice = "Track price",
            goToStore = "Go to store",
            saveComparison = "Save comparison",
            officialProductInfo = "Official product",
            newSearch = "New Search",
            appearance = "Appearance",
            darkMode = "Dark Mode",
            language = "Language",
            english = "English",
            vietnamese = "Vietnamese",
            productHunterUser = "Product Hunter User",
            freePlan = "Free Plan",
            bestPrice = "BEST PRICE",
            marketplacePrices = "Marketplace prices",
            priceHistory = "Price history",
            preparingPriceData = "Preparing price data...",
            notEnoughChartData = "Not enough data to draw the chart",
            priceAlertSaved = "Price alert saved",
            priceAlertTitle = "PRICE ALERT",
            priceAlertDescription = { "Tracking $it. We will email you when the price drops to this target." },
            confirmPriceAlert = "CONFIRM PRICE ALERT",
            close = "Close",
            inStock = "In stock",
            outOfStock = "Out of stock",
            platforms = { count -> "$count platforms" },
            lowest = "Lowest",
            averagePrice = "Average price",
            bestPriceInStock = "Best price - In stock",
            goToShop = "Go to shop",
            current = "CURRENT",
            target = "TARGET",
            checking = "Checking",
            trackedProduct = "Tracked product",
            activeAlert = "Active alert",
            paused = "Paused",
            productFallback = { id -> "Product ${id.take(8)}" },
            targetReached = "Target reached!",
            waitingForPriceDrop = "Waiting for price drop",
            priceAlertSaveSuccess = "Price alert saved",
            priceCheckStarted = "Price check started",
            clearedAlerts = { count -> "Cleared $count price alerts" },
            noAlertsToClear = "No price alerts to clear",
            clearAll = "Clear\nAll",
            runCheck = "Run\nCheck",
            activePrecisionTracking = "* ACTIVE PRECISION TRACKING",
            noActivePriceAlerts = "No active price alerts",
            emptyWishlist = "Your wishlist is empty",
            upcomingAlerts = "- UPCOMING ALERTS",
            noScheduledDrops = "No Scheduled Drops",
            noScheduledDropsDescription = "You do not have any seasonal or recurring alerts set up. Track historical sales to anticipate the next big dip.",
            browsePriceHistoryTrends = "Browse Price History Trends",
        )

        val vi = AppStrings(
            feed = "Bảng tin",
            search = "Tìm kiếm",
            wishlist = "Yêu thích",
            alerts = "Cảnh báo",
            priceAlerts = "Cảnh báo giá",
            trendingDeals = "Deal nổi bật",
            seeAll = "Xem tất cả",
            profile = "Hồ sơ",
            history = "Lịch sử",
            details = "Chi tiết",
            appInformation = "Thông tin ứng dụng",
            aiAgent = "Trợ lý AI",
            fallbackUserName = "bạn",
            greeting = { name -> "Xin chào, $name" },
            searchLandingSubtitle = "Tìm sản phẩm công nghệ hoặc hỏi trợ lý AI\nđể tìm ưu đãi và so sánh tốt nhất.",
            normalSearchPlaceholder = "Tìm kiếm sản phẩm...",
            aiAgentPlaceholder = "Hỏi bất kỳ điều gì về sản phẩm...",
            searchResultsFound = { count -> "Tôi tìm thấy $count sản phẩm cho bạn:" },
            searchError = { message -> "Lỗi khi tìm kiếm: $message" },
            suggestionCompareTitle = "So sánh",
            suggestionCompareDescription = "Samsung Tab S10 vs iPad Mini",
            suggestionCompareQuery = "So sánh Samsung Tab S10 vs iPad Mini",
            suggestionGamingTitle = "Laptop Gaming",
            suggestionGamingDescription = "dưới 20M",
            suggestionGamingQuery = "Laptop Gaming dưới 20M",
            suggestionPhoneTitle = "iPhone 15",
            suggestionPhoneDescription = "vs Galaxy S24",
            suggestionPhoneQuery = "iPhone 15 vs Galaxy S24",
            suggestionDealsTitle = "Deals hot",
            suggestionDealsDescription = "hôm nay",
            suggestionDealsQuery = "Deals hot hôm nay",
            todayHotDeals = "Deals hot hôm nay",
            discountPercent = { percent -> "Giảm $percent%" },
            compareProducts = { count -> "So sánh $count sản phẩm" },
            viewPriceNow = "Xem giá ngay",
            viewDetails = "Xem chi tiết",
            productDetails = "Chi tiết sản phẩm",
            priceHistory30Days = "Lịch sử giá (30 ngày)",
            productInfo = { brand -> "Thông tin: $brand. Sản phẩm này đang có giá tốt tại Shopee và Lazada." },
            followPrice = "Theo dõi giá",
            goToStore = "Đến cửa hàng",
            saveComparison = "Lưu so sánh",
            officialProductInfo = "Thông tin chính hãng",
            newSearch = "Tìm kiếm mới",
            appearance = "Giao diện",
            darkMode = "Chế độ tối",
            language = "Ngôn ngữ",
            english = "Tiếng Anh",
            vietnamese = "Tiếng Việt",
            productHunterUser = "Người dùng Product Hunter",
            freePlan = "Gói miễn phí",
            bestPrice = "GIÁ TỐT NHẤT",
            marketplacePrices = "Giá tại các sàn TMĐT",
            priceHistory = "Lịch sử biến động giá",
            preparingPriceData = "Đang tổng hợp dữ liệu giá...",
            notEnoughChartData = "Chưa đủ dữ liệu vẽ biểu đồ",
            priceAlertSaved = "Đã đặt cảnh báo giá",
            priceAlertTitle = "CẢNH BÁO GIÁ",
            priceAlertDescription = { "Theo dõi $it. Chúng tôi sẽ gửi email ngay khi giá giảm xuống mức này." },
            confirmPriceAlert = "XÁC NHẬN ĐẶT THÔNG BÁO",
            close = "Đóng",
            inStock = "Còn hàng",
            outOfStock = "Hết hàng",
            platforms = { count -> "$count sàn" },
            lowest = "Thấp nhất",
            averagePrice = "Giá trung bình",
            bestPriceInStock = "Giá tốt nhất - Còn hàng",
            goToShop = "Đi đến shop",
            current = "HIỆN TẠI",
            target = "MỤC TIÊU",
            checking = "Đang kiểm tra",
            trackedProduct = "Sản phẩm đang theo dõi",
            activeAlert = "Đang hoạt động",
            paused = "Tạm dừng",
            productFallback = { id -> "Sản phẩm ${id.take(8)}" },
            targetReached = "Đã đạt giá mục tiêu!",
            waitingForPriceDrop = "Đang chờ giảm giá",
            priceAlertSaveSuccess = "Đã lưu cảnh báo giá",
            priceCheckStarted = "Đã bắt đầu kiểm tra giá",
            clearedAlerts = { count -> "Đã xóa $count cảnh báo giá" },
            noAlertsToClear = "Không có cảnh báo giá để xóa",
            clearAll = "Xóa\ntất cả",
            runCheck = "Kiểm\ntra",
            activePrecisionTracking = "* ĐANG THEO DÕI CHÍNH XÁC",
            noActivePriceAlerts = "Không có cảnh báo giá đang hoạt động",
            emptyWishlist = "Danh sách yêu thích đang trống",
            upcomingAlerts = "- CẢNH BÁO SẮP TỚI",
            noScheduledDrops = "Không có lịch giảm giá",
            noScheduledDropsDescription = "Bạn chưa thiết lập cảnh báo theo mùa hoặc định kỳ. Hãy theo dõi lịch sử giá để dự đoán lần giảm tiếp theo.",
            browsePriceHistoryTrends = "Xem xu hướng lịch sử giá",
        )

        fun forMode(mode: LanguageMode): AppStrings =
            if (mode == LanguageMode.ENGLISH) en else vi
    }
}

fun formatPriceFromVnd(vnd: Double, languageMode: LanguageMode): String =
    if (languageMode == LanguageMode.ENGLISH) {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = java.util.Currency.getInstance("USD")
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }.format(vnd / VND_PER_USD)
    } else {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).apply {
            maximumFractionDigits = 0
        }.format(vnd) + " đ"
    }

fun displayPriceValueFromVnd(vnd: Double, languageMode: LanguageMode): Double =
    if (languageMode == LanguageMode.ENGLISH) vnd / VND_PER_USD else vnd

fun targetInputValueFromVnd(vnd: Double?, languageMode: LanguageMode): String =
    vnd?.let {
        val displayValue = displayPriceValueFromVnd(it, languageMode).roundToLong()
        formatDigitGroups(displayValue.toString())
    } ?: ""

fun targetInputToVnd(input: String, languageMode: LanguageMode): Double? {
    val value = input.filter(Char::isDigit).toDoubleOrNull() ?: return null
    return if (languageMode == LanguageMode.ENGLISH) value * VND_PER_USD else value
}

fun formatDigitGroups(raw: String): String {
    val digits = raw.filter(Char::isDigit)
    if (digits.isBlank()) return ""
    return digits.reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}

fun currencySuffix(languageMode: LanguageMode): String =
    if (languageMode == LanguageMode.ENGLISH) "$" else "đ"

fun compactPriceFromDisplayValue(value: Float, languageMode: LanguageMode): String =
    if (languageMode == LanguageMode.ENGLISH) {
        when {
            value >= 1_000_000f -> String.format(Locale.US, "$%.1fM", value / 1_000_000f)
            value >= 1_000f -> String.format(Locale.US, "$%.0fK", value / 1_000f)
            else -> String.format(Locale.US, "$%.0f", value)
        }
    } else {
        when {
            value >= 1_000_000f -> String.format(Locale.US, "%.1fM", value / 1_000_000f)
            value >= 1_000f -> String.format(Locale.US, "%.0fK", value / 1_000f)
            else -> String.format(Locale.US, "%.0f", value)
        }
    }
