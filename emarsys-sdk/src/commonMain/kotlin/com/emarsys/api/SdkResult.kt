package com.emarsys.api

sealed class SdkResult {
    data class Success<T>(val value: T) : SdkResult()
    data class Failure(val error: Throwable) : SdkResult()

}