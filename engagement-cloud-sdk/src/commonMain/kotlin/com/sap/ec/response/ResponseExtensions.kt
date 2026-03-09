package com.sap.ec.response

import com.sap.ec.event.SdkEvent

internal fun SdkEvent.Internal.Sdk.Answer.Response<*>.mapToUnitOrFailure(): Result<Unit> {
    return this.result.map { result -> Unit }
}