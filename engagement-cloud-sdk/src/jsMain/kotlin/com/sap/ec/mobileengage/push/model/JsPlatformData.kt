package com.sap.ec.mobileengage.push.model

import com.sap.ec.InternalSdkApi
import com.sap.ec.mobileengage.push.PlatformData
import kotlinx.serialization.Serializable

//needs to be exposed for ServiceWorker
@InternalSdkApi
@Serializable
data object JsPlatformData : PlatformData