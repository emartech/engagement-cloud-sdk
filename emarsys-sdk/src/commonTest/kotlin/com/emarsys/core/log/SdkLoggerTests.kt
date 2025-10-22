package com.emarsys.core.log

import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.SdkLogger.Companion.breadcrumbsQueue
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetCalls
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SdkLoggerTests {

    companion object {
        private val LOGGER_NAME = SdkLoggerTests::class.simpleName!!
    }

    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockRemoteLogger: RemoteLoggerApi
    private lateinit var logger: Logger

    @BeforeTest
    fun setup() = runTest {
        mockSdkContext = mock()
        every { mockSdkContext.logBreadcrumbsQueueSize } returns 10
        mockRemoteLogger = mock(MockMode.autofill)
        logger = SdkLogger(
            LOGGER_NAME,
            mock(MockMode.autofill),
            remoteLogger = mockRemoteLogger,
            sdkContext = mockSdkContext
        )
    }

    @AfterTest
    fun tearDown() = runTest {
        breadcrumbsQueue.clear()
    }

    @Test
    fun testLogger() = runTest {
        logger.info("this is an info log")

        logger.debug(
            "this is a testMessage",
            buildJsonObject {
                put("testField", JsonPrimitive("testValue"))
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

    @Test
    fun testBreadCrumbsQueue_shouldFillUp_whenThereIsMoreThanConfiguredParallelLogs() = runTest {
        val testLogBreadcrumbsQueueSize = 10
        every { mockSdkContext.logBreadcrumbsQueueSize } returns testLogBreadcrumbsQueueSize

        val numberOfLogs = mockSdkContext.logBreadcrumbsQueueSize + 5
        val jobs = (0..numberOfLogs).map {
            CoroutineScope(Dispatchers.Default + CoroutineName("coroutine $it")).launch {
                logger.info("log number $it")
            }
        }

        jobs.joinAll()

        breadcrumbsQueue.size shouldBe testLogBreadcrumbsQueueSize
    }

    @Test
    fun testBreadCrumbsQueue_shouldContainLatestArrivingLogs() = runTest {
        val testLogBreadcrumbsQueueSize = 10
        every { mockSdkContext.logBreadcrumbsQueueSize } returns testLogBreadcrumbsQueueSize

        val numberOfLogs = mockSdkContext.logBreadcrumbsQueueSize + 5
        (0..numberOfLogs).map {
            logger.info("log number $it")
        }

        breadcrumbsQueue.size shouldBe testLogBreadcrumbsQueueSize
        breadcrumbsQueue.forEachIndexed { i, breadcrumb ->
            breadcrumb.second["message"]?.jsonPrimitive?.content shouldBe "log number ${numberOfLogs - i}"
        }
    }

    @Test
    fun logToRemote_shouldContainAllAttributes() = runTest {
        val breadcrumbTestMessage = "breadcrumb test message"
        logger.info(breadcrumbTestMessage)
        resetCalls(mockRemoteLogger)

        val testMessage = "test message"
        val testException = RuntimeException("test exception")
        val expectedLogObject = buildJsonObject {
            put("loggerName", LOGGER_NAME)
            put("level", LogLevel.Error.name.uppercase())
            put("message", testMessage)
            put("exception", testException.toString())
            put("reason", testException.message)
            put("stackTrace", testException.stackTraceToString())
            put("contextKey1", 123)
            put("contextKey2", "456")
            put("key1", "value")
            put("key2", buildJsonObject { put("innerKey", "innerValue") })
            put("breadcrumbs", buildJsonObject {
                put("entry_0", buildJsonObject {
                    put("loggerName", LOGGER_NAME)
                    put("message", breadcrumbTestMessage)
                    put("level", LogLevel.Info.name.uppercase())
                })
            })

        }

        val logContext = buildJsonObject {
            put("contextKey1", 123)
            put("contextKey2", "456")
        }
        withLogContext(logContext) {
            logger.error(
                "test message",
                testException,
                data = buildJsonObject {
                    put("key1", "value")
                    put("key2", buildJsonObject { put("innerKey", "innerValue") })
                })
        }

        verifySuspend {
            mockRemoteLogger.logToRemote(
                LogLevel.Error, expectedLogObject
            )
        }
    }

    @Test
    fun sdkLogger_shouldNotCollectBreadcrumbs_inCaseThereIsNoRemoteLogger() = runTest {
        val logger = SdkLogger(
            LOGGER_NAME,
            mock(MockMode.autofill),
            remoteLogger = null,
            sdkContext = mockSdkContext
        )

        logger.info("test")

        breadcrumbsQueue.size shouldBe 0
        verifySuspend(VerifyMode.exactly(0)) {
            mockRemoteLogger.logToRemote(LogLevel.Info, any())
        }
    }

    @Test
    fun sdkLogger_shouldNotLogToRemote_ifIsRemoteLogFlag_isFalse() = runTest {
        val logger = SdkLogger(
            LOGGER_NAME,
            mock(MockMode.autofill),
            remoteLogger = mockRemoteLogger,
            sdkContext = mockSdkContext
        )

        logger.info("test", isRemoteLog = false)

        verifySuspend(VerifyMode.exactly(0)) {
            mockRemoteLogger.logToRemote(LogLevel.Info, any())
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
            logger.debug("check if context works")
        }
    }
}