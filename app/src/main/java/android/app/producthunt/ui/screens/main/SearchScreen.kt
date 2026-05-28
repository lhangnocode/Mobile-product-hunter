package android.app.producthunt.ui.screens.main

import android.app.producthunt.data.remote.dto.ProductResponse
import android.app.producthunt.data.remote.dto.UserResponse
import android.app.producthunt.core.state.UiState
import android.app.producthunt.ui.components.SearchModeSwitch
import android.app.producthunt.ui.components.UserAvatar
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.theme.*
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
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    navController: NavController,
    initialQuery: String? = null,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: ProductViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf(initialQuery ?: "") }
    var searchMode by remember { mutableStateOf("AI Agent") }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Separate chat histories for Search and AI Agent
    val aiMessages = remember { mutableStateListOf<ChatMessage>() }
    val searchMessages = remember { mutableStateListOf<ChatMessage>() }

    // Use current messages based on mode
    val currentMessages = if (searchMode == "AI Agent") aiMessages else searchMessages

    // Observe backend search state
    val searchState by viewModel.searchState.collectAsState()
    val currentUserState by authViewModel.currentUserState.collectAsState()
    val currentUser = (currentUserState as? UiState.Success)?.data

    // Handle backend search results for "Search" mode
    LaunchedEffect(searchState) {
        if (searchMode == "Search" && searchState is UiState.Success) {
            val response = (searchState as UiState.Success).data
            if (searchMessages.isNotEmpty() && searchMessages.last().isLoading) {
                searchMessages.removeAt(searchMessages.lastIndex)
            }
            searchMessages.add(ChatMessage(
                text = "Tôi tìm thấy ${response.totalResults} sản phẩm cho bạn:",
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
                text = "Lỗi khi tìm kiếm: ${(searchState as UiState.Error).message}",
                isUser = false,
                showAgentHeader = false
            ))
        }
    }

    // Auto-scroll to bottom
    LaunchedEffect(currentMessages.size) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.lastIndex)
        }
    }

    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank() && aiMessages.isEmpty()) {
            scope.launch {
                performAiSearch(initialQuery, aiMessages)
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
                                scope.launch { performAiSearch(query, aiMessages) }
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
                            scope.launch { performAiSearch(queryToSend, aiMessages) }
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

private fun performNormalSearch(query: String, messages: SnapshotStateList<ChatMessage>, viewModel: ProductViewModel) {
    messages.add(ChatMessage(text = query, isUser = true))
    messages.add(ChatMessage(text = "", isUser = false, isLoading = true, showAgentHeader = false))
    viewModel.search(query)
}

private suspend fun performAiSearch(query: String, chatMessages: SnapshotStateList<ChatMessage>) {
    chatMessages.add(ChatMessage(text = query, isUser = true))
    chatMessages.add(ChatMessage(text = "", isUser = false, isLoading = true, showAgentHeader = true))
    
    delay(1500)
    if (chatMessages.isNotEmpty()) {
        chatMessages.removeAt(chatMessages.lastIndex)
    }
    
    if (query.lowercase().contains("compare") || query.lowercase().contains("so sánh")) {
        chatMessages.add(ChatMessage(
            text = "Dưới đây là bảng so sánh chi tiết giữa các sản phẩm bạn yêu cầu:",
            isUser = false,
            isComparison = true,
            showAgentHeader = true
        ))
    } else {
        chatMessages.add(ChatMessage(
            text = "Tôi đã tìm thấy một số lựa chọn tốt nhất cho \"$query\". Bạn có muốn xem lịch sử giá không?",
            isUser = false,
            showAgentHeader = true
        ))
    }
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
    val showAgentHeader: Boolean = true
)

@Composable
fun DiscoveryLanding(
    currentUser: UserResponse?,
    onSuggestionClick: (String) -> Unit,
) {
    val displayName = currentUser?.fullName?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: "there"

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
            text = "Hello, $displayName",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                brush = Brush.linearGradient(GreetingGradient)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Search for tech products or ask the AI agent\nto help you find the best deals and\ncomparisons.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(Modifier.height(48.dp))
        
        // 2x2 Grid for Suggestions
        val suggestions = listOf(
            "So sánh Samsung Tab S10 vs iPad Mini",
            "Laptop Gaming dưới 20M",
            "iPhone 15 vs Galaxy S24",
            "Deals hot hôm nay"
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            suggestions.chunked(2).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { suggestion ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                                .clickable { onSuggestionClick(suggestion) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopStart) {
                                Column {
                                    val title = if (suggestion.startsWith("So sánh")) "So sánh" else suggestion.split(" ").take(2).joinToString(" ")
                                    val desc = if (suggestion.startsWith("So sánh")) suggestion.removePrefix("So sánh ") else suggestion.split(" ").drop(2).joinToString(" ")
                                    
                                    Text(text = title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Text(text = desc, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
    onProductClick: (ProductResponse) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(messages) { message ->
            when {
                message.isComparison -> AIComparisonMessage(message.text)
                message.isProductList -> ProductListMessage(message, onProductClick)
                message.isDetail -> InlineProductDetail(message)
                else -> ChatBubble(message)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        if (!message.isUser && !message.isLoading && message.showAgentHeader) {
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
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = PH_Primary)
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
fun ProductListMessage(message: ChatMessage, onProductClick: (ProductResponse) -> Unit) {
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
                            Text(product.productName ?: "Sản phẩm", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Xem giá ngay", color = PH_Primary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text("${product.brand ?: "Chính hãng"} • Xem chi tiết", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
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
    val product = message.productData
    Column(modifier = Modifier.fillMaxWidth()) {
        if (message.showAgentHeader) AgentHeader()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = product?.productName ?: "Chi tiết sản phẩm", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
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
                
                Text("Lịch sử giá (30 ngày)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)))
                
                Spacer(Modifier.height(16.dp))
                Text("Thông tin: ${product?.brand ?: "Chính hãng"}. Sản phẩm này đang có giá tốt tại Shopee và Lazada.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = PH_Primary)) { 
                         Text("Theo dõi giá") 
                    }
                    Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("Đến cửa hàng", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun AgentHeader() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PH_Primary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text("AI Agent", style = MaterialTheme.typography.labelSmall, color = PH_Primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AIComparisonMessage(text: String) {
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
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Lưu so sánh") }
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { 
                    Text(
                        if (mode == "AI Agent") "Ask anything about products..." else "Tìm kiếm sản phẩm...",
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
                    IconButton(
                        onClick = onSend,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFCA5A5))
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF450A0A), modifier = Modifier.size(20.dp))
                    }
                }
            )
        }
    }
}
