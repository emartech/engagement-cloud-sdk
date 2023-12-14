package com.emarsys

import com.emarsys.core.exceptions.PreconditionFailed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EmarsysTests {

    @Test
    fun testEnableTracking_shouldValidateConfig() = runTest {
        val config = EmarsysConfig(applicationCode = "null")
        shouldThrow<PreconditionFailed> {
            Emarsys.enableTracking(config)
        }
    }
}