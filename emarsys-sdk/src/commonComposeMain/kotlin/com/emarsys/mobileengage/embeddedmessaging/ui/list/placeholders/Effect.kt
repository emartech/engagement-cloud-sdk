package com.emarsys.mobileengage.embeddedmessaging.ui.list.placeholders

import androidx.annotation.FloatRange
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush

interface Effect {
    val animationSpec: InfiniteRepeatableSpec<Float>?

    fun brush(@FloatRange(from = 0.0, to = 1.0) progress: Float, size: Size): Brush

    @FloatRange(from = 0.0, to = 1.0)
    fun alpha(progress: Float): Float
}