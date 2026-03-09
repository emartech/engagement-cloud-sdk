package com.sap.ec.networking.clients.error

import kotlinx.serialization.Serializable

@Serializable
internal data class ResponseErrorBody(val error: ResponseError)

@Serializable
internal data class ResponseError(val code: String, val message: String, val target: String, val details: List<ErrorDetail>)

@Serializable
internal data class ErrorDetail(val code: String, val message: String)