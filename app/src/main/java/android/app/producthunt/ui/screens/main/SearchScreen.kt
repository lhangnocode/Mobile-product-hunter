package android.app.producthunt.ui.screens.main

import android.app.producthunt.core.agent.AgentProductSummary
import android.app.producthunt.core.agent.AgentTrendingItemSummary
import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.dto.ProductResponse
import android.app.producthunt.data.remote.dto.UserResponse
import android.app.producthunt.ui.components.SearchModeSwitch
import android.app.producthunt.ui.components.UserAvatar
import android.app.producthunt.ui.i18n.LocalAppStrings
import android.app.producthunt.ui.i18n.LocalLanguageMode
import android.app.producthunt.ui.i18n.formatPriceFromVnd
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.theme.*
import android.app.producthunt.ui.viewmodel.AgentSearchViewModel
import android.app.producthunt.ui.viewmodel.AuthViewModel
import android.app.producthunt.ui.viewmodel.ProductViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    navController: NavController,
    initialQuery: String? = null,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: ProductViewModel = hiltViewModel(),
    agentViewModel: AgentSearchViewModel = hiltViewModel(),
) {
    val strings = LocalAppStrings.current
    var searchQuery by remember { mutableStateOf(initialQuery ?: "") }
    var searchMode by remember { mutableStateOf("Search") }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Separate chat histories for Search and AI Agent
    val searchMessages = remember { mutableStateListOf<ChatMessage>() }

    // Observe backend search state
    val searchState by viewModel.searchState.collectAsState()
    val currentUserState by authViewModel.currentUserState.collectAsState()
    val currentUser = (currentUserState as? UiState.Success)?.data
    val agentState by agentViewModel.uiState.collectAsState()

    // Use current messages based on mode
    val currentMessages = if (searchMode == "AI Agent") agentState.messages else searchMessages

    // Handle backend search results for "Search" mode
    LaunchedEffect(searchState) {
        if (searchMode == "Search" && searchState is UiState.Success) {
            val response = (searchState as UiState.Success).data
            if (searchMessages.isNotEmpty() && searchMessages.last().isLoading) {
                searchMessages.removeAt(searchMessages.lastIndex)
            }
            searchMessages.add(ChatMessage(
                text = strings.searchResultsFound(response.totalResults),
                isUser = false,
                isProductList = true,
                productList = response.data,
                showAgentHeader = false 
            ))
        } else if (searchMode == "Search" && searchState is UiState.Error) {
            if (searchMessages.isNotEmpty() && searchMessages.last().isLoading) {
                searchMessages.removeAt(searchMessages.lastIndex)
            }
            searchMessages.add(ChatMessage(
                text = strings.searchError((searchState as UiState.Error).message),
                isUser = false,
                showAgentHeader = false
            ))
        }
    }

    // Auto-scroll to bottom
    LaunchedEffect(currentMessages.size, currentMessages.lastOrNull()?.text) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.lastIndex)
        }
    }

    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank() && agentState.messages.isEmpty()) {
            scope.launch {
                agentViewModel.sendQuery(initialQuery)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Sliding Mode Switch
            SearchModeSwitch(
                selectedMode = searchMode,
                onModeChange = { searchMode = it }
            )

            // 2. Content Area
            Box(modifier = Modifier.weight(1f)) {
                if (currentMessages.isEmpty()) {
                    DiscoveryLanding(
                        currentUser = currentUser,
                        onSuggestionClick = { query ->
                            searchQuery = query
                            if (searchMode == "AI Agent") {
                                scope.launch {
                                    agentViewModel.sendQuery(query)
                                }
                            } else {
                                performNormalSearch(query, searchMessages, viewModel)
                            }
                        }
                    )
                } else {
                    ConversationArea(
                        messages = currentMessages,
                        listState = listState,
                        onProductClick = { product ->
                            product.id?.let { id ->
                                val encodedImage = product.mainImageUrl?.let {
                                    URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
                                }
                                val route = buildString {
                                    append("${Route.PRODUCT_DETAIL}/$id")
                                    if (!encodedImage.isNullOrBlank()) append("?imageUrl=$encodedImage")
                                }
                                navController.navigate(route)
                            }
                        },
                        onAgentProductClick = { product ->
                            product.id?.let { id ->
                                val encodedImage = product.mainImageUrl?.let {
                                    URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
                                }
                                val route = buildString {
                                    append("${Route.PRODUCT_DETAIL}/$id")
                                    if (!encodedImage.isNullOrBlank()) append("?imageUrl=$encodedImage")
                                }
                                navController.navigate(route)
                            }
                        }
                    )
                }
            }

            // 3. Chat Input Area
            ChatInputArea(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                mode = searchMode,
                onSend = {
                    if (searchQuery.isNotBlank()) {
                        val queryToSend = searchQuery
                        searchQuery = ""
                        if (searchMode == "AI Agent") {
                            scope.launch {
                                agentViewModel.sendQuery(queryToSend)
                            }
                        } else {
                            performNormalSearch(queryToSend, searchMessages, viewModel)
                        }
                        focusManager.clearFocus()
                    }
                }
            )
        }
    }
}

private fun performNormalSearch(query: String, messages: MutableList<ChatMessage>, viewModel: ProductViewModel) {
    messages.add(ChatMessage(text = query, isUser = true))
    messages.add(ChatMessage(text = "", isUser = false, isLoading = true, showAgentHeader = false))
    viewModel.search(query)
}


data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false,
    val isComparison: Boolean = false,
    val isProductList: Boolean = false,
    val isDetail: Boolean = false,
    val productData: ProductResponse? = null,
    val productList: List<ProductResponse> = emptyList(),
    val compareItems: List<android.app.producthunt.data.remote.dto.SearchCompareItem> = emptyList(),
    val trendingItems: List<android.app.producthunt.data.remote.dto.TrendingDealResponse> = emptyList(),
    val agentProductList: List<AgentProductSummary> = emptyList(),
    val agentTrendingItems: List<AgentTrendingItemSummary> = emptyList(),
    val showAgentHeader: Boolean = true
)

private data class SearchSuggestion(
    val title: String,
    val description: String,
    val query: String,
)

@Composable
fun DiscoveryLanding(
    currentUser: UserResponse?,
    onSuggestionClick: (String) -> Unit,
) {
    val strings = LocalAppStrings.current
    val displayName = currentUser?.fullName?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: strings.fallbackUserName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        UserAvatar(
            name = currentUser?.fullName,
            email = currentUser?.email,
            size = 64.dp,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = strings.greeting(displayName),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                brush = Brush.linearGradient(GreetingGradient)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = strings.searchLandingSubtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(Modifier.height(48.dp))
        
        // 2x2 Grid for Suggestions
        val suggestions = listOf(
            SearchSuggestion(strings.suggestionCompareTitle, strings.suggestionCompareDescription, strings.suggestionCompareQuery),
            SearchSuggestion(strings.suggestionGamingTitle, strings.suggestionGamingDescription, strings.suggestionGamingQuery),
            SearchSuggestion(strings.suggestionPhoneTitle, strings.suggestionPhoneDescription, strings.suggestionPhoneQuery),
            SearchSuggestion(strings.suggestionDealsTitle, strings.suggestionDealsDescription, strings.suggestionDealsQuery),
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            suggestions.chunked(2).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { suggestion ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                                .clickable { onSuggestionClick(suggestion.query) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopStart) {
                                Column {
                                    Text(text = suggestion.title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Text(text = suggestion.description, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationArea(
    messages: List<ChatMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onProductClick: (ProductResponse) -> Unit,
    onAgentProductClick: (AgentProductSummary) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(messages) { message ->
            when {
                message.agentTrendingItems.isNotEmpty() -> AgentTrendingSummaryMessage(message)
                message.agentProductList.isNotEmpty() -> AgentProductSummaryListMessage(message, onAgentProductClick)
                message.compareItems.isNotEmpty() -> AgentCompareMessage(message)
                message.trendingItems.isNotEmpty() -> TrendingDealsMessage(message)
                message.isComparison -> AIComparisonMessage(message.text)
                message.isProductList -> ProductListMessage(message, onProductClick)
                message.isDetail -> InlineProductDetail(message)
                else -> ChatBubble(message)
            }
        }
    }
}

@Composable
fun AgentProductSummaryListMessage(
    message: ChatMessage,
    onProductClick: (AgentProductSummary) -> Unit,
) {
    val strings = LocalAppStrings.current
    Column(modifier = Modifier.fillMaxWidth()) {
        if (message.showAgentHeader) AgentHeader()
        Text(text = message.text, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            message.agentProductList.forEach { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = product.id != null) { onProductClick(product) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (product.mainImageUrl != null) {
                                AsyncImage(
                                    model = product.mainImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.productName ?: strings.trackedProduct,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                product.brand ?: strings.officialProductInfo,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AgentTrendingSummaryMessage(message: ChatMessage) {
    val strings = LocalAppStrings.current
    val languageMode = LocalLanguageMode.current
    Column(modifier = Modifier.fillMaxWidth()) {
        if (message.showAgentHeader) AgentHeader()
        val title = message.text.ifBlank { strings.todayHotDeals }
        Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            message.agentTrendingItems.forEach { deal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AgentSummaryImage(deal.mainImageUrl)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = deal.productName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.discountPercent(deal.discountPercent?.toInt() ?: 0),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = formatPriceFromVnd(deal.currentPrice, languageMode),
                            color = PH_Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgentSummaryImage(imageUrl: String?) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        if (!message.isUser && message.showAgentHeader) {
            AgentHeader()
        }
        
        Surface(
            color = if (message.isUser) PH_Primary else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            )
        ) {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                if (message.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = PH_Primary,
                        )
                        if (message.text.isNotBlank()) {
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = message.text,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                } else {
                    Text(
                        text = message.text,
                        color = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun AgentCompareMessage(message: ChatMessage) {
    val strings = LocalAppStrings.current
    Column(modifier = Modifier.fillMaxWidth()) {
        if (message.showAgentHeader) AgentHeader()
        val total = message.compareItems.size
        val title = if (message.text.isNotBlank()) message.text else strings.compareProducts(total)
        Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            message.compareItems.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.mainImageUrl != null) {
                                AsyncImage(
                                    model = item.mainImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.platforms(item.platforms.size),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        if (item.lowestPrice != null) {
                            Text(
                                text = "${item.lowestPrice}",
                                color = PH_Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingDealsMessage(message: ChatMessage) {
    val strings = LocalAppStrings.current
    val languageMode = LocalLanguageMode.current
    Column(modifier = Modifier.fillMaxWidth()) {
        if (message.showAgentHeader) AgentHeader()
        val title = if (message.text.isNotBlank()) message.text else strings.todayHotDeals
        Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            message.trendingItems.forEach { deal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (deal.mainImageUrl != null) {
                                AsyncImage(
                                    model = deal.mainImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = deal.productName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.discountPercent(deal.discountPercent?.toInt() ?: 0),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = formatPriceFromVnd(deal.currentPrice, languageMode),
                            color = PH_Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductListMessage(message: ChatMessage, onProductClick: (ProductResponse) -> Unit) {
    val strings = LocalAppStrings.current
    Column(modifier = Modifier.fillMaxWidth()) {
        if (message.showAgentHeader) AgentHeader()
        Text(text = message.text, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
        Spacer(Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            message.productList.forEach { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(product) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (product.mainImageUrl != null) {
                                AsyncImage(
                                    model = product.mainImageUrl, 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.productName ?: strings.trackedProduct, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(strings.viewPriceNow, color = PH_Primary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text("${product.brand ?: strings.officialProductInfo} • ${strings.viewDetails}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InlineProductDetail(message: ChatMessage) {
    val strings = LocalAppStrings.current
    val product = message.productData
    Column(modifier = Modifier.fillMaxWidth()) {
        if (message.showAgentHeader) AgentHeader()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = product?.productName ?: strings.productDetails, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    if (product?.mainImageUrl != null) {
                        AsyncImage(
                            model = product.mainImageUrl, 
                            contentDescription = null, 
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(Icons.Default.Laptop, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))
                
                Text(strings.priceHistory30Days, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)))
                
                Spacer(Modifier.height(16.dp))
                Text(strings.productInfo(product?.brand ?: strings.officialProductInfo), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = PH_Primary)) { 
                         Text(strings.followPrice) 
                    }
                    Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(strings.goToStore, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun AgentHeader() {
    val strings = LocalAppStrings.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PH_Primary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(strings.aiAgent, style = MaterialTheme.typography.labelSmall, color = PH_Primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AIComparisonMessage(text: String) {
    val strings = LocalAppStrings.current
    Column(modifier = Modifier.fillMaxWidth()) {
        AgentHeader()
        Text(text = text, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ProductCompItem("Samsung", "Tab S10", Color.Cyan)
                    Text("vs.", modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold, color = PH_Primary)
                    ProductCompItem("iPad Mini", "(6th Gen)", Color.Magenta)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                PriceRowItem("Shopee", "17.990k", "14.490k")
                PriceRowItem("Lazada", "18.190k", "14.790k")
                PriceRowItem("Tiki", "17.890k", "14.390k")
                Spacer(Modifier.height(16.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text(strings.saveComparison) }
            }
        }
    }
}

@Composable
fun ProductCompItem(brand: String, model: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Box(modifier = Modifier.size(70.dp).background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
             Icon(Icons.Default.TabletAndroid, contentDescription = null, tint = color)
        }
        Spacer(Modifier.height(8.dp))
        Text(brand, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(model, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

@Composable
fun PriceRowItem(store: String, p1: String, p2: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(store, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, modifier = Modifier.width(60.dp))
        Text(p1, color = PH_Primary, fontWeight = FontWeight.Bold)
        Text(p2, color = PH_Primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChatInputArea(value: String, onValueChange: (String) -> Unit, mode: String, onSend: () -> Unit) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { 
                    Text(
                        if (mode == "AI Agent") strings.aiAgentPlaceholder else strings.normalSearchPlaceholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = PH_Primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(26.dp),
                trailingIcon = {
                    val submitIcon = if (mode == "Search") Icons.Default.Search else Icons.Default.ArrowUpward
                    IconButton(
                        onClick = onSend,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFCA5A5))
                    ) {
                        Icon(submitIcon, contentDescription = null, tint = Color(0xFF450A0A), modifier = Modifier.size(20.dp))
                    }
                }
            )
        }
    }
}
