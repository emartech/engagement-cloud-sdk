package com.sap.ec.mobileengage.inapp.presentation

internal sealed class InAppPresentationAnimation {
    data object Fade: InAppPresentationAnimation()
    data class Slide(val duration: Double): InAppPresentationAnimation()
}