package com.emarsys.core.channel

import com.emarsys.event.SdkEvent
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class SdkEventWaiterTests {
    private companion object {
        const val ORIGIN_ID_1 = "originId1"
        const val ORIGIN_ID_2 = "originId2"
        val testException = Exception("test error")
        val testEventResult = Result.success(Unit)
        val testEventResult2 = Result.failure<Exception>(testException)
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var sdkEvent: SdkEvent
    private lateinit var mockConnectionStatus: StateFlow<Boolean>
    private lateinit var sharedFlow: MutableSharedFlow<SdkEvent>

    @BeforeTest
    fun setup() {
        mockSdkEventDistributor = mock()
        mockConnectionStatus = mock()
        sharedFlow = MutableSharedFlow()
    }

    @Test
    fun await_shouldReturn_answerResponse_whenAMatchingEventIsEmitted_andConnectionStatus_isTrue() =
        runTest {
            sdkEvent = SdkEvent.Internal.Sdk.AppStart(id = ORIGIN_ID_1)

            val testAnswer = SdkEvent.Internal.Sdk.Answer.Response(
                ORIGIN_ID_1,
                testEventResult
            )

            everySuspend { mockConnectionStatus.value } returns true
            everySuspend { mockSdkEventDistributor.sdkEventFlow } returns sharedFlow

            val testWaiter = SdkEventWaiter(mockSdkEventDistributor, sdkEvent, mockConnectionStatus)

            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                val result = testWaiter.await<Result<Unit>>()
                result.result shouldBe testEventResult
            }

            sharedFlow.emit(testAnswer)
        }

    @Test
    fun await_shouldReturn_answerResponse_whenAMatchingEventIsEmitted_andIgnoreOtherResults_andConnectionStatus_isTrue() =
        runTest {
            sdkEvent = SdkEvent.Internal.Sdk.AppStart(id = ORIGIN_ID_1)

            val testAnswer = SdkEvent.Internal.Sdk.Answer.Response(
                ORIGIN_ID_1,
                testEventResult
            )
            val testAnswer2 = SdkEvent.Internal.Sdk.Answer.Response(
                ORIGIN_ID_2,
                testEventResult2
            )

            everySuspend { mockConnectionStatus.value } returns true
            everySuspend { mockSdkEventDistributor.sdkEventFlow } returns sharedFlow

            val testWaiter = SdkEventWaiter(mockSdkEventDistributor, sdkEvent, mockConnectionStatus)

            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
                val result = testWaiter.await<Result<Unit>>()
                result.result shouldBe testEventResult
            }

            sharedFlow.emit(testAnswer2)

            sharedFlow.emit(testAnswer)
        }

    @Test
    fun await_shouldReturn_answerResponse_withFailureResult_whenEventIsWaited_andConnectionStatus_isFalse() =
        runTest {
            sdkEvent = SdkEvent.Internal.Sdk.AppStart(id = ORIGIN_ID_1)

            everySuspend { mockConnectionStatus.value } returns false
            everySuspend { mockSdkEventDistributor.sdkEventFlow } returns sharedFlow

            val testWaiter = SdkEventWaiter(mockSdkEventDistributor, sdkEvent, mockConnectionStatus)

            val result = testWaiter.await<Result<Unit>>()

            result.result.isFailure shouldBe true
            result.result.exceptionOrNull()?.message shouldBe "No internet connection. Event: ${sdkEvent.type} is stored and will be processed when connection is restored."
        }
}