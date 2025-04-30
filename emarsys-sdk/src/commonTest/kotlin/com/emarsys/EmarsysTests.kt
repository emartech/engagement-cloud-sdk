package com.emarsys

import com.emarsys.core.exceptions.PreconditionFailedException
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EmarsysTests {

    @Test
    fun testEnableTracking_shouldValidateConfig() = runTest {
        val config = TestEmarsysConfig(applicationCode = "null")
        shouldThrow<PreconditionFailedException> {
            Emarsys.enableTracking(config)
        }
    }
}