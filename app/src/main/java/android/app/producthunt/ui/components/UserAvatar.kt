package android.app.producthunt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

object UserInitials {
    fun from(name: String?, email: String? = null): String {
        val source = name?.takeIf { it.isNotBlank() } ?: email.orEmpty()
        val words = source
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        val initials = when {
            words.size >= 2 -> listOf(words.first(), words.last())
            words.size == 1 -> words.first().split("@").first().take(2).map { it.toString() }
            else -> emptyList()
        }

        return initials
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
            .uppercase(Locale.getDefault())
            .ifBlank { "U" }
    }
}

@Composable
fun UserAvatar(
    name: String?,
    email: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = UserInitials.from(name, email),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value * 0.36f).sp,
        )
    }
}
