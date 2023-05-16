package com.example.centertab

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlin.math.roundToInt

private const val TAG = "CenterTabLayout"

@Composable
fun CenterTabLayout(
    modifier: Modifier = Modifier,
    contentColor: Color = Color.DarkGray,
    selectedIndex: Int = 0,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    scrollState: ScrollState = rememberScrollState(),
    tabs: @Composable () -> Unit
) {
    val centerTabLayoutState = rememberCenterTabLayoutState(
        coroutineScope,
        scrollState
    )
    LaunchedEffect(key1 = Unit) {
        centerTabLayoutState.centerItemIndex.collect {
            Log.d(TAG, "CenterTabLayout: centerItemIndex $it")
        }
    }
    LaunchedEffect(key1 = Unit) {
        var lastInteraction: Interaction? = null
        centerTabLayoutState.scrollState.interactionSource.interactions.collect { new ->
            if (lastInteraction is DragInteraction.Start && new !is DragInteraction.Start) {
                centerTabLayoutState.scrollToCenterOfIndex(centerTabLayoutState.centerItemIndex.value)
            }
            lastInteraction = new
        }
    }

    Surface(
        modifier = modifier,
        color = contentColor
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = CenterVertically)
        ) {
            val containerWidth = with(LocalDensity.current) {
                this@BoxWithConstraints.maxWidth.toPx().roundToInt()
            }
            SubcomposeLayout(
                modifier = Modifier
                    .horizontalScroll(centerTabLayoutState.scrollState)
            ) { constraints ->
                Log.d(TAG, "CenterTabLayout: parentConstraint $constraints ")
                val tabMeasurables = subcompose("TABS", tabs)

                val layoutHeight = tabMeasurables.fold(0) { acc, measurable ->
                    maxOf(acc, measurable.maxIntrinsicHeight(Constraints.Infinity))
                }

                val tabConstraints = constraints.copy(minWidth = 50, minHeight = layoutHeight)

                val tabPlaceables = tabMeasurables.map {
                    it.measure(tabConstraints)
                }

                tabPlaceables.forEach {
                    Log.d(TAG, "CenterTabLayout: width  ${it.width}")
                    Log.d(TAG, "CenterTabLayout: height ${it.height}")
                }

                val accTabsWidth = tabPlaceables.fold(0) { acc, placeable ->
                    acc + placeable.width
                }

                // Make first item can be center of container when scroll 0.
                val startPadding = (containerWidth - tabPlaceables.first().width).div(2)
                // Make last item can be center of container when scroll max.
                val endPadding = (containerWidth - tabPlaceables.last().width).div(2)

                val layoutWidth = accTabsWidth + startPadding + endPadding

                Log.d(TAG, "CenterTabLayout: startPadding  $startPadding")
                Log.d(TAG, "CenterTabLayout: endPadding  $endPadding")
                Log.d(TAG, "CenterTabLayout: layoutWidth  $layoutWidth")

                layout(layoutWidth, layoutHeight) {
                    Log.d(TAG, "CenterTabLayout: containerWidth $containerWidth")
                    var left = startPadding
                    val tabPositions = mutableListOf<TabPosition>()
                    tabPlaceables.forEachIndexed { index, placeable ->
                        placeable.placeRelative(left, 0)
                        tabPositions.add(TabPosition(left, placeable.width))
                        left += placeable.width
                    }

                    centerTabLayoutState.onLaidOut(
                        totalTabRowWidth = layoutWidth,
                        visibleWidth = containerWidth,
                        tabPositions = tabPositions
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CenterTabLayoutPreview() {
    Box(
        modifier = Modifier.height(200.dp).fillMaxWidth()
    ) {
        CenterTabLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.background(Color.Red, shape = CircleShape)
            ) {
                Text(text = "bbbbbb", color = Color.Green)
            }
            Box(
                modifier = Modifier.background(Color.Green, shape = CircleShape)
            ) {
                Text(text = "ccccc")
            }
            Box(
                modifier = Modifier.background(Color.Green, shape = CircleShape)
            ) {
                Text(text = "dddddd")
            }
        }
        Spacer(modifier = Modifier.fillMaxHeight().width(2.dp).background(Color.Yellow).align(Center))
    }
}

@Preview
@Composable
private fun TabOffPreview() {
    ScrollableTabRow(
        selectedTabIndex = 0,
        modifier = Modifier.height(200.dp).background(Color.Yellow)
    ) {
        Surface(
            modifier = Modifier,
            color = Color.Red
        ) {
            Column() {
                Text(text = "sss")
                Text(text = "sss")
                Text(text = "sss")
            }
        }
        Box(
            modifier = Modifier.background(Color.Red, shape = CircleShape)
        ) {
            Text(text = "bbbbbb", color = Color.Green)
        }
        Box(
            modifier = Modifier.background(Color.Green, shape = CircleShape)
        ) {
            Text(text = "ccccc")
        }
    }
}
