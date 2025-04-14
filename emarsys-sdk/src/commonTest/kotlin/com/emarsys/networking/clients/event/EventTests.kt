package com.emarsys.networking.clients.event

import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.event.model.ack
import dev.mokkery.MockMode
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EventTests {

    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockSdkLogger: Logger

    @BeforeTest
    fun setup() {
        mockEventsDao = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
    }

    @Test
    fun ack_shouldRemoveEventFromDB() = runTest {
        val onlineSdkEvent = SdkEvent.Internal.Sdk.AppStart()

        onlineSdkEvent.ack(mockEventsDao, mockSdkLogger)

        verifySuspend {
            mockEventsDao.removeEvent(onlineSdkEvent)
        }
    }

    @Test
    fun ack_shouldLogError_whenRemovingEventFromDb() = runTest {
        val onlineSdkEvent = SdkEvent.Internal.Sdk.AppStart()
        everySuspend { mockEventsDao.removeEvent(onlineSdkEvent) } throws Exception("Error removing element from db")


        onlineSdkEvent.ack(mockEventsDao, mockSdkLogger)

        verifySuspend {
            mockEventsDao.removeEvent(onlineSdkEvent)
            mockSdkLogger.error(any(), any<Exception>(), any(), true)
        }
    }

    @Test
    fun ack_shouldLogError_onlyLocally_whenRemovingLogEventEventFromDbFails() = runTest {
        val logEvent = SdkEvent.Internal.Sdk.Log(level = LogLevel.Error)
        everySuspend { mockEventsDao.removeEvent(logEvent) } throws Exception("Error removing element from db")


        logEvent.ack(mockEventsDao, mockSdkLogger)

        verifySuspend {
            mockEventsDao.removeEvent(logEvent)
            mockSdkLogger.error(any(), any<Exception>(), any(), false)
        }
    }

    @Test
    fun ack_onList_shouldAckAllEvents() = runTest {
        val onlineSdkEvent1 = SdkEvent.Internal.Sdk.AppStart()
        val onlineSdkEvent2 = SdkEvent.Internal.Sdk.AppStart()
        val eventsList = listOf(onlineSdkEvent1, onlineSdkEvent2)

        eventsList.ack(mockEventsDao, mockSdkLogger)

        verifySuspend {
            mockEventsDao.removeEvent(onlineSdkEvent1)
            mockEventsDao.removeEvent(onlineSdkEvent2)
        }
    }

}