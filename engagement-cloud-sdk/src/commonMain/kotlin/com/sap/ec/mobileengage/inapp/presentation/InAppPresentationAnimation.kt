package com.sap.ec.mobileengage.inapp.presentation

sealed class InAppPresentationAnimation {
    data object Fade: InAppPresentationAnimation()
    data class Slide(val duration: Double): InAppPresentationAnimation()
}