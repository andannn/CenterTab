package com.example.centertab

import android.util.Log
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import com.example.centertab.LensItemScope.lensItem
import com.example.centertab.ui.theme.LensLayoutTheme
import kotlinx.coroutines.CoroutineScope
import kotlin.math.exp
import kotlin.math.roundToInt

private val LENS_LAYOUT_FULLY_EXPANDED_WIDTH = 500
private const val TAG = "LensLayout"

/**
 *
 * @param modifier Modifier to modify this layout.
 * @param selectedIndex selected index of tabs.
 * @param onScrollFinishToSelectIndex this will be called back when drag finished.
 * @param coroutineScope coroutineScope to do scroll operation.
 * @param lens composabel tabs to put in this layout.
 */
@Composable
fun LensLayout(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    onScrollFinishToSelectIndex: (Int) -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    scrollState: ScrollState = rememberScrollState(),
    animeState: AnimationState,
    lens: @Composable () -> Unit
) {
    val onScrollFinishToSelectIndexState = rememberUpdatedState(onScrollFinishToSelectIndex)

    val layoutState = rememberLensLayoutState(
        coroutineScope = coroutineScope,
        scrollState = scrollState,
        initialSelectedIndex = selectedIndex
    )

    val currentAnimeState by rememberUpdatedState(newValue = animeState)
    val transation =
        updateTransition(targetState = currentAnimeState, label = "LENS_LAYOUT_ANIMATION")

    BoxWithConstraints(
        modifier = modifier
            .wrapContentHeight(align = CenterVertically)
    ) {
        val layoutRatio by transation.animateFloat(label = "lens_layout_width") { state ->
            when (state) {
                AnimationState.COLLAPASED -> 0f
                AnimationState.EXPANDED -> 1f
            }
        }
        val containerWidth = with(LocalDensity.current) {
            this@BoxWithConstraints.maxWidth.toPx().roundToInt()
        }
        SubcomposeLayout(
            modifier = Modifier
                .wrapContentWidth(CenterHorizontally)
                .horizontalScroll(layoutState.scrollState)
        ) { constraints ->
            val lensMeasurables = subcompose("LENS", lens)

            val layoutHeight = lensMeasurables.fold(0) { acc, measurable ->
                maxOf(acc, measurable.maxIntrinsicHeight(Constraints.Infinity))
            }

            val lensConstraints = constraints.copy(minWidth = 50, minHeight = layoutHeight)

            val lensItemData = lensMeasurables
                .map { it.parentData as? LensItemParentData }

            val lensPlaceables = lensMeasurables.map {
                it.measure(lensConstraints)
            }

            val shrinkWidth = lensPlaceables.fold(0) { acc, item ->
                item.width + acc
            }
            val expandWidth = LENS_LAYOUT_FULLY_EXPANDED_WIDTH
            val lensLayoutWidth = lerp(shrinkWidth, expandWidth, layoutRatio)

            val horizonPadding = (containerWidth.div(2) * layoutRatio).roundToInt()

            val layoutWidth = lensLayoutWidth + (horizonPadding * 2)

            Log.d(TAG, "LensLayout:  horizonPadding $horizonPadding")
            layout(layoutWidth, layoutHeight) {
                var left = horizonPadding

                lensPlaceables.forEachIndexed { index, placeable ->
                    val shrinkStart = left
                    val startFactor = lensItemData.getOrNull(index)?.startFactor!!
                    val halfWidth = placeable.width.div(2f)
                    val expandStart = (startFactor * expandWidth - halfWidth).roundToInt()
                    val start = lerp(shrinkStart, expandStart, layoutRatio) + horizonPadding
                    placeable.placeRelative(start, 0)
                    left += placeable.width
                }
            }
        }
    }
}

@Immutable
object LensItemScope {
    sealed interface LensItem {
        data class SingleLens(val zoomRatio: Float) : LensItem
        data class RangeLens(val startZoomRatio: Float, val endZoomRatio: Float) : LensItem
    }

    fun Modifier.lensItem(
        lensItem: LensItem,
        lensLayoutRatioRange: ClosedFloatingPointRange<Float>
    ): Modifier {
        val startZoomRatio = when (lensItem) {
            is LensItem.RangeLens -> lensItem.startZoomRatio
            is LensItem.SingleLens -> lensItem.zoomRatio
        }
        val startFactor = with(lensLayoutRatioRange) {
            (startZoomRatio - start) / (endInclusive - start)
        }
        return then(
            LensItemParentData(startFactor)
        )
    }
}

private class LensItemParentData(
    val startFactor: Float
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@LensItemParentData
}

enum class AnimationState {
    COLLAPASED,
    EXPANDED
}

@Preview
@Composable
private fun LensLayoutPreview() {
    LensLayoutTheme {
        var selectedIndex by remember {
            mutableStateOf(2)
        }
        var state by remember {
            mutableStateOf(AnimationState.COLLAPASED)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LensLayout(
                modifier = Modifier
                    .widthIn(min = 20.dp, max = 300.dp)
                    .align(Center)
                    .background(Color.Blue),
                selectedIndex = selectedIndex,
                onScrollFinishToSelectIndex = {
                    selectedIndex = it
                },
                animeState = state
            ) {
                Box(modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .size(50.dp)
                    .background(Color.Red)
                    .lensItem(
                        lensItem = LensItemScope.LensItem.SingleLens(0.2f),
                        lensLayoutRatioRange = 0.2f..0.8f
                    )
                    .clickable {
                        if (state == AnimationState.COLLAPASED) {
                            state = AnimationState.EXPANDED
                        } else {
                            state = AnimationState.COLLAPASED
                        }
                    })
                Box(modifier = Modifier
                    .size(50.dp)
                    .background(Color.Green)
                    .lensItem(
                        lensItem = LensItemScope.LensItem.SingleLens(0.7f),
                        lensLayoutRatioRange = 0.2f..0.8f
                    )
                    .clickable {
                        if (state == AnimationState.COLLAPASED) {
                            state = AnimationState.EXPANDED
                        } else {
                            state = AnimationState.COLLAPASED
                        }
                    })
            }
        }
    }
}
