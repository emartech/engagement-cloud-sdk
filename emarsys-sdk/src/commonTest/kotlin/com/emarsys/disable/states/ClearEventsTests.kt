package com.emarsys.disable.states

import com.emarsys.core.db.events.EventsDaoApi
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClearEventsTests {

    private lateinit var clearEvents: ClearEvents
    private lateinit var mockEventsDao: EventsDaoApi

    @BeforeTest
    fun setup() {
        mockEventsDao = mock(MockMode.autofill)
        clearEvents = ClearEvents(mockEventsDao)
    }

    @Test
    fun testActive_shouldDropAllEvents_fromDatabase() = runTest {
        clearEvents.active()

        verifySuspend { mockEventsDao.removeAll() }
    }
}