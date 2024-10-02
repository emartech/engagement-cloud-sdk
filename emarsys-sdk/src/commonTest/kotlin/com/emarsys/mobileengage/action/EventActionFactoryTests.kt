package com.emarsys.mobileengage.action

import com.emarsys.api.oneventaction.OnEventActionInternalApi
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.channel.DeviceEventChannelApi
import com.emarsys.core.clipboard.ClipboardHandlerApi
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.message.MsgHubApi
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.actions.AppEventAction
import com.emarsys.mobileengage.action.actions.BadgeCountAction
import com.emarsys.mobileengage.action.actions.CustomEventAction
import com.emarsys.mobileengage.action.actions.DismissAction
import com.emarsys.mobileengage.action.actions.OpenExternalUrlAction
import com.emarsys.mobileengage.action.actions.RequestPushPermissionAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicBadgeCountActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.BasicDismissActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel
import dev.mokkery.mock
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EventActionFactoryTests {

    private lateinit var mockEventChannel: DeviceEventChannelApi
    private lateinit var mockOnEventActionInternal: OnEventActionInternalApi
    private lateinit var mockPermissionHandler: PermissionHandlerApi
    private lateinit var mockBadgeCountHandler: BadgeCountHandlerApi
    private lateinit var mockExternalUrlOpener: ExternalUrlOpenerApi
    private lateinit var mockMsgHub: MsgHubApi
    private lateinit var mockClipboardHandler: ClipboardHandlerApi

    private lateinit var actionFactory: ActionFactoryApi<ActionModel>

    @BeforeTest
    fun setUp() {
        mockEventChannel = mock()
        mockOnEventActionInternal = mock()
        mockPermissionHandler = mock()
        mockBadgeCountHandler = mock()
        mockExternalUrlOpener = mock()
        mockMsgHub = mock()
        mockClipboardHandler = mock()

        actionFactory = EventActionFactory(
            mockOnEventActionInternal,
            mockEventChannel,
            mockPermissionHandler,
            mockBadgeCountHandler,
            mockExternalUrlOpener,
            mockMsgHub,
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
    fun testCreate_withBadgeCountActionModel() = runTest {
        val action = BasicBadgeCountActionModel("method", 0)

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<BadgeCountAction>()
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