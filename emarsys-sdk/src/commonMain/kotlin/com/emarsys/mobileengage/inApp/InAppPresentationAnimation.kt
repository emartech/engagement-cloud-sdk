package com.emarsys.mobileengage.inApp

sealed class InAppPresentationAnimation {
    data object Fade: InAppPresentationAnimation()
    data class Slide(val duration: Double): InAppPresentationAnimation()
}