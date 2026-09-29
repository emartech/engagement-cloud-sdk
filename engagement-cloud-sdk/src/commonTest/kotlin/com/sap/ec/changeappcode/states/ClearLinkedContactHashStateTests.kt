package com.sap.ec.changeappcode.states

import com.sap.ec.changeappcode.states.ClearLinkedContactHashState
import com.sap.ec.core.networking.context.RequestContext
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.fake.FakeStringStorage
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClearLinkedContactHashStateTests : KoinTest {
    private companion object {
        const val CONTACT_HASH = "testContactHash"
    }

    override fun getKoin(): Koin = koin

    private lateinit var testModule: Module
    private lateinit var requestContext: RequestContextApi
    private lateinit var clearLinkedContactHashState: ClearLinkedContactHashState

    @BeforeTest
    fun setUp() {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        requestContext = RequestContext()
        requestContext.linkedContactHash = CONTACT_HASH

        clearLinkedContactHashState =
            ClearLinkedContactHashState(requestContext, sdkLogger = mock(MockMode.autofill))
    }

    @AfterTest
    fun tearDown() {
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun active_shouldClearLinkedContactHash() = runTest {
        requestContext.linkedContactHash shouldBe CONTACT_HASH

        clearLinkedContactHashState.active() shouldBe Result.success(Unit)

        requestContext.linkedContactHash shouldBe null
    }
}