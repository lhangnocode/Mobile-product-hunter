package android.app.producthunt.ui.components.appbar

import android.app.producthunt.R
import android.app.producthunt.ui.theme.PHSpacing
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val TopAppBarBlack = androidx.compose.ui.graphics.Color(0xFF1A1A1A)

@Composable
fun ProductHunterMainTopAppBar(
    modifier: Modifier = Modifier,
    onSearchClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TopAppBarBlack,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = PHSpacing.AppBarHorizontal, vertical = PHSpacing.AppBarVertical),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "ProductHunter",
                style = MaterialTheme.typography.titleLarge,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (onSearchClick != null) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = androidx.compose.ui.graphics.Color.White,
                    )
                }
            }
            if (onProfileClick != null) {
                IconButton(onClick = onProfileClick) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductHunterChildTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: () -> Unit,
    showSearchAction: Boolean = false,
    showCalendarAction: Boolean = false,
    onSearchClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    MediumTopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (showSearchAction) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
            if (showCalendarAction) {
                IconButton(onClick = onCalendarClick) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Calendar")
                }
            }
        },
        expandedHeight = 104.dp,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = TopAppBarBlack,
            scrolledContainerColor = TopAppBarBlack,
            navigationIconContentColor = androidx.compose.ui.graphics.Color.White,
            titleContentColor = androidx.compose.ui.graphics.Color.White,
            actionIconContentColor = androidx.compose.ui.graphics.Color.White,
        ),
        scrollBehavior = scrollBehavior,
    )
}
