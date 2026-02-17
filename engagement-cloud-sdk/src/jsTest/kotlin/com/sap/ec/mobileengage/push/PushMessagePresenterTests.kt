package com.sap.ec.mobileengage.push

import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.sap.ec.mobileengage.push.model.JsPlatformData
import com.sap.ec.mobileengage.push.model.JsPushMessage
import com.sap.ec.mobileengage.push.model.WebPushNotificationData
import com.sap.ec.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.w3c.notifications.NotificationAction
import org.w3c.notifications.NotificationOptions
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushMessagePresenterTests {

    companion object {
        private const val ICON = "icon"
        private const val IMAGE = "image"
        private const val REPORTING = """{"reportingKey":"reportingValue"}"""
        private const val REPORTING2 = """{"reportingKey2":"reportingValue2"}"""
    }

    private val slot = Capture.slot<WebPushNotificationData>()
    private lateinit var mockWebPushNotificationPresenter: WebPushNotificationPresenterApi
    private lateinit var pushMessagePresenter: PushMessagePresenter

    @BeforeTest
    fun setup() {
        mockWebPushNotificationPresenter = mock()
        everySuspend {
            mockWebPushNotificationPresenter.showNotification(
                capture(slot)
            )
        } returns Unit

        pushMessagePresenter = PushMessagePresenter(mockWebPushNotificationPresenter)
    }

    @Test
    fun present_shouldCallShowNotification_withOnlyTitleAndBody_whenNothingElseIsPresent() =
        runTest {
            val testPushMessage = getTestJsPushMessage()
            val expectedNotificationOptions: NotificationOptions =
                getTestNotificationOptions(testPushMessage, emptyArray())

            pushMessagePresenter.present(testPushMessage)

            val slotCapture = slot.get()
            slotCapture.title shouldBe testPushMessage.displayableData?.title
            JSON.stringify(slotCapture.options) shouldBe JSON.stringify(expectedNotificationOptions)
        }

    @Test
    fun present_shouldCallShowNotification_withIcon_whenPresent() = runTest {
        val testPushMessage = getTestJsPushMessage(icon = ICON)
        val expectedNotificationOptions: NotificationOptions =
            getTestNotificationOptions(testPushMessage, emptyArray())

        pushMessagePresenter.present(testPushMessage)

        slot.get().title shouldBe testPushMessage.displayableData?.title
        JSON.stringify(slot.get().options) shouldBe JSON.stringify(expectedNotificationOptions)
    }

    @Test
    fun present_shouldCallShowNotification_withImage_whenPresent() = runTest {
        val testPushMessage = getTestJsPushMessage(image = IMAGE)
        val expectedNotificationOptions: NotificationOptions =
            getTestNotificationOptions(testPushMessage, emptyArray())

        pushMessagePresenter.present(testPushMessage)

        slot.get().title shouldBe testPushMessage.displayableData?.title
        JSON.stringify(slot.get().options) shouldBe JSON.stringify(expectedNotificationOptions)
    }

    @Test
    fun present_shouldCallShowNotification_withActionMappedCorrectly() = runTest {
        val testActions = listOf(
            PresentableOpenExternalUrlActionModel(
                id = "actionId1",
                reporting = REPORTING,
                title = "actionTitle1",
                url = "actionUrl1"
            ),
            PresentableOpenExternalUrlActionModel(
                id = "actionId2",
                reporting = REPORTING2,
                title = "actionTitle2",
                url = "actionUrl2"
            )
        )
        val testPushMessage = getTestJsPushMessage(actions = testActions)
        val expectedNotificationOptions: NotificationOptions =
            getTestNotificationOptions(
                testPushMessage, arrayOf(
                js("{}").unsafeCast<NotificationAction>().apply {
                    action = "actionId1"
                    title = "actionTitle1"
                },
                js("{}").unsafeCast<NotificationAction>().apply {
                    action = "actionId2"
                    title = "actionTitle2"
                }
            ))

        pushMessagePresenter.present(testPushMessage)

        slot.get().title shouldBe testPushMessage.displayableData?.title
        JSON.stringify(slot.get().options) shouldBe JSON.stringify(expectedNotificationOptions)
    }

    private fun getTestJsPushMessage(
        icon: String? = null,
        image: String? = null,
        actions: List<PresentableActionModel> = emptyList(),
    ): JsPushMessage {
        return JsPushMessage(
            trackingInfo = """{"trackingInfoKey":"trackingInfoValue"}""",
            platformData = JsPlatformData,
            badgeCount = null,
            actionableData = ActionableData(
                actions = actions,
                defaultTapAction = null
            ),
            displayableData = DisplayableData(
                title = "title",
                body = "body",
                iconUrlString = icon,
                imageUrlString = image
            )
        )
    }

    private fun getTestNotificationOptions(
        testPushMessage: JsPushMessage,
        notificationActions: Array<NotificationAction>
    ): NotificationOptions {
        val notificationOptions: NotificationOptions =
            js("{}").unsafeCast<NotificationOptions>().apply {
                body = testPushMessage.displayableData?.body
                icon = testPushMessage.displayableData?.iconUrlString
                badge = testPushMessage.displayableData?.imageUrlString
                tag = testPushMessage.trackingInfo
                actions = notificationActions
                data = JsonUtil.json.encodeToString<JsPushMessage>(testPushMessage)
            }
        return notificationOptions
    }
}