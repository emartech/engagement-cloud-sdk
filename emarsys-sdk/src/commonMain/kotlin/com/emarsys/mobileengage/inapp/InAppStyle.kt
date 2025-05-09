package com.emarsys.mobileengage.inapp

sealed class InAppPresentationMode {

    data object Overlay: InAppPresentationMode()

    data class Sheet(val direction: SheetDirection): InAppPresentationMode()
}

enum class InAppType {
    OVERLAY,
    INLINE,
    RIBBON
}

enum class SheetDirection {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}