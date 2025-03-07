package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.push.mapper.AndroidPushV1Mapper
import com.emarsys.mobileengage.push.mapper.AndroidPushV2Mapper
import com.emarsys.mobileengage.push.mapper.SilentAndroidPushV1Mapper
import com.emarsys.mobileengage.push.mapper.SilentAndroidPushV2Mapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.Before
import org.junit.Test

class AndroidPushMessageFactoryTest {

    private lateinit var mockV1Mapper: AndroidPushV1Mapper
    private lateinit var mockSilentV1Mapper: SilentAndroidPushV1Mapper
    private lateinit var mockV2Mapper: AndroidPushV2Mapper
    private lateinit var mockSilentV2Mapper: SilentAndroidPushV2Mapper

    private lateinit var factory: AndroidPushMessageFactory

    @Before
    fun setup() = runTest {
        mockV1Mapper = mockk(relaxed = true)
        mockSilentV1Mapper = mockk(relaxed = true)
        mockV2Mapper = mockk(relaxed = true)
        mockSilentV2Mapper = mockk(relaxed = true)

        factory = AndroidPushMessageFactory(mockV1Mapper, mockSilentV1Mapper, mockV2Mapper, mockSilentV2Mapper)
    }

    @Test
    fun map_shouldReturnNull() = runTest {
        coEvery { mockV1Mapper.map(any()) } returns null

        val input = buildJsonObject {
            put("noNecessaryKeys4U", JsonPrimitive("NO"))
        }

        val result = factory.create(input)

        result shouldBe null
    }

    @Test
    fun map_shouldDelegate_toSilentMapperV1() = runTest {
        val input = buildJsonObject {
            put("ems.silent", JsonPrimitive("true"))
        }

        val result = factory.create(input)

        result shouldNotBe null

        coVerify { mockSilentV1Mapper.map(any()) }
    }

    @Test
    fun map_shouldDelegate_toMapperV1() = runTest {
        val input = buildJsonObject {
            put("ems.silent", JsonPrimitive("false"))
        }

        val result = factory.create(input)

        result shouldNotBe null

        coVerify { mockV1Mapper.map(any()) }
    }

    @Test
    fun map_shouldDelegate_toSilentMapperV2() = runTest {
        val input = buildJsonObject {
            put("ems", buildJsonObject {
                put("version", JsonPrimitive("value"))
            })
            put("notification.silent", JsonPrimitive("true"))
        }

        val result = factory.create(input)

        result shouldNotBe null

        coVerify { mockSilentV2Mapper.map(any()) }
    }

    @Test
    fun map_shouldDelegate_toMapperV2() = runTest {
        val input = buildJsonObject {
            put("ems", buildJsonObject {
                put("version", JsonPrimitive("value"))
            })
            put("notification.silent", JsonPrimitive("false"))
        }

        val result = factory.create(input)

        result shouldNotBe null

        coVerify { mockV2Mapper.map(any()) }
    }

}