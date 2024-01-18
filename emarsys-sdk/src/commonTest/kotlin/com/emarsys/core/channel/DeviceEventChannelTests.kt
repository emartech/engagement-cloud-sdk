package com.emarsys.core.channel

import com.emarsys.core.networking.clients.event.model.Event
import com.emarsys.core.networking.clients.event.model.EventType
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeviceEventChannelTests {

    private companion object {
        val testEvent = Event(
            EventType.CUSTOM,
            "test name",
            null
        )
    }

    private lateinit var testChannel: Channel<Event>
    private lateinit var deviceEventChannel: DeviceEventChannel

    @BeforeTest
    fun setup() {
        testChannel = Channel()
        deviceEventChannel = DeviceEventChannel(testChannel)
    }

    @AfterTest
    fun tearDown() {
        testChannel.cancel()
    }

    @Test
    fun testSend_should_send_event_to_channel() = runTest {
        launch {
            deviceEventChannel.send(testEvent)
        }

        val result = testChannel.receive()

        result shouldBe testEvent
    }

    @Test
    fun testConsume_should_return_flow_of_events() = runTest {
        launch {
            deviceEventChannel.send(testEvent)
        }

        val result = deviceEventChannel.consume()

        result.first() shouldBe testEvent
    }
}