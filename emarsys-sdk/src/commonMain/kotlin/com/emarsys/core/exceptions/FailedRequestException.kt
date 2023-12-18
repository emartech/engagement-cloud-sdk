package com.emarsys.core.exceptions

import com.emarsys.networking.model.Response

class FailedRequestException(val response: Response) : SdkException(response.bodyAsText ?: "Unknown error")
