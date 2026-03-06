package com.sap.ec.mobileengage.embeddedmessaging.ui.translation

import com.sap.ec.networking.clients.embedded.messaging.model.MetaData

internal interface TranslationMapperApi {
    fun map(metaData: MetaData?): StringResources
}