package com.emarsys.core.channel

import com.emarsys.event.SdkEvent
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

internal interface SdkEventWaiterApi {
    val sdkEventDistributor: SdkEventDistributorApi
    val sdkEvent: SdkEvent

    val connectionStatus: StateFlow<Boolean>

    suspend fun <T : Any> await(expectedResultSuccessClass: KClass<T>? = null): SdkEvent.Internal.Sdk.Answer.Response<T>
}