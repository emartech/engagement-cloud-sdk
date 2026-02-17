package com.sap.ec.iosNotificationService.provider

import io.kotest.matchers.shouldNotBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class UUIDProviderTests {

    private lateinit var uuidProvider: UUIDProvider

    @BeforeTest
    fun setup() {
        uuidProvider = UUIDProvider()
    }

    @Test
    fun provide_shouldReturnAnUUIDObject() {
        val uuid = uuidProvider.provide()
        val uuidString = uuid.UUIDString()

        uuid shouldNotBe null
        uuidString shouldNotBe null
    }

    @Test
    fun provide_should_returnDifferentResults() {
        val uuid = uuidProvider.provide()
        val uuid2 = uuidProvider.provide()

        uuid shouldNotBe uuid2
    }

}
