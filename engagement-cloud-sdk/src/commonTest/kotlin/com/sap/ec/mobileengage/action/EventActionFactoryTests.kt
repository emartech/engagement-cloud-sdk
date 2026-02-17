package com.sap.ec.mobileengage.action

import com.sap.ec.core.actions.badge.BadgeCountHandlerApi
import com.sap.ec.core.actions.clipboard.ClipboardHandlerApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.SdkLogger
import com.sap.ec.core.permission.PermissionHandlerApi
import com.sap.ec.core.url.ExternalUrlOpenerApi
import com.sap.ec.mobileengage.action.actions.AppEventAction
import com.sap.ec.mobileengage.action.actions.CustomEventAction
import com.sap.ec.mobileengage.action.actions.DismissAction
import com.sap.ec.mobileengage.action.actions.OpenExternalUrlAction
import com.sap.ec.mobileengage.action.actions.RequestPushPermissionAction
import com.sap.ec.mobileengage.action.actions.RichContentDisplayAction
import com.sap.ec.mobileengage.action.models.BasicAppEventActionModel
import com.sap.ec.mobileengage.action.models.BasicCustomEventActionModel
import com.sap.ec.mobileengage.action.models.BasicDismissActionModel
import com.sap.ec.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.sap.ec.mobileengage.action.models.BasicRichContentDisplayActionModel
import com.sap.ec.mobileengage.action.models.RequestPushPermissionActionModel
import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessageAnimation
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EventActionFactoryTests {
    private companion object {
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockPermissionHandler: PermissionHandlerApi
    private lateinit var mockBadgeCountHandler: BadgeCountHandlerApi
    private lateinit var mockExternalUrlOpener: ExternalUrlOpenerApi
    private lateinit var mockClipboardHandler: ClipboardHandlerApi

    private lateinit var actionFactory: EventActionFactoryApi

    @BeforeTest
    fun setUp() {
        mockSdkEventDistributor = mock()
        mockPermissionHandler = mock()
        mockBadgeCountHandler = mock()
        mockExternalUrlOpener = mock()
        mockClipboardHandler = mock()

        actionFactory = EventActionFactory(
            mockSdkEventDistributor,
            mockPermissionHandler,
            mockExternalUrlOpener,
            mockClipboardHandler,
            SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock())
        )
    }

    @Test
    fun testCreate_withAppEventActionModel() = runTest {
        val action = BasicAppEventActionModel(REPORTING, "name", null)

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<AppEventAction>()
    }

    @Test
    fun testCreate_withCustomEventActionModel() = runTest {
        val action = BasicCustomEventActionModel(REPORTING, "name", null)

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
        val action = BasicOpenExternalUrlActionModel(REPORTING, "url")

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<OpenExternalUrlAction>()
    }

    @Test
    fun testCreate_withBasicRichContentDisplayModel() = runTest {
        val action = BasicRichContentDisplayActionModel(
            REPORTING,
            "url",
            animation = EmbeddedMessageAnimation.FADING_FROM_BOTTOM
        )

        val result = actionFactory.create(action)

        result shouldNotBe null
        result.shouldBeTypeOf<RichContentDisplayAction>()
    }
}