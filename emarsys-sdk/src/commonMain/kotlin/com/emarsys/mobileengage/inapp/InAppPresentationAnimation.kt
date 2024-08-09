package com.emarsys.mobileengage.inapp

sealed class InAppPresentationAnimation {
    data object Fade: InAppPresentationAnimation()
    data class Slide(val duration: Double): InAppPresentationAnimation()
}