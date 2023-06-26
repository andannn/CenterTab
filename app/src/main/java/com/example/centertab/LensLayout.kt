package com.example.centertab

import android.graphics.RectF
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.centertab.LensItemScope.lensItem
import com.example.centertab.ui.theme.LensLayoutTheme
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt


private val LENS_LAYOUT_FULLY_EXPANDED_WIDTH = 1900
private const val PointIntervalWidth = 10f
private const val TAG = "LensLayout"

/**
 *
 * @param modifier Modifier to modify this layout.
 * @param selectedIndex selected index of tabs.
 * @param onScrollFinishToSelectIndex this will be called back when drag finished.
 * @param coroutineScope coroutineScope to do scroll operation.
 * @param lens composabel tabs to put in this layout.
 */
@RequiresApi(Build.VERSION_CODES.Q)
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
        // Always calculate position according to this layoutRatio.
        val layoutRatio by transation.animateFloat(label = "lens_layout_width") { state ->
            when (state) {
                AnimationState.COLLAPSED -> 0f
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

            val lensConstraints = constraints.copy(minHeight = layoutHeight)

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

            // Start/end padding is 0 when totally shrink.
            val horizonPadding = (containerWidth.div(2) * layoutRatio).roundToInt()

            val layoutWidth = lensLayoutWidth + (horizonPadding * 2)

            layout(layoutWidth, layoutHeight) {
                var left = horizonPadding
                val insufficientAreaList = mutableListOf<ClosedFloatingPointRange<Float>>()

                lensPlaceables.forEachIndexed { index, placeable ->
                    val shrinkStart = left
                    val startFactor = lensItemData.getOrNull(index)?.startFactor!!
                    val halfWidth = placeable.width.div(2f)
                    val expandStart = (startFactor * expandWidth - halfWidth).roundToInt()
                    val startInLensLayout = lerp(shrinkStart, expandStart, layoutRatio)
                    val endInLensLayout = startInLensLayout + placeable.width
                    val start = startInLensLayout + horizonPadding
                    placeable.placeRelative(start, 0)
                    left += placeable.width

                    insufficientAreaList.add(
                        startInLensLayout.div(lensLayoutWidth.toFloat()).rangeTo(
                            endInLensLayout.div(lensLayoutWidth.toFloat())
                        )
                    )
                }

                subcompose("Tint") {
                    InsufficientBackgroundTint(insufficientAreaList = insufficientAreaList)
                }.forEach {
                    it.measure(
                        constraints.copy(
                            maxWidth = lensLayoutWidth,
                            maxHeight = layoutHeight
                        )
                    )
                        .placeRelative(x = horizonPadding, y = 0)
                }
            }
        }
    }
}

@Immutable
object LensItemScope {

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

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
private fun InsufficientBackgroundTint(
    modifier: Modifier = Modifier,
    insufficientAreaList: List<ClosedFloatingPointRange<Float>>,
    tintColor: Color = Color.Red
) {
    Spacer(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val insufficientAreas = insufficientAreaList
                    .map {
                        val start = (size.width * it.start)
                        val end = (size.width * it.endInclusive)
                        start.rangeTo(end)
                    }

                // draw background tint.
                val pointsSize = (size.width / PointIntervalWidth).roundToInt()
                val ptsArray: FloatArray = FloatArray(
                    pointsSize * 2
                ) { index ->
                    if (index % 2 == 0) {
                        (index / 2) * PointIntervalWidth // x
                    } else {
                        center.y // y
                    }
                }
                val canvas = drawContext.canvas.nativeCanvas
                val paint = Paint()
                    .apply {
                        strokeWidth = 3f
                        color = tintColor
                    }
                    .asFrameworkPaint()

                val count = canvas.saveLayer(null, paint)
                canvas.drawPoints(
                    ptsArray,
                    paint
                )
                paint.blendMode = android.graphics.BlendMode.DST_OUT
                insufficientAreas.onEach { range ->
                    canvas.drawRect(
                        RectF(
                            range.start,
                            0f,
                            range.start + range.endInclusive - range.start,
                            size.height
                        ),
                        paint
                    )
                }
                canvas.restoreToCount(count)
            }
    )
}

private class LensItemParentData(
    val startFactor: Float
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@LensItemParentData
}

enum class AnimationState {
    COLLAPSED,
    EXPANDED
}

object ZoomRatioToPxPolicy {


    fun zoomRatioToPx(ratio: Float, startZoomRatio: Float) {

    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Preview
@Composable
private fun InsufficientBackgroundTintPreview() {
    LensLayoutTheme {
        Surface {
            InsufficientBackgroundTint(
                modifier = Modifier
                    .height(90.dp)
                    .width(280.dp),
                insufficientAreaList = listOf(
                    0.2f.rangeTo(0.3f)
                )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Preview
@Composable
private fun LensLayoutPreview() {
    LensLayoutTheme {
        var selectedIndex by remember {
            mutableStateOf(2)
        }
        var state by remember {
            mutableStateOf(AnimationState.EXPANDED)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
        ) {
            LensLayout(
                modifier = Modifier
                    .widthIn(min = 20.dp, max = 400.dp)
                    .align(Center)
                    .background(
                        color = MaterialTheme.colorScheme.inverseSurface,
                        shape = CircleShape
                    )
                    .padding(3.dp),
                selectedIndex = selectedIndex,
                onScrollFinishToSelectIndex = {
                    selectedIndex = it
                },
                animeState = state
            ) {
                SingleLensItem(
                    modifier = Modifier
                        .lensItem(
                            lensItem = LensItem.SingleLens(0.3f),
                            lensLayoutRatioRange = 0.2f..4.8f
                        ),
                    lensItem = LensItem.SingleLens(0.3f),
                    onClick = {
                        if (state == AnimationState.COLLAPSED) {
                            state = AnimationState.EXPANDED
                        } else {
                            state = AnimationState.COLLAPSED
                        }
                    }
                )
                SingleLensItem(
                    modifier = Modifier
                        .lensItem(
                            lensItem = LensItem.SingleLens(1.0f),
                            lensLayoutRatioRange = 0.2f..4.8f
                        ),
                    lensItem = LensItem.SingleLens(1.0f),
                    onClick = {
                        if (state == AnimationState.COLLAPSED) {
                            state = AnimationState.EXPANDED
                        } else {
                            state = AnimationState.COLLAPSED
                        }
                    }
                )
                RangeLensItem(
                    modifier = Modifier
                        .lensItem(
                            lensItem = LensItem.RangeLens(3.8f, 5.2f),
                            lensLayoutRatioRange = 0.2f..4.8f
                        ),
                    lensItem = LensItem.RangeLens(3.8f, 5.2f)
                )
            }
        }
    }
}
