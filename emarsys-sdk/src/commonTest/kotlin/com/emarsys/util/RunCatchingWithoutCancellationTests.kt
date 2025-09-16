package com.emarsys.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class RunCatchingWithoutCancellationTests {

    @Test
    fun runCatchingWithoutCancellation_shouldReturn_theResultOfTheProvidedBlock() = runTest {
        val testBlock = { "testResult" }

        val result = runCatchingWithoutCancellation { testBlock() }

        result shouldBe Result.success("testResult")
    }

    @Test
    fun runCatchingWithoutCancellation_shouldReturn_failure_whenBlockThrowsException() = runTest {
        val testException = Exception("testError")
        val testBlock = { throw testException }

        val result = runCatchingWithoutCancellation { testBlock() }

        result shouldBe Result.failure(testException)
    }

    @Test
    fun runCatchingWithoutCancellation_shouldRethrow_cancellationExceptions() = runTest {
        val testException = CancellationException("testError")
        val testBlock = { throw testException }

        shouldThrow<CancellationException> { runCatchingWithoutCancellation { testBlock() } }

    }
}