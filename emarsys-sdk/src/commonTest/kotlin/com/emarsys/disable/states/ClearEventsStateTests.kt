package com.emarsys.disable.states

import com.emarsys.core.db.events.EventsDaoApi
import dev.mokkery.MockMode
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClearEventsStateTests {

    private lateinit var clearEventsState: ClearEventsState
    private lateinit var mockEventsDao: EventsDaoApi

    @BeforeTest
    fun setup() {
        mockEventsDao = mock(MockMode.autofill)
        clearEventsState = ClearEventsState(mockEventsDao)
    }

    @Test
    fun testActive_shouldDropAllEvents_fromDatabase() = runTest {
        val result = clearEventsState.active()

        verifySuspend { mockEventsDao.removeAll() }
        result shouldBe Result.success(Unit)
    }

    @Test
    fun testActive_shouldReturn_failure_ifDBOperation_fails() = runTest {
        val testException = Exception("failure")
        everySuspend { mockEventsDao.removeAll() } throws testException

        val result = clearEventsState.active()

        verifySuspend { mockEventsDao.removeAll() }
        result shouldBe Result.failure(testException)
    }
}