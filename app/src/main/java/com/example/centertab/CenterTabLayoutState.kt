package com.example.centertab

import android.util.Range
import androidx.compose.foundation.ScrollState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun rememberCenterTabLayoutState(
    coroutineScope: CoroutineScope,
    scrollState: ScrollState
) = CenterTabLayoutState(coroutineScope, scrollState)

private const val TAG = "CenterTabLayoutState"

class CenterTabLayoutState(
    val coroutineScope: CoroutineScope,
    val scrollState: ScrollState
) {
    val centerItemIndex = MutableStateFlow(0)

    private var tabPositions: List<TabPosition> = emptyList()
    private var totalTabRowWidth: Int = 0
    private var visibleWidth: Int = 0

    fun onLaidOut(
        totalTabRowWidth: Int,
        visibleWidth: Int,
        tabPositions: List<TabPosition>
    ) {
        this.tabPositions = tabPositions
        this.totalTabRowWidth = totalTabRowWidth
        this.visibleWidth = visibleWidth

        val size = tabPositions.size
        val ranges = tabPositions.mapIndexed { index, tabPosition ->
            val centerOffset = tabPosition.calculateTabOffset(
                totalTabRowWidth,
                visibleWidth
            )
            val halfWidth = tabPosition.width.div(2)
            when (index) {
                0 -> {
                    Range(0, halfWidth)
                }
                size - 1 -> {
                    Range(centerOffset - halfWidth, centerOffset)
                }
                else -> {
                    Range(centerOffset - halfWidth, centerOffset + halfWidth)
                }
            }
        }

        centerItemIndex.value = ranges.indexOfFirst { range ->
            scrollState.value in range
        }
    }

    fun scrollToCenterOfIndex(value: Int) {
        coroutineScope.launch {
            tabPositions.getOrNull(value)?.let { tabPosition ->
                val offset = tabPosition.calculateTabOffset(totalTabRowWidth, visibleWidth)
                scrollState.animateScrollTo(
                    offset
                )
            }
        }
    }

    private fun TabPosition.calculateTabOffset(
        totalTabRowWidth: Int,
        visibleWidth: Int
    ): Int {
        val tabOffset = left
        val tabWidth = width
        val scrollerCenter = visibleWidth / 2
        val centeredTabOffset = tabOffset - (scrollerCenter - tabWidth / 2)
        // How much space we have to scroll. If the visible width is <= to the total width, then
        // we have no space to scroll as everything is always visible.
        val availableSpace = (totalTabRowWidth - visibleWidth).coerceAtLeast(0)
        return centeredTabOffset.coerceIn(0, availableSpace)
    }
}

data class TabPosition(
    val left: Int,
    val width: Int
) {
    val right get() = left + width
}
