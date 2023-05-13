package com.example.centertab

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class TabItem(
    val label: String
)

@Composable
fun CenterScrollTabLayout(
    modifier: Modifier,
    items: List<TabItem>,
    selectedItem: TabItem
) {
    val scrollState = rememberLazyListState()
    val selectedIndex = remember { mutableStateOf(0) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        state = scrollState
    ) {
        items(
            items = items,
            key = { it.label }
        ) { item ->
            val isSelected = item == selectedItem
            Tab(
                modifier = Modifier,
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
            modifier = Modifier.padding(PaddingValues(horizontal = 10.dp, vertical = 5.dp)),
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
            ),
            TabItem(
                label = "Test2"
            ),
            TabItem(
                label = "Test3"
            ),
            TabItem(
                label = "Test4"
            ),
            TabItem(
                label = "Test5"
            )
        ),
        selectedItem = TabItem(
            label = "Test2"
        )
    )
}
