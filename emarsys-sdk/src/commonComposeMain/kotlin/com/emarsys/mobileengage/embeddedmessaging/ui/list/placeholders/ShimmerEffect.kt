package com.emarsys.mobileengage.embeddedmessaging.ui.list.placeholders

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

public data class Shimmer(
    private val highlightColor: Color,
    override val animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = 1200, easing = LinearEasing),
    ),
    private val intensity: Float = 0f,
    private val dropOff: Float = 0.5f,
    private val tilt: Float = 20f,
) : Effect {

    override fun brush(
        progress: Float,
        size: Size,
    ): Brush {
        val shimmerColorStops = arrayOf(
            max((1f - intensity - dropOff) / 2f, 0f) to Color.Transparent,
            max((1f - intensity - 0.001f) / 2f, 0f) to highlightColor,
            min((1f + intensity + 0.001f) / 2f, 1f) to highlightColor,
            min((1f + intensity + dropOff) / 2f, 1f) to Color.Transparent,
        )
        val tiltRad = tilt * 3.14f / 180f
        val totalWidth = size.width + tan(tiltRad) * size.height
        val dx = offset(-totalWidth, totalWidth * 2f, progress)
        val dy = 0f
        val start = Offset(dx - (totalWidth / 2f), dy)

        return Brush.linearGradient(
            colorStops = shimmerColorStops,
            start = start,
            end = Offset(start.x + totalWidth, size.height),
        )
    }

    override fun alpha(
        progress: Float,
    ): Float = 1.0f

    private fun offset(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }
}