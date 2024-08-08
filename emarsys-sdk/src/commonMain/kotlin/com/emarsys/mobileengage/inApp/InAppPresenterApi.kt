package com.emarsys.mobileengage.inApp

interface InAppPresenterApi {

    suspend fun present(view: InAppViewApi, mode: InAppPresentationMode, animation: InAppPresentationAnimation? = InAppPresentationAnimation.Slide(0.3))

}
