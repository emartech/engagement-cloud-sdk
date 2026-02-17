package com.sap.ec.api.generic

import Activatable
import com.sap.ec.api.Api
import com.sap.ec.api.AutoRegisterable
import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException.PreconditionFailedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf

internal open class GenericApi<Logging : Activatable, Gatherer : Activatable, Internal : Activatable>(
    private val loggingApi: Logging,
    private val gathererApi: Gatherer,
    private val internalApi: Internal,
    final override val sdkContext: SdkContextApi
) : Api, AutoRegisterable {
    var activeInstance: Activatable = loggingApi

    private suspend fun activate(instance: Activatable) {
        instance.activate()
        activeInstance = instance
    }

    override suspend fun registerOnContext() {
        CoroutineScope(sdkContext.sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
            sdkContext.currentSdkState.collect {
                setActiveInstance(it)
            }
        }
    }

    inline fun <reified ApiType> activeInstance(): ApiType {
        return if (activeInstance is ApiType) {
            (activeInstance as ApiType)
        } else {
            throw PreconditionFailedException("Active instance must be ${typeOf<ApiType>()}")
        }
    }

    private suspend fun setActiveInstance(state: SdkState) {
        activate(
            when (state) {
                SdkState.Active -> internalApi
                SdkState.OnHold -> gathererApi
                SdkState.UnInitialized -> loggingApi
                SdkState.Initialized -> loggingApi
            }
        )
    }

}
