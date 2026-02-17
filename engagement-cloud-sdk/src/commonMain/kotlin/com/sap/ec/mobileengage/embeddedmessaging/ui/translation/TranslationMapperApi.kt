package com.sap.ec.mobileengage.embeddedmessaging.ui.translation

import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi

internal interface TranslationMapperApi {
    fun map(embeddedMessagingContext: EmbeddedMessagingContextApi): StringResources
}