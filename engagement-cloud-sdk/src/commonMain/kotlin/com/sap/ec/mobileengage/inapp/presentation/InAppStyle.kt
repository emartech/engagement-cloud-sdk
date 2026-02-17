package com.sap.ec.mobileengage.inapp.presentation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class InAppPresentationMode {

    data object Overlay : InAppPresentationMode()

    data class Sheet(val direction: SheetDirection) : InAppPresentationMode()
}

@Serializable
enum class InAppType {
    @SerialName("overlay")
    OVERLAY,

    @SerialName("inline")
    INLINE,

    @SerialName("ribbon")
    RIBBON
}

enum class SheetDirection {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}