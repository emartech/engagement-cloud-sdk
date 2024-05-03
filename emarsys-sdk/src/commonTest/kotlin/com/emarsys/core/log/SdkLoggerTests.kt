package com.emarsys.core.log

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SdkLoggerTests {
    private lateinit var logger: SdkLogger

    @BeforeTest
    fun setup() = runTest {
        logger = SdkLogger(ConsoleLogger())
    }

    @Test
    fun testLogger() = runTest {
        logger.info("info log topic", "this is an info log")

        logger.debug(
            "test_tag", "this is a testMessage", mapOf(
                "contactFieldId" to 1234,
                "contactFieldValue" to "test@test.com",
                "details" to mapOf(
                    "param1" to 3,
                    "param2" to "hello"
                ).toString()
            )
        )

        logger.error("some_method", Exception("thrown exception"))

    }

    @Test
    fun testContext() = runTest {
            withLogContext(emptyMap()) {
                contextTestMethod1()
            }
    }

    @Test
    fun testContext2() = runTest {
            withLogContext(mapOf("test" to mapOf("key" to "value"))) {
                contextTestMethod2()
            }
    }

    private suspend fun contextTestMethod1() {
        val logContext = mapOf("key1" to 123)
        withLogContext(logContext) {
            contextTestMethod2("testParam")
        }
    }

    private suspend fun contextTestMethod2(param: String = "param") {
        val logContext = mapOf("key2" to "456", "methodParam" to param)

        withLogContext(logContext) {
            contextTestMethod3()
        }
    }

    private suspend fun contextTestMethod3() {
        val logContext = mapOf("key3" to "789")
        withLogContext(logContext) {
            logger.debug("test", "check if context works")
        }
    }
}