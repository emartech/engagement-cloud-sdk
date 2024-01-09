package com.emarsys.context

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import io.ktor.http.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface SdkContextApi {
    val sdkState: StateFlow<SdkState>
    val sdkDispatcher: CoroutineDispatcher
    var config: EmarsysConfig?
    fun setSdkState(sdkState: SdkState)
    fun createUrl(
        baseUrl: String,
        version: String = "v3",
        withAppCode: Boolean = true,
        path: String?
    ): Url

}