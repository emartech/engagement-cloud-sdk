package com.emarsys.api.event

import com.emarsys.api.SdkState
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.fake.FakeStringStorage
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventTrackerTests: KoinTest {

    override fun getKoin(): Koin = koin

    private companion object {
        val event = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
    }

    private lateinit var testModule: Module

    private lateinit var mockLoggingEventTracker: EventTrackerInstance
    private lateinit var mockEventTrackerGatherer: EventTrackerInstance
    private lateinit var mockEventTrackerInternal: EventTrackerInstance
    private lateinit var sdkContext: SdkContextApi
    private lateinit var eventTracker: EventTracker<EventTrackerInstance, EventTrackerInstance, EventTrackerInstance>

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    @BeforeTest
    fun setup() = runTest {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        mockLoggingEventTracker = mock()
        mockEventTrackerGatherer = mock()
        mockEventTrackerInternal = mock()

        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )

        everySuspend { mockLoggingEventTracker.activate() } returns Unit
        everySuspend { mockEventTrackerGatherer.activate() } returns Unit
        everySuspend { mockEventTrackerInternal.activate() } returns Unit

        eventTracker =
            EventTracker(
                mockLoggingEventTracker,
                mockEventTrackerGatherer,
                mockEventTrackerInternal,
                sdkContext
            )
        eventTracker.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun testTrackEvent_inactiveState() = runTest {
        everySuspend {
            mockLoggingEventTracker.trackEvent(event)
        } returns Unit

        eventTracker.trackEvent(event)

        verifySuspend {
            mockLoggingEventTracker.trackEvent(event)
        }
    }

    @Test
    fun testTrackEvent_onHoldState() = runTest {
        everySuspend {
            mockEventTrackerGatherer.trackEvent(event)
        } returns Unit


        sdkContext.setSdkState(SdkState.OnHold)
        eventTracker.trackEvent(event)

        verifySuspend {
            mockEventTrackerGatherer.trackEvent(event)
        }
    }

    @Test
    fun testTrackEvent_activeState() = runTest {
        everySuspend {
            mockEventTrackerInternal.trackEvent(event)
        } returns Unit

        sdkContext.setSdkState(SdkState.Active)
        eventTracker.trackEvent(event)

        verifySuspend {
            mockEventTrackerInternal.trackEvent(event)
        }
    }

    @Test
    fun testTrackEvent_activeState_shouldReturnErrorInResult() = runTest {
        val expectException = Exception()
        everySuspend {
            mockEventTrackerInternal.trackEvent(event)
        } throws expectException

        sdkContext.setSdkState(SdkState.Active)
        val result = eventTracker.trackEvent(event)

        result.exceptionOrNull() shouldBe expectException
    }
}