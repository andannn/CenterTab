package com.example.centertab

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.centertab.ui.theme.CenterTabTheme
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt

/**
 * Scrollable centering tab layout.
 * Selected item will be center of this layout.
 *
 * @param modifier Modifier to modify this layout.
 * @param selectedIndex selected index of tabs.
 * @param onScrollFinishToSelectIndex this will be called back when drag finished.
 * @param coroutineScope coroutineScope to do scroll operation.
 * @param tabs composabel tabs to put in this layout.
 */
@Composable
fun CenterTabLayout(
    modifier: Modifier = Modifier,
    paddingVertical: Dp = 20.dp,
    selectedIndex: Int = 0,
    onScrollFinishToSelectIndex: (Int) -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    scrollState: ScrollState = rememberScrollState(),
    tabs: @Composable () -> Unit
) {
    val onScrollFinishToSelectIndexState = rememberUpdatedState(onScrollFinishToSelectIndex)

    val layoutState = rememberCenterTabLayoutState(
        coroutineScope = coroutineScope,
        scrollState = scrollState,
        initialSelectedIndex = selectedIndex
    )

    // Side effect to do when drag finish.
    LaunchedEffect(layoutState, selectedIndex) {
        var lastInteraction: Interaction? = null
        layoutState.scrollState.interactionSource.interactions.collect { new ->
            if (lastInteraction is DragInteraction.Start && new !is DragInteraction.Start) {
                // Drag up.
                if (layoutState.centerItemIndex.value != selectedIndex) {
                    // Invoke call back if selected index changed.
                    onScrollFinishToSelectIndexState.value.invoke(layoutState.centerItemIndex.value)
                } else {
                    // Animate scroll to center of current item.
                    layoutState.animateScrollToCenterOfIndex(selectedIndex)
                }
            }
            lastInteraction = new
        }
    }

    // Side effect to animate scroll to selected index.
    LaunchedEffect(selectedIndex) {
        layoutState.animateScrollToCenterOfIndex(selectedIndex)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(align = CenterVertically)
    ) {
        val containerWidth = with(LocalDensity.current) {
            this@BoxWithConstraints.maxWidth.toPx().roundToInt()
        }
        SubcomposeLayout(
            modifier = Modifier
                .horizontalScroll(layoutState.scrollState)
                .padding(vertical = paddingVertical)
        ) { constraints ->
            val tabMeasurables = subcompose("TABS", tabs)

            val layoutHeight = tabMeasurables.fold(0) { acc, measurable ->
                maxOf(acc, measurable.maxIntrinsicHeight(Constraints.Infinity))
            }

            val tabConstraints = constraints.copy(minWidth = 50, minHeight = layoutHeight)

            val tabPlaceables = tabMeasurables.map {
                it.measure(tabConstraints)
            }

            val accTabsWidth = tabPlaceables.fold(0) { acc, placeable ->
                acc + placeable.width
            }

            // Make first item can be center of container when scroll 0.
            val startPadding = (containerWidth - tabPlaceables.first().width).div(2)
            // Make last item can be center of container when scroll max.
            val endPadding = (containerWidth - tabPlaceables.last().width).div(2)

            val layoutWidth = accTabsWidth + startPadding + endPadding

            layout(layoutWidth, layoutHeight) {
                var left = startPadding
                val tabPositions = mutableListOf<TabPosition>()
                tabPlaceables.forEachIndexed { index, placeable ->
                    placeable.placeRelative(left, 0)
                    tabPositions.add(TabPosition(left, placeable.width))
                    left += placeable.width
                }

                layoutState.onLaidOut(
                    totalTabRowWidth = layoutWidth,
                    visibleWidth = containerWidth,
                    tabPositions = tabPositions
                )
            }
        }
    }
}

@Preview
@Composable
private fun CenterTabLayoutPreview() {
    CenterTabTheme {
        var selectedIndex by remember {
            mutableStateOf(2)
        }
        CenterTabLayout(
            modifier = Modifier.fillMaxSize(),
            selectedIndex = selectedIndex,
            onScrollFinishToSelectIndex = {
                selectedIndex = it
            }
        ) {
            repeat(6) { index ->
                Tab(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    selected = index == selectedIndex,
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        selectedIndex = index
                    },
                    text = @Composable {
                        Text(text = "item $index")
                    }
                )
            }
        }
    }
}
