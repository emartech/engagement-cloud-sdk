package com.sap.ec.mobileengage.inapp.presentation

import com.sap.ec.InternalSdkApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal sealed class InAppPresentationMode {

    data object Overlay : InAppPresentationMode()

    data class Sheet(val direction: SheetDirection) : InAppPresentationMode()
}

@InternalSdkApi
@Serializable
enum class InAppType {
    @SerialName("overlay")
    OVERLAY,

    @SerialName("inline")
    INLINE,

    @SerialName("ribbon")
    RIBBON
}

internal enum class SheetDirection {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}