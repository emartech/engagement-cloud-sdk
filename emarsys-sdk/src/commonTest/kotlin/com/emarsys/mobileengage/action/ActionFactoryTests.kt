package com.emarsys.mobileengage.action

import com.emarsys.api.oneventaction.OnEventActionInternalApi
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.channel.DeviceEventChannelApi
import com.emarsys.core.message.MsgHubApi
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.actions.AppEventAction
import com.emarsys.mobileengage.action.actions.AskForPushPermissionAction
import com.emarsys.mobileengage.action.actions.BadgeCountAction
import com.emarsys.mobileengage.action.actions.CustomEventAction
import com.emarsys.mobileengage.action.actions.DismissAction
import com.emarsys.mobileengage.action.actions.OpenExternalUrlAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.AppEventActionModel
import com.emarsys.mobileengage.action.models.AskForPushPermissionActionModel
import com.emarsys.mobileengage.action.models.BadgeCountActionModel
import com.emarsys.mobileengage.action.models.CustomEventActionModel
import com.emarsys.mobileengage.action.models.DismissActionModel
import com.emarsys.mobileengage.action.models.OpenExternalUrlActionModel
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class ActionFactoryTests : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockEventChannel: DeviceEventChannelApi

    @Mock
    lateinit var mockOnEventActionInternal: OnEventActionInternalApi

    @Mock
    lateinit var mockPermissionHandler: PermissionHandlerApi

    @Mock
    lateinit var mockBadgeCountHandler: BadgeCountHandlerApi

    @Mock
    lateinit var mockExternalUrlOpener: ExternalUrlOpenerApi

    @Mock
    lateinit var mockMsgHub: MsgHubApi

    private val actionFactory: ActionFactoryApi<ActionModel> by withMocks {
        ActionFactory(
            mockOnEventActionInternal,
            mockEventChannel,
            mockPermissionHandler,
            mockBadgeCountHandler,
            mockExternalUrlOpener,
            mockMsgHub
        )
    }

    @Test
    fun testCreate_withAppEventActionModel() = runTest {
        val action = AppEventActionModel("type", "name", null)

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<AppEventAction>()
    }

    @Test
    fun testCreate_withCustomEventActionModel() = runTest {
        val action = CustomEventActionModel("type", "name", null)

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<CustomEventAction>()
    }

    @Test
    fun testCreate_withAskForPushPermissionActionModel() = runTest {
        val action = AskForPushPermissionActionModel("type")

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<AskForPushPermissionAction>()
    }

    @Test
    fun testCreate_withBadgeCountActionModel() = runTest {
        val action = BadgeCountActionModel("type", "method", 0)

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<BadgeCountAction>()
    }

    @Test
    fun testCreate_withDismissActionModel() = runTest {
        val action = DismissActionModel("type", "testTopic")

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<DismissAction>()
    }

    @Test
    fun testCreate_withOpenExternalUrlActionModel() = runTest {
        val action = OpenExternalUrlActionModel("type", "url")

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<OpenExternalUrlAction>()
    }

}