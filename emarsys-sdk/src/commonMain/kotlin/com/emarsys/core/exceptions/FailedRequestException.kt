package com.emarsys.core.exceptions

import com.emarsys.core.networking.model.Response

class FailedRequestException(val response: Response) : SdkException("request: ${response.originalRequest.url}, responseBody: ${response.bodyAsText}")
