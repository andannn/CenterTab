package com.example.centertab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

sealed interface LensItem {
    data class SingleLens(val zoomRatio: Float) : LensItem
    data class RangeLens(val startZoomRatio: Float, val endZoomRatio: Float) : LensItem
}

private val LensSize = 50.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleLensItem(
    modifier: Modifier = Modifier,
    lensItem: LensItem.SingleLens,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.sizeIn(minWidth = LensSize, minHeight = LensSize),
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    ) {
        Box {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "${lensItem.zoomRatio}x", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.inversePrimary

            )
        }
    }
}

@Composable
fun RangeLensItem(
    modifier: Modifier = Modifier,
    lensItem: LensItem.RangeLens,
    onStartClick: () -> Unit = {},
    onEndClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    ) {
        Row {
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = LensSize, minHeight = LensSize)
                    .clickable { onStartClick() }
                    .clip(CircleShape),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "${lensItem.startZoomRatio}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inversePrimary
                )
            }
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = LensSize, minHeight = LensSize)
                    .clickable { onEndClick() }
                    .clip(CircleShape),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "${lensItem.endZoomRatio}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inversePrimary

                )
            }
        }
    }
}

@Preview
@Composable
private fun SingleLensItemPreview() {
    MaterialTheme {
        Surface {
            SingleLensItem(
                lensItem = LensItem.SingleLens(0.3f)
            )
        }
    }
}

@Preview
@Composable
private fun RangeLensItemPreview() {
    MaterialTheme {
        Surface {
            RangeLensItem(
                lensItem = LensItem.RangeLens(0.3f, 0.5f)
            )
        }
    }

}
