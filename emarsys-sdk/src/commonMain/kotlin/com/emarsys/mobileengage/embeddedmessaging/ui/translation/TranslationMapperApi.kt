package com.emarsys.mobileengage.embeddedmessaging.ui.translation

import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi

interface TranslationMapperApi {
    fun map(embeddedMessagingContext: EmbeddedMessagingContextApi): StringResources
}