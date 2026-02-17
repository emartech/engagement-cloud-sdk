package com.sap.ec.core.device

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

object AndroidVersionUtils {
    val isBelowQ: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    val isBelowS: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    val isTiramisuOrAbove: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val isBelowTiramisu: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    val isUpsideDownCakeOrAbove: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    val isUpsideDownCake: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    val isBelowUpsideDownCake: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}