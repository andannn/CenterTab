package com.example.centertab

import android.icu.text.ListFormatter.Width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

fun rememberCenterScrollTabLayoutState(
    scrollState: ScrollState
) = CenterTabLayoutState(scrollState)

class CenterTabLayoutState(
    val scrollState: ScrollState
) {
}

data class TabPosition(
    val left: Int,
    val width: Width
)
