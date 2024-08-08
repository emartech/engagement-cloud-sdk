package com.emarsys.mobileengage.inApp

sealed class InAppPresentationMode {

    data object Overlay: InAppPresentationMode()

    data class Sheet(val direction: SheetDirection): InAppPresentationMode()

}

enum class SheetDirection {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}