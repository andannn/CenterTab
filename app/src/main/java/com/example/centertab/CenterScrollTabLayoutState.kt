package com.example.centertab

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class TabItem(
    val label: String
)

fun rememberCenterScrollTabLayoutState(
    lazyListState: LazyListState
) = CenterScrollTabLayoutState(lazyListState)

class CenterScrollTabLayoutState(
    val lazyListState: LazyListState
) {
    private val visibleItemsInfo = snapshotFlow {
        lazyListState.layoutInfo.visibleItemsInfo.map {
            ItemInfo(
                index = it.index,
                offset = it.offset,
                size = it.size
            )
        }
    }

    val viewportSize
        get() = lazyListState.layoutInfo.viewportSize

    val centerItemIndex = visibleItemsInfo
        .map { infoList ->
            infoList
                .firstOrNull { info ->
                    val range = with(info) {
                        IntRange(offset, offset + size)
                    }
                    range.contains(viewportSize.width.div(2))
                }
                ?.index ?: 0
        }
        .distinctUntilChanged()
}

data class ItemInfo(
    val index: Int,
    val offset: Int,
    val size: Int
)
