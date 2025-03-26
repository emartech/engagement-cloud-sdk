package com.emarsys.mobileengage.action

import com.emarsys.core.actions.badge.BadgeCountHandlerApi
import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.actions.AppEventAction
import com.emarsys.mobileengage.action.actions.CustomEventAction
import com.emarsys.mobileengage.action.actions.DismissAction
import com.emarsys.mobileengage.action.actions.OpenExternalUrlAction
import com.emarsys.mobileengage.action.actions.RequestPushPermissionAction
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.BasicDismissActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.mock
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EventActionFactoryTests {

    private lateinit var mockSdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var mockPermissionHandler: PermissionHandlerApi
    private lateinit var mockBadgeCountHandler: BadgeCountHandlerApi
    private lateinit var mockExternalUrlOpener: ExternalUrlOpenerApi
    private lateinit var mockClipboardHandler: ClipboardHandlerApi

    private lateinit var actionFactory: EventActionFactoryApi

    @BeforeTest
    fun setUp() {
        mockSdkEventFlow = mock()
        mockPermissionHandler = mock()
        mockBadgeCountHandler = mock()
        mockExternalUrlOpener = mock()
        mockClipboardHandler = mock()

        actionFactory = EventActionFactory(
            mockSdkEventFlow,
            mockPermissionHandler,
            mockExternalUrlOpener,
            mockClipboardHandler,
            SdkLogger(ConsoleLogger())
        )
    }

    @Test
    fun testCreate_withAppEventActionModel() = runTest {
        val action = BasicAppEventActionModel("name", null)

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<AppEventAction>()
    }

    @Test
    fun testCreate_withCustomEventActionModel() = runTest {
        val action = BasicCustomEventActionModel("name", null)

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<CustomEventAction>()
    }

    @Test
    fun testCreate_withAskForPushPermissionActionModel() = runTest {
        val action = RequestPushPermissionActionModel

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<RequestPushPermissionAction>()
    }

    @Test
    fun testCreate_withDismissActionModel() = runTest {
        val action = BasicDismissActionModel("testTopic")

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<DismissAction>()
    }

    @Test
    fun testCreate_withOpenExternalUrlActionModel() = runTest {
        val action = BasicOpenExternalUrlActionModel("url")

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<OpenExternalUrlAction>()
    }

}