package com.emarsys.api.generic

import Activatable
import SdkContext
import com.emarsys.api.Api
import com.emarsys.api.SdkState
import com.emarsys.core.exceptions.PreconditionFailedException
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf

open class GenericApi<Logging: Activatable, Gatherer: Activatable, Internal: Activatable>(private val loggingApi: Logging,
                                                                                          private val gathererApi: Gatherer,
                                                                                          private val internalApi: Internal,
                                                                                          final override val sdkContext: SdkContext): Api {
    
    var active: Activatable = loggingApi
        set(value) {
            sdkContext.sdkScope.launch {
                value.activate()
            }
            field = value
        }
    
    init {
        sdkContext.sdkScope.launch {
            sdkContext.sdkState.collect {
                setActiveInstance(it)
            }
        }
    }

    inline fun <reified ApiType>activeInstance(): ApiType {
        return if (active is ApiType) {
            (active as ApiType)
        } else {
            throw PreconditionFailedException("Active instance must be ${typeOf<ApiType>()}")
        }
    }
    
    private fun setActiveInstance(state: SdkState) {
        active = when (state) {
            SdkState.active -> internalApi
            SdkState.onHold -> gathererApi
            SdkState.inactive -> loggingApi
        }
    }
    
}
