package com.sap.ec.core.device

import com.sap.ec.core.device.IosNotificationConstant.Companion.fromLong
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class IosNotificationConstantTests {

    @Test
    fun testFromLong_shouldReturnCorrectIosAuthorizationStatus() {
        val expected = IosAuthorizationStatus.Denied

        val result = fromLong<IosAuthorizationStatus>(1L)

        result shouldBe expected
    }

    @Test
    fun testFromLong_shouldReturnEnumWithValue0_whenValueIsNull() {
        val expected = IosAuthorizationStatus.NotDetermined

        val result = fromLong<IosAuthorizationStatus>(null)

        result shouldBe expected
    }

    @Test
    fun testToShowPreviewSetting_shouldReturnAlways() {
        val expected = IosShowPreviewSetting.Always

        val result = "UNShowPreviewsSettingAlways".toShowPreviewSetting()

        result shouldBe expected
    }

    @Test
    fun testToShowPreviewSetting_shouldReturnWhenAuthenticated() {
        val expected = IosShowPreviewSetting.WhenAuthenticated

        val result = "UNShowPreviewsSettingWhenAuthenticated".toShowPreviewSetting()

        result shouldBe expected
    }

    @Test
    fun testToShowPreviewSetting_shouldReturnNever() {
        val expected = IosShowPreviewSetting.Never

        val result = "UNShowPreviewsSettingNever".toShowPreviewSetting()

        result shouldBe expected
    }

    @Test
    fun testToShowPreviewSetting_shouldReturnAlwaysAsDefaultWhenStringCantBeMapped() {
        val expected = IosShowPreviewSetting.Always

        val result = "NotARealPreviewSetting".toShowPreviewSetting()

        result shouldBe expected
    }


}