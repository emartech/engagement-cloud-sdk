package com.sap.ec.response

import com.sap.ec.event.SdkEvent
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ResponseExtensionTests {

    @Test
    fun mapToUnitOrFailure_shouldReturnUnit_ifResultWasSuccess() {
        val testResponse =
            SdkEvent.Internal.Sdk.Answer.Response("testId", Result.success("success"))

        testResponse.mapToUnitOrFailure() shouldBe Result.success(Unit)
    }

    @Test
    fun mapToUnitOrFailure_shouldReturnFailure_ifResultWasFailure() {
        val testException = Exception("not success")
        val testResponse =
            SdkEvent.Internal.Sdk.Answer.Response("testId", Result.failure<Unit>(testException))

        testResponse.mapToUnitOrFailure() shouldBe Result.failure(testException)
    }
}