package com.emarsys.mobileengage.inapp.providers

import com.emarsys.core.providers.Provider
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import platform.UIKit.UIScene
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WindowProviderTest {
    private lateinit var mockSceneProvider: Provider<UIScene>
    private lateinit var mockViewControllerProvider: Provider<UIViewController>
    private lateinit var mainDispatcher: CoroutineDispatcher

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())

        mockSceneProvider = mock()
        mockViewControllerProvider = mock()
        mainDispatcher = StandardTestDispatcher()
        every { mockSceneProvider.provide() } returns UIWindowScene()
        every { mockViewControllerProvider.provide() } returns UIViewController()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun testProvideReturnsUIWindow() = runTest {
        val windowProvider =
            WindowProvider(mockSceneProvider, mockViewControllerProvider, mainDispatcher)

        val window = windowProvider.provide()

        window shouldNotBe null
    }

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun testProvideReturnsUIWindowWithCorrectFrameAndViewController() = runTest {
        val windowProvider =
            WindowProvider(mockSceneProvider, mockViewControllerProvider, mainDispatcher)

        val window = windowProvider.provide()

        window shouldNotBe null

        val screenBounds = UIScreen.mainScreen.bounds

        window.frame shouldBe screenBounds

        val rootViewController = window.rootViewController

        rootViewController shouldNotBe null
    }


}