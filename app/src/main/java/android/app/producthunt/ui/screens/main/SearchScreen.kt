package android.app.producthunt.ui.screens.main

import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.local.LanguageMode
import android.app.producthunt.data.remote.dto.ProductResponse
import android.app.producthunt.data.remote.dto.UserResponse
import android.app.producthunt.ui.components.SearchModeSwitch
import android.app.producthunt.ui.components.UserAvatar
import android.app.producthunt.ui.i18n.LocalAppStrings
import android.app.producthunt.ui.i18n.LocalLanguageMode
import android.app.producthunt.ui.i18n.formatPriceFromVnd
import android.app.producthunt.ui.navigation.Route
import android.app.producthunt.ui.state.AiAssistantMessage
import android.app.producthunt.ui.theme.*
import android.app.producthunt.ui.viewmodel.AiAssistantViewModel
import android.app.producthunt.ui.viewmodel.AuthViewModel
import android.app.producthunt.ui.viewmodel.ProductViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
    initialConversationId: String? = null,
    authViewModel: AuthViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel(),
    aiAssistantViewModel: AiAssistantViewModel = hiltViewModel(),
) {
    var searchMode by remember(initialConversationId, initialQuery) {
        val shouldOpenAgentMode =
            initialConversationId == NEW_AGENT_CONVERSATION_ID ||
                !initialConversationId.isNullOrBlank()

        mutableStateOf(
            if (shouldOpenAgentMode) {
                "AI Agent"
            } else {
                "Search"
            }
        )
    }
    val currentUserState by authViewModel.currentUserState.collectAsState()
    val currentUser = (currentUserState as? UiState.Success)?.data

    LaunchedEffect(initialConversationId) {
        when {
            initialConversationId == NEW_AGENT_CONVERSATION_ID -> {
                searchMode = "AI Agent"
                aiAssistantViewModel.startNewConversation()
            }
            !initialConversationId.isNullOrBlank() -> {
                searchMode = "AI Agent"
                aiAssistantViewModel.loadConversation(initialConversationId)
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
                if (searchMode == "AI Agent") {
                    AiAssistantPanel(
                        viewModel = aiAssistantViewModel,
                    )
                } else {
                    ProductSearchPanel(
                        navController = navController,
                        initialQuery = initialQuery,
                        currentUser = currentUser,
                        viewModel = productViewModel,
                    )
                }
            }
        }
    }
}

private const val NEW_AGENT_CONVERSATION_ID = "new"

@Composable
private fun ProductSearchPanel(
    navController: NavController,
    initialQuery: String?,
    currentUser: UserResponse?,
    viewModel: ProductViewModel,
) {
    val strings = LocalAppStrings.current
    val focusManager = LocalFocusManager.current
    var query by remember { mutableStateOf(initialQuery.orEmpty()) }
    val messages = remember { mutableStateListOf<ProductSearchMessage>() }
    val listState = rememberLazyListState()
    val searchState by viewModel.searchState.collectAsState()

    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank() && messages.isEmpty()) {
            performNormalSearch(initialQuery, messages, viewModel)
        }
    }

    LaunchedEffect(searchState) {
        if (searchState is UiState.Success) {
            val response = (searchState as UiState.Success).data
            if (messages.isNotEmpty() && messages.last().isLoading) {
                messages.removeAt(messages.lastIndex)
            }
            messages.add(
                ProductSearchMessage(
                    text = strings.searchResultsFound(response.totalResults),
                    isUser = false,
                    isProductList = true,
                    productList = response.data,
                    showAgentHeader = false,
                )
            )
        } else if (searchState is UiState.Error) {
            if (messages.isNotEmpty() && messages.last().isLoading) {
                messages.removeAt(messages.lastIndex)
            }
            messages.add(
                ProductSearchMessage(
                    text = strings.searchError((searchState as UiState.Error).message),
                    isUser = false,
                    showAgentHeader = false,
                )
            )
        }
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                ProductSearchLanding(
                    currentUser = currentUser,
                    onSuggestionClick = { suggestion ->
                        query = suggestion
                        performNormalSearch(suggestion, messages, viewModel)
                    },
                )
            } else {
                ConversationArea(
                    messages = messages,
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
                )
            }
        }
        ChatInputArea(
            value = query,
            onValueChange = { query = it },
            mode = "Search",
            onSend = {
                if (query.isNotBlank()) {
                    val queryToSend = query
                    query = ""
                    performNormalSearch(queryToSend, messages, viewModel)
                    focusManager.clearFocus()
                }
            },
        )
    }
}

@Composable
private fun AiAssistantPanel(
    viewModel: AiAssistantViewModel,
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.text) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            if (state.messages.isEmpty()) {
                AiAssistantLanding(
                    onSuggestionClick = { suggestion ->
                        query = ""
                        scope.launch {
                            viewModel.sendQuery(suggestion)
                        }
                    },
                )
            } else {
                AiAssistantConversationArea(
                    messages = state.messages,
                    listState = listState,
                )
            }
        }
        ChatInputArea(
            value = query,
            onValueChange = { query = it },
            mode = "AI Agent",
            onSend = {
                if (query.isNotBlank()) {
                    val queryToSend = query
                    query = ""
                    scope.launch {
                        viewModel.sendQuery(queryToSend)
                    }
                    focusManager.clearFocus()
                }
            },
        )
    }
}

private fun performNormalSearch(query: String, messages: MutableList<ProductSearchMessage>, viewModel: ProductViewModel) {
    messages.add(ProductSearchMessage(text = query, isUser = true))
    messages.add(ProductSearchMessage(text = "", isUser = false, isLoading = true, showAgentHeader = false))
    viewModel.search(query)
}


data class ProductSearchMessage(
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
    val showAgentHeader: Boolean = true
)

@Composable
fun ProductSearchLanding(
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
            text = when (LocalLanguageMode.current) {
                LanguageMode.ENGLISH -> "Search by product name to compare prices and stores."
                LanguageMode.VIETNAMESE -> "Tìm theo tên sản phẩm để xem giá và nơi bán."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(Modifier.height(48.dp))
        
        Text(
            text = when (LocalLanguageMode.current) {
                LanguageMode.ENGLISH -> "POPULAR SEARCHES"
                LanguageMode.VIETNAMESE -> "TÌM KIẾM PHỔ BIẾN"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.42f),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.8.sp,
        )
        Spacer(Modifier.height(14.dp))

        val suggestions = listOf("IPHONE 15", "SONY WH-1000XM5", "MACBOOK AIR M2", "AIRPODS PRO", "SAMSUNG S24 ULTRA")

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            suggestions.forEach { suggestion ->
                Surface(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .clickable { onSuggestionClick(suggestion) },
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                    ),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiAssistantLanding(
    onSuggestionClick: (String) -> Unit,
) {
    val languageMode = LocalLanguageMode.current
    val title = when (languageMode) {
        LanguageMode.ENGLISH -> "Ask the AI Agent"
        LanguageMode.VIETNAMESE -> "Hỏi Trợ lý AI"
    }
    val subtitle = when (languageMode) {
        LanguageMode.ENGLISH -> "Ask for buying advice, comparisons, best stores, or deal checks."
        LanguageMode.VIETNAMESE -> "Hỏi tư vấn mua hàng, so sánh, nơi bán tốt, hoặc kiểm tra giá có tốt không."
    }
    val suggestions = when (languageMode) {
        LanguageMode.ENGLISH -> listOf(
            "Where is the best place to buy iPhone 16?",
            "Compare iPhone 15 and Galaxy S24 for me",
            "Find good phone deals today",
            "Is this product price a good deal?",
        )
        LanguageMode.VIETNAMESE -> listOf(
            "Nơi nào mua iPhone 16 tốt nhất?",
            "So sánh iPhone 15 và Galaxy S24 giúp tôi",
            "Tìm deal điện thoại tốt hôm nay",
            "Giá sản phẩm này có đáng mua không?",
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PH_Primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = PH_Primary,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
        )
        Spacer(Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            suggestions.forEach { suggestion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PH_Primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = suggestion,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationArea(
    messages: List<ProductSearchMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onProductClick: (ProductResponse) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(messages) { message ->
            when {
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
fun AiAssistantConversationArea(
    messages: List<AiAssistantMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        items(messages) { message ->
            AiAssistantBubble(message)
        }
    }
}

@Composable
private fun AiAssistantBubble(message: AiAssistantMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
    ) {
        if (!message.isUser) {
            AgentHeader()
        }

        if (message.isUser) {
            Surface(
                color = PH_Primary,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 4.dp,
                ),
            ) {
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Text(
                        text = message.text,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                }
            }
        } else if (message.isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                MarkdownResponseText(message.text)
            }
        }
    }
}

@Composable
private fun MarkdownResponseText(text: String) {
    val lines = text.lines().collapseBlankLines()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        var index = 0
        while (index < lines.size) {
            val tableLines = lines.drop(index).takeWhile { it.trim().isMarkdownTableRow() }
            if (tableLines.size >= 2 && tableLines.getOrNull(1)?.trim()?.isMarkdownTableDivider() == true) {
                MarkdownTable(tableLines)
                index += tableLines.size
                continue
            }

            val rawLine = lines[index]
            val line = rawLine.trim()
            when {
                line.isBlank() -> Spacer(Modifier.height(2.dp))
                line.startsWith("### ") -> MarkdownLine(
                    text = line.removePrefix("### "),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                line.startsWith("## ") -> MarkdownLine(
                    text = line.removePrefix("## "),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                line.startsWith("# ") -> MarkdownLine(
                    text = line.removePrefix("# "),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                line.startsWith("- ") || line.startsWith("* ") -> MarkdownListLine(
                    marker = "•",
                    text = line.drop(2),
                )
                line.matches(Regex("""\d+\.\s+.*""")) -> {
                    val marker = line.substringBefore(" ") 
                    MarkdownListLine(
                        marker = marker,
                        text = line.substringAfter(" "),
                    )
                }
                else -> MarkdownLine(text = line)
            }
            index += 1
        }
    }
}

private fun List<String>.collapseBlankLines(): List<String> {
    val collapsed = mutableListOf<String>()
    var previousBlank = false
    forEach { line ->
        val isBlank = line.isBlank()
        if (!isBlank || !previousBlank) {
            collapsed.add(line)
        }
        previousBlank = isBlank
    }
    return collapsed
}

@Composable
private fun MarkdownTable(lines: List<String>) {
    val rows = lines
        .filterNot { it.trim().isMarkdownTableDivider() }
        .map { it.toMarkdownTableCells() }
        .filter { it.isNotEmpty() }

    if (rows.isEmpty()) return

    val columnCount = rows.maxOf { it.size }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
    ) {
        rows.forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(columnCount) { columnIndex ->
                    val cell = row.getOrNull(columnIndex).orEmpty()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (rowIndex == 0) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                    ) {
                        MarkdownLine(
                            text = cell,
                            fontSize = 13.sp,
                            fontWeight = if (rowIndex == 0) FontWeight.ExtraBold else FontWeight.Normal,
                        )
                    }
                }
            }
            if (rowIndex != rows.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                    thickness = 0.5.dp,
                )
            }
        }
    }
}

@Composable
private fun MarkdownListLine(
    marker: String,
    text: String,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = marker,
            modifier = Modifier.width(26.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )
        MarkdownLine(
            text = text,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MarkdownLine(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    val uriHandler = LocalUriHandler.current
    val linkColor = PH_Primary
    val annotatedText = remember(text, linkColor) {
        text.toMarkdownAnnotatedString(linkColor)
    }
    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = fontSize,
            fontWeight = fontWeight,
            lineHeight = 22.sp,
        ),
        onClick = { offset ->
            annotatedText
                .getStringAnnotations(tag = MARKDOWN_URL_TAG, start = offset, end = offset)
                .firstOrNull()
                ?.let { annotation ->
                    runCatching { uriHandler.openUri(annotation.item.ensureUrlScheme()) }
                }
        },
    )
}

private fun String.isMarkdownTableRow(): Boolean =
    startsWith("|") && endsWith("|") && count { it == '|' } >= 2

private fun String.isMarkdownTableDivider(): Boolean {
    if (!isMarkdownTableRow()) return false
    return trim('|')
        .split("|")
        .all { cell ->
            val normalized = cell.trim()
            normalized.isNotEmpty() && normalized.all { it == '-' || it == ':' }
        }
}

private fun String.toMarkdownTableCells(): List<String> =
    trim()
        .trim('|')
        .split("|")
        .map { it.trim() }

private const val MARKDOWN_URL_TAG = "URL"

private val MarkdownLinkRegex = Regex("""\[([^\]]+)]\(([^)\s]+)\)""")
private val RawUrlRegex = Regex("""https?://[^\s)]+|www\.[^\s)]+""")

private fun String.toMarkdownAnnotatedString(linkColor: Color) = buildAnnotatedString {
    val source = this@toMarkdownAnnotatedString
    var index = 0
    while (index < source.length) {
        val nextBold = source.indexOf("**", startIndex = index).takeIf { it >= 0 }
        val nextMarkdownLink = MarkdownLinkRegex.find(source, index)
        val nextRawUrl = RawUrlRegex.find(source, index)
        val nextStart = listOfNotNull(
            nextBold,
            nextMarkdownLink?.range?.first,
            nextRawUrl?.range?.first,
        ).minOrNull()

        if (nextStart == null) {
            append(source.substring(index))
            break
        }

        append(source.substring(index, nextStart))

        when (nextStart) {
            nextMarkdownLink?.range?.first -> {
                val label = nextMarkdownLink.groupValues[1]
                val url = nextMarkdownLink.groupValues[2]
                appendMarkdownLink(label = label, url = url, linkColor = linkColor)
                index = nextMarkdownLink.range.last + 1
            }
            nextRawUrl?.range?.first -> {
                val url = nextRawUrl.value.trimEnd('.', ',', ';', ':')
                appendMarkdownLink(label = url, url = url, linkColor = linkColor)
                index = nextRawUrl.range.first + url.length
            }
            nextBold -> {
                val boldEnd = source.indexOf("**", startIndex = nextBold + 2)
                if (boldEnd == -1) {
                    append(source.substring(nextBold))
                    break
                }

                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                    append(source.substring(nextBold + 2, boldEnd))
                }
                index = boldEnd + 2
            }
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendMarkdownLink(
    label: String,
    url: String,
    linkColor: Color,
) {
    pushStringAnnotation(tag = MARKDOWN_URL_TAG, annotation = url)
    withStyle(
        SpanStyle(
            color = linkColor,
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.SemiBold,
        )
    ) {
        append(label)
    }
    pop()
}

private fun String.ensureUrlScheme(): String =
    if (startsWith("http://") || startsWith("https://")) this else "https://$this"

@Composable
fun ChatBubble(message: ProductSearchMessage) {
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
fun AgentCompareMessage(message: ProductSearchMessage) {
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
fun TrendingDealsMessage(message: ProductSearchMessage) {
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
fun ProductListMessage(message: ProductSearchMessage, onProductClick: (ProductResponse) -> Unit) {
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
fun InlineProductDetail(message: ProductSearchMessage) {
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
