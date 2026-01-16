package com.emarsys.networking.clients.event

import com.emarsys.SdkConstants
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.event.model.DeviceEvent
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DeviceEventTests {
    private companion object {
        const val PAGE_LOCATION = "www.example.com"
    }

    @Test
    fun toDeviceEvent_should_setAttributes_fromSdkEvent() {
        val testEvent = SdkEvent.Internal.Sdk.AppStart(
            attributes = buildJsonObject { put("key", "value") })

        val expectedEvent =
            DeviceEvent(
                "internal",
                "app:start",
                testEvent.timestamp,
                mapOf("key" to "value"),
                null,
                null
            )

        testEvent.toDeviceEvent(pageLocation = PAGE_LOCATION) shouldBe expectedEvent
    }

    @Test
    fun toDeviceEvent_should_setAttributes_toEmptyMap_ifSdkEvent_doesNotContainIt() {
        val testEvent = SdkEvent.Internal.Sdk.AppStart(
            attributes = null,
        )

        val expectedEvent =
            DeviceEvent("internal", "app:start", testEvent.timestamp, mapOf(), null, null)

        testEvent.toDeviceEvent(pageLocation = PAGE_LOCATION) shouldBe expectedEvent
    }

    @Test
    fun toDeviceEvent_should_setUrl_inAttributes_ifPlatformCategory_isWeb_andAttributesAreNull() {
        val testEvent = SdkEvent.Internal.Sdk.AppStart(
            attributes = null,
        )

        val expectedEvent =
            DeviceEvent(
                "internal",
                "app:start",
                testEvent.timestamp,
                mapOf("url" to PAGE_LOCATION),
                null,
                null
            )

        testEvent.toDeviceEvent(SdkConstants.WEB_PLATFORM_CATEGORY, PAGE_LOCATION) shouldBe expectedEvent
    }

    @Test
    fun toDeviceEvent_should_setUrl_inAttributes_ifPlatformCategory_isWeb_andAttributesAreEmpty() {
        val testEvent = SdkEvent.Internal.Sdk.AppStart(
            attributes = buildJsonObject { }
        )

        val expectedEvent =
            DeviceEvent(
                "internal",
                "app:start",
                testEvent.timestamp,
                mapOf("url" to PAGE_LOCATION),
                null,
                null
            )

        testEvent.toDeviceEvent(SdkConstants.WEB_PLATFORM_CATEGORY, PAGE_LOCATION) shouldBe expectedEvent
    }

    @Test
    fun toDeviceEvent_should_setUrl_inAttributes_ifPlatformCategory_isWeb_andAttributesArePresent() {
        val testEvent = SdkEvent.Internal.Sdk.AppStart(
            attributes = buildJsonObject { put("key", "value") },
        )

        val expectedEvent =
            DeviceEvent(
                "internal",
                "app:start",
                testEvent.timestamp,
                mapOf("key" to "value", "url" to PAGE_LOCATION),
                null,
                null
            )

        testEvent.toDeviceEvent(SdkConstants.WEB_PLATFORM_CATEGORY, PAGE_LOCATION) shouldBe expectedEvent
    }

}