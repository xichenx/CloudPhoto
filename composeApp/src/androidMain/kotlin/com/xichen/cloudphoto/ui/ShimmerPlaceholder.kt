package com.xichen.cloudphoto.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 图片加载占位：底色 + **自上而下** 循环移动的竖向高光带（骨架屏 shimmer）。
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    isOnDarkBackground: Boolean = false
) {
    val highlightMidAlpha = if (isOnDarkBackground) 0.12f else 0.18f
    val transition = rememberInfiniteTransition(label = "verticalShimmer")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "verticalShift"
    )
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val hPx = max(constraints.maxHeight.toFloat(), 1f)
        val bandPx = max(hPx * 0.42f, with(density) { 36.dp.toPx() })
        val yPx = -bandPx + shift * (hPx + bandPx)
        val bandDp = with(density) { bandPx.toDp() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(baseColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bandDp)
                    .offset { IntOffset(0, yPx.roundToInt()) }
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.5f to Color.White.copy(alpha = highlightMidAlpha),
                                1f to Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
