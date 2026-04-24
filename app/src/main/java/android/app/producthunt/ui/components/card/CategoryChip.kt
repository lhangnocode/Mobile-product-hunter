package android.app.producthunt.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.app.producthunt.ui.theme.AndroidAppProductHuntTheme

@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }
    
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryChipPreview() {
    AndroidAppProductHuntTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CategoryChip(
                text = "All Items",
                isSelected = true,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryChipUnselectedPreview() {
    AndroidAppProductHuntTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CategoryChip(
                text = "Dropped",
                isSelected = false,
                onClick = {}
            )
        }
    }
}
