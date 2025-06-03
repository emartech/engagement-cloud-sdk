package com.emarsys.networking.clients.error

import kotlinx.serialization.Serializable

@Serializable
data class ResponseErrorBody(val error: ResponseError)

@Serializable
data class ResponseError(val code: Int, val message: String, val target: String, val details: List<ErrorDetail>)

@Serializable
data class ErrorDetail(val code: Int, val message: String)