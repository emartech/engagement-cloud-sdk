package com.sap.ec.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.sap.ec.api.push.PushApi
import com.sap.ec.api.push.PushConstants
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorage
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.di.DispatcherTypes
import com.sap.ec.di.SdkKoinIsolationContext.koin
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest

@OptIn(ExperimentalCoroutinesApi::class)
class PushTokenBroadcastReceiverTest: KoinTest  {
    override fun getKoin(): Koin = koin

    private companion object {
        const val PUSH_TOKEN = "testToken"
    }

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var broadcastReceiver: PushTokenBroadcastReceiver
    private lateinit var mockPushApi: PushApi
    private lateinit var mockStringStorage: StringStorage
    private lateinit var mockLogger: Logger

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        sdkDispatcher = StandardTestDispatcher()

        mockStringStorage = mockk(relaxed = true)
        mockPushApi = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)

        val testModules = module {
            single<PushApi> { mockPushApi }
            single<StringStorageApi> { mockStringStorage }
            single<CoroutineDispatcher>(named(DispatcherTypes.Sdk)) { sdkDispatcher }
            single<Logger> { mockLogger }
        }
        koin.loadModules(listOf(testModules))

        broadcastReceiver = PushTokenBroadcastReceiver()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun testOnReceive_shouldCallPushApiWithToken() = runTest {
        val intent = Intent().apply {
            action = "com.sap.ec.sdk.PUSH_TOKEN"
            putExtra("pushToken", PUSH_TOKEN)
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify { mockPushApi.registerPushToken(PUSH_TOKEN) }
    }

    @Test
    fun testOnReceive_shouldCallPut_onStringStorage_withToken() = runTest {
        val intent = Intent().apply {
            action = "com.sap.ec.sdk.PUSH_TOKEN"
            putExtra("pushToken", PUSH_TOKEN)
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify { mockStringStorage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) }
    }
}