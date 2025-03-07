package com.emarsys.core.log

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
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
            "test_tag", "this is a testMessage",
            buildJsonObject {
                put("testField", JsonPrimitive("testValue"))
                put("contactFieldId", JsonPrimitive(1234))
                put("contactFieldValue", JsonPrimitive("test@test.com"))
                put("details", buildJsonObject {
                    put("param1", JsonPrimitive(3))
                    put("param2", JsonPrimitive("hello"))
                })
            },
        )

        logger.error("some_method", Exception("thrown exception"))

    }

    @Test
    fun testContext() = runTest {
        withLogContext(JsonObject(mapOf())) {
            contextTestMethod1()
        }
    }

    @Test
    fun testContext2() = runTest {
        withLogContext(buildJsonObject {
            put(
                "test",
                buildJsonObject { put("key", JsonPrimitive("value")) })
        }) {
            contextTestMethod2()
        }
    }

    private suspend fun contextTestMethod1() {
        val logContext = buildJsonObject { put("key1", JsonPrimitive(123)) }
        withLogContext(logContext) {
            contextTestMethod2("testParam")
        }
    }

    private suspend fun contextTestMethod2(param: String = "param") {
        val logContext = buildJsonObject {
            put("key2", JsonPrimitive("456"))
            put("methodParam", JsonPrimitive(param))
        }

        withLogContext(logContext) {
            contextTestMethod3()
        }
    }

    private suspend fun contextTestMethod3() {
        val logContext = buildJsonObject { put("key3", JsonPrimitive("789")) }
        withLogContext(logContext) {
            logger.debug("test", "check if context works")
        }
    }
}