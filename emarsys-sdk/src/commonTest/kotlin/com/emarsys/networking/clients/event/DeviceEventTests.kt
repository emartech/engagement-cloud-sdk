package com.emarsys.networking.clients.event

import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.event.model.DeviceEvent
import com.emarsys.networking.clients.event.model.toDeviceEvent
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DeviceEventTests {

    @Test
    fun toDeviceEvent_should_setAttributes_fromSdkEvent() {
        val testEvent = SdkEvent.Internal.Sdk.AppStart(
            attributes = buildJsonObject { put("key", "value") })

        val expectedEvent =
            DeviceEvent("internal", "app:start", testEvent.timestamp, mapOf("key" to "value"), null, null)

        testEvent.toDeviceEvent() shouldBe expectedEvent
    }

    @Test
    fun toDeviceEvent_should_setAttributes_toEmptyMap_ifSdkEvent_doesNotContainIt() {
        val testEvent = SdkEvent.Internal.Sdk.AppStart(
            attributes = null,
        )

        val expectedEvent =
            DeviceEvent("internal", "app:start", testEvent.timestamp, mapOf(), null, null)

        testEvent.toDeviceEvent() shouldBe expectedEvent
    }
}