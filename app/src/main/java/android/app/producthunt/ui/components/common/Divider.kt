package android.app.producthunt.ui.components.common

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CommonDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant // Tuỳ chỉnh màu theo Design System của bạn
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DividerPreview() {
    CommonDivider()
}