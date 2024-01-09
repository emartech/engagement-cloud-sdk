package com.emarsys.api.generic

import Activatable
import com.emarsys.api.Api
import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.PreconditionFailedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.typeOf

open class GenericApi<Logging : Activatable, Gatherer : Activatable, Internal : Activatable>(
    private val loggingApi: Logging,
    private val gathererApi: Gatherer,
    private val internalApi: Internal,
    final override val sdkContext: SdkContextApi
) : Api {
    var activeInstance: Activatable = loggingApi

    private suspend fun activate(instance: Activatable) {
        instance.activate()
        activeInstance = instance
    }

    init {
        CoroutineScope(sdkContext.sdkDispatcher).launch {
            sdkContext.sdkState.collect {
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
                SdkState.active -> internalApi
                SdkState.onHold -> gathererApi
                SdkState.inactive -> loggingApi
            }
        )
    }

}
