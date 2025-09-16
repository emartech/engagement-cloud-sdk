package com.emarsys.response

import com.emarsys.event.SdkEvent

fun SdkEvent.Internal.Sdk.Answer.Response<*>.mapToUnitOrFailure(): Result<Unit> {
    return this.result.map { result -> Unit }
}