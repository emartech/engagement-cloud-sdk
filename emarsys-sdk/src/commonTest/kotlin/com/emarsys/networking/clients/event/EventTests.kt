package com.emarsys.networking.clients.event

import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
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
            mockSdkLogger.error(any(), any<Exception>(), any())
        }
    }

}