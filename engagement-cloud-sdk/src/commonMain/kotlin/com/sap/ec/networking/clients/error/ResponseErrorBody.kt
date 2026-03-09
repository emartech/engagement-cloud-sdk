package com.sap.ec.networking.clients.error

import com.sap.ec.InternalSdkApi
import kotlinx.serialization.Serializable

@Serializable
@InternalSdkApi
data class ResponseErrorBody(val error: ResponseError)

@Serializable
@InternalSdkApi
data class ResponseError(val code: String, val message: String, val target: String, val details: List<ErrorDetail>)

@Serializable
@InternalSdkApi
data class ErrorDetail(val code: String, val message: String)