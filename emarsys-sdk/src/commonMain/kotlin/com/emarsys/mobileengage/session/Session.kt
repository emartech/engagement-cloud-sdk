package com.emarsys.mobileengage.session

import com.emarsys.api.SdkResult

interface Session {
    suspend fun startSession(): SdkResult
    suspend fun endSession(): SdkResult
}