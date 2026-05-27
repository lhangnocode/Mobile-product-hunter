package android.app.producthunt.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.app.producthunt.ui.navigation.Route

@Composable
fun SearchHistoryScreen(navController: NavController) {
    val historyItems = listOf(
        "Compare Samsung Tab S10 vs iPad Mini",
        "Best laptop for AI under 20M",
        "iPhone 15 vs Galaxy S24",
        "Gaming PC build under 30M",
        "Today's best deals"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { /* Clear all */ }) {
                    Text("Clear all", color = MaterialTheme.colorScheme.error)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(historyItems) { item ->
                    ListItem(
                        headlineContent = { Text(item) },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                        trailingContent = {
                            IconButton(onClick = { /* More actions */ }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }
                        },
                        modifier = Modifier.clickable {
                            navController.navigate("${Route.SEARCH}?q=$item")
                        }
                    )
                }
            }
        }
    }
}
