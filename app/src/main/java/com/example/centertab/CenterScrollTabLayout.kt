package com.example.centertab

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect

private const val TAG = "CenterScrollTabLayout"

@Composable
fun CenterScrollTabLayout(
    modifier: Modifier,
    items: List<TabItem>
) {
    val layoutState = rememberCenterScrollTabLayoutState(
        lazyListState = rememberLazyListState()
    )
    val selectedIndex = remember { mutableStateOf(0) }
//
//    LaunchedEffect(Unit) {
//        snapshotFlow {
//            scrollState.firstVisibleItemIndex
//        }.collect {
//            Log.d(TAG, "firstVisibleItemIndex: $it")
//        }
//    }
    LaunchedEffect(Unit) {
        snapshotFlow {
            layoutState.lazyListState.layoutInfo.viewportSize
        }.collect {
            Log.d(TAG, "viewportSize: $it")
        }
    }
    val centerIndex by layoutState.centerItemIndex.collectAsState(initial = 0)
    LaunchedEffect(Unit) {
        layoutState.centerItemIndex.collect {
            Log.d(TAG, "centerItemIndex: $it")
        }
    }
//    LaunchedEffect(key1 = Unit) {
//        delay(2000)
//        scrollState.animateScrollToItem(2, 100)
//    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        state = layoutState.lazyListState
    ) {
        itemsIndexed(
            items = items,
            key = { index, item ->
                item.label
            }
        ) { index, item ->
            val isSelected = item == items.getOrNull(centerIndex)
            var paddingStartPx by remember {
                mutableStateOf(0)
            }
            var paddingEndPx by remember {
                mutableStateOf(0)
            }
            Tab(
                modifier = Modifier
                    .onSizeChanged { newSize ->
                        Log.d(TAG, "onSizeChanged: newSize $newSize")
                        val containerWidth = layoutState.viewportSize.width
                        Log.d(TAG, "onSizeChanged: containerWidth $containerWidth")
                        if (index == 0) {
                            paddingStartPx = (containerWidth.div(2) - newSize.width.div(2)).coerceAtLeast(0)
                        } else if (index == items.size - 1) {
                            paddingEndPx = (containerWidth.div(2) - newSize.width.div(2)).coerceAtLeast(0)
                        }
                    }
                    .padding(
                        PaddingValues(
                            start = with(LocalDensity.current) { paddingStartPx.toDp() },
                            end = with(LocalDensity.current) { paddingEndPx.toDp() }
                        )
                    ),
                item = item,
                isSelected = isSelected
            )
        }
    }

//    LaunchedEffect(scrollState.value) {
//        val visibleItems = scrollState.layoutInfo.visibleItemsInfo
//        if (visibleItems.isNotEmpty()) {
//            val middleIndex = visibleItems.size / 2
//            val middleItem = visibleItems[middleIndex]
//            val selectedIndexValue = items.indexOf(middleItem.key)
//            selectedIndex.value = selectedIndexValue
//        }
//    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tab(
    modifier: Modifier,
    item: TabItem,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        shape = CircleShape,
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.padding(PaddingValues(horizontal = 60.dp, vertical = 5.dp)),
            text = item.label
        )
    }
}

@Preview
@Composable
fun TabPreview() {
    Tab(
        modifier = Modifier,
        item = TabItem(
            label = "Test"
        ),
        isSelected = true
    )
}

@Preview
@Composable
fun TabLayoutPreview() {
    CenterScrollTabLayout(
        modifier = Modifier,
        items = listOf(
            TabItem(
                label = "Test"
            )
//            TabItem(
//                label = "Test2"
//            ),
//            TabItem(
//                label = "Test3"
//            ),
//            TabItem(
//                label = "Test4"
//            ),
//            TabItem(
//                label = "Test5"
//            )
        )
    )
}

@Preview
@Composable
fun TestSlider() {
    ScrollableTabRow(selectedTabIndex = 0) {
        androidx.compose.material3.Tab(selected = true, onClick = { /*TODO*/ }) {
            Text(text = "Test5")
        }
        androidx.compose.material3.Tab(selected = false, onClick = { /*TODO*/ }) {
            Text(text = "Test10")
        }
    }
}
