package com.emarsys.core.providers

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class UUIDProviderTests {
    companion object {
        val uuidRegex = """[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89ab][a-f0-9]{3}-[a-f0-9]{12}""".toRegex(RegexOption.IGNORE_CASE)
    }

    private lateinit var provider: UUIDProvider

    @BeforeTest
    fun setup() = runTest {
        provider = UUIDProvider()
    }

    @Test
    fun testProvide_resultShouldNotBeNull() = runTest {
        val result = provider.provide()

        result shouldNotBe null
    }

    @Test
    fun testProvide_resultShouldBeInUUIDFormat() = runTest {
        val uuid = provider.provide()

        val result = uuidRegex.matches(uuid)

        result shouldBe true
    }

}