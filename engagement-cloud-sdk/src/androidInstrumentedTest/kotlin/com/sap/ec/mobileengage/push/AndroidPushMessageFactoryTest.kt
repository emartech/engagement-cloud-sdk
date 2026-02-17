package com.sap.ec.mobileengage.push

import com.sap.ec.mobileengage.push.mapper.AndroidPushV2Mapper
import com.sap.ec.mobileengage.push.mapper.HuaweiPushV2Mapper
import com.sap.ec.mobileengage.push.mapper.SilentAndroidPushV2Mapper
import com.sap.ec.mobileengage.push.mapper.SilentHuaweiPushV2Mapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Before
import org.junit.Test

class AndroidPushMessageFactoryTest {

    private lateinit var mockV2Mapper: AndroidPushV2Mapper
    private lateinit var mockSilentV2Mapper: SilentAndroidPushV2Mapper
    private lateinit var mockHuaweiPushV2Mapper: HuaweiPushV2Mapper
    private lateinit var mockSilentHuaweiPushV2Mapper: SilentHuaweiPushV2Mapper

    private lateinit var factory: AndroidPushMessageFactory

    @Before
    fun setup() = runTest {
        mockV2Mapper = mockk(relaxed = true)
        mockSilentV2Mapper = mockk(relaxed = true)
        mockHuaweiPushV2Mapper = mockk(relaxed = true)
        mockSilentHuaweiPushV2Mapper = mockk(relaxed = true)

        factory =
            AndroidPushMessageFactory(
                mockV2Mapper,
                mockSilentV2Mapper,
                mockHuaweiPushV2Mapper,
                mockSilentHuaweiPushV2Mapper
            )
    }

    @Test
    fun map_shouldReturnNull_whenPayload_isNotMatchingAnyMapper() = runTest {
        val input = buildJsonObject {
            put("noNecessaryKeys4U", "NO")
        }

        val result = factory.create(input)

        result shouldBe null
        confirmVerified(mockV2Mapper)
        confirmVerified(mockSilentV2Mapper)
        confirmVerified(mockHuaweiPushV2Mapper)
    }

    @Test
    fun map_shouldDelegate_toMapperV2_whenEmsVersion_isFCM_V2() = runTest {
        val input = buildJsonObject {
            put("ems.version", "FCM_V2")
            put("notification.silent", "false")
        }

        val result = factory.create(input)

        result shouldNotBe null

        coVerify { mockV2Mapper.map(input) }
    }

    @Test
    fun map_shouldDelegate_toSilentMapperV2_whenEmsVersion_isFCM_V2_andSilentProperty_isTrue() =
        runTest {
            val input = buildJsonObject {
                put("ems.version", "FCM_V2")
                put("notification.silent", "true")
            }

            val result = factory.create(input)

            result shouldNotBe null

            coVerify { mockSilentV2Mapper.map(input) }
        }

    @Test
    fun map_shouldReturnNull_whenEmsVersion_isNotFCM_V2() = runTest {
        val input = buildJsonObject {
            put("ems.version", "value")
            put("notification.silent", "false")
        }

        val result = factory.create(input)

        result shouldBe null

        confirmVerified(mockV2Mapper)
        confirmVerified(mockSilentV2Mapper)
        confirmVerified(mockHuaweiPushV2Mapper)
    }

    @Test
    fun map_shouldDelegate_toHuaweiMapperV2_whenEmsVersion_isHUAWEI_V2() = runTest {
        val input = buildJsonObject {
            put("ems", buildJsonObject { put("version", "HUAWEI_V2") })
            put("notification", buildJsonObject { put("silent", false) })
        }

        val result = factory.create(input)

        result shouldNotBe null

        coVerify { mockHuaweiPushV2Mapper.map(input) }
    }

    @Test
    fun map_shouldDelegate_toSilentHuaweiMapperV2_whenEmsVersion_isHUAWEI_V2_andSilentProperty_isTrue() =
        runTest {
            val input = buildJsonObject {
                put("ems", buildJsonObject { put("version", "HUAWEI_V2") })
                put("notification", buildJsonObject { put("silent", true) })
            }

            val result = factory.create(input)

            result shouldNotBe null

            coVerify { mockSilentHuaweiPushV2Mapper.map(input) }
        }

    @Test
    fun map_shouldReturnNull_whenEmsVersion_isNotHUAWEI_V2() = runTest {
        val input = buildJsonObject {
            put("ems.version", "value")
            put("notification", buildJsonObject { put("silent", true) })
        }

        val result = factory.create(input)

        result shouldBe null

        confirmVerified(mockV2Mapper)
        confirmVerified(mockSilentV2Mapper)
        confirmVerified(mockHuaweiPushV2Mapper)
    }

}