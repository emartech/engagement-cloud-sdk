package com.emarsys.context

import SdkContext
import com.emarsys.EmarsysConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import kotlin.math.exp
import kotlin.test.BeforeTest
import kotlin.test.Test

class SdkContextTests {
    lateinit var sdkContext: SdkContext

    @BeforeTest
    fun setUp() {
        sdkContext = SdkContext()
        sdkContext.config = EmarsysConfig("EMS11-C3FD3")
    }

    @Test
    fun testCreateUrl_should_create_valid_url() {
        val result = sdkContext.createUrl(
            baseUrl = "https://me-client.eservice.emarsys.net",
            version = "v3",
            withAppCode = true,
            path = "/client/push-token"
        )
        val expected = Url("https://me-client.eservice.emarsys.net/v3/apps/EMS11-C3FD3/client/push-token")

        result shouldBe expected
    }

    @Test
    fun testCreateUrl_should_create_valid_url_without_appcode() {
        val result = sdkContext.createUrl(
            baseUrl = "https://me-client.eservice.emarsys.net",
            version = "v3",
            withAppCode = false,
            path = "/client/push-token"
        )
        val expected = Url("https://me-client.eservice.emarsys.net/v3/client/push-token")

        result shouldBe expected
    }

    @Test
    fun testCreateUrl_should_create_valid_url_with_different_data() {
        val result = sdkContext.createUrl(
            baseUrl = "https://me-event.eservice.emarsys.net",
            version = "v1",
            withAppCode = false,
            path = "/client/contact"
        )
        val expected = Url("https://me-event.eservice.emarsys.net/v1/client/contact")

        result shouldBe expected
    }

    @Test
    fun testCreateUrl_should_throw_illegalArgumentException_when_config_isMissing() {
        shouldThrow<IllegalArgumentException> {
            SdkContext().createUrl(
                baseUrl = "https://me-client.eservice.emarsys.net",
                version = "v3",
                withAppCode = true,
                path = "/client/push-token"
            )
        }
    }

}