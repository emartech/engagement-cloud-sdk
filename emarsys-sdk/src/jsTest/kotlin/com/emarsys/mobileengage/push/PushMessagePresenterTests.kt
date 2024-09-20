package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.mobileengage.inapp.PushToInApp
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.mobileengage.push.model.WebPushNotificationData
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import org.w3c.notifications.NotificationAction
import org.w3c.notifications.NotificationOptions
import kotlin.test.BeforeTest
import kotlin.test.Test


class PushMessagePresenterTests {

    companion object {
        private const val ICON = "icon"
        private const val IMAGE = "image"
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
            slotCapture.title shouldBe testPushMessage.title
            JSON.stringify(slotCapture.options) shouldBe JSON.stringify(expectedNotificationOptions)
        }

    @Test
    fun present_shouldCallShowNotification_withIcon_whenPresent() = runTest {
        val testPushMessage = getTestJsPushMessage(icon = ICON)
        val expectedNotificationOptions: NotificationOptions =
            getTestNotificationOptions(testPushMessage, emptyArray())

        pushMessagePresenter.present(testPushMessage)

        slot.get().title shouldBe testPushMessage.title
        JSON.stringify(slot.get().options) shouldBe JSON.stringify(expectedNotificationOptions)
    }

    @Test
    fun present_shouldCallShowNotification_withImage_whenPresent() = runTest {
        val testPushMessage = getTestJsPushMessage(image = IMAGE)
        val expectedNotificationOptions: NotificationOptions =
            getTestNotificationOptions(testPushMessage, emptyArray())

        pushMessagePresenter.present(testPushMessage)

        slot.get().title shouldBe testPushMessage.title
        JSON.stringify(slot.get().options) shouldBe JSON.stringify(expectedNotificationOptions)
    }

    @Test
    fun present_shouldCallShowNotification_withPushToInapp_whenPresent() = runTest {
        val pushToInApp = PushToInApp(
            "testCampaignId",
            "https://www.sap.com",
            false
        )
        val testPushMessage =
            getTestJsPushMessage(image = IMAGE, icon = ICON, pushToInApp = pushToInApp)
        val expectedNotificationOptions: NotificationOptions =
            getTestNotificationOptions(testPushMessage, emptyArray())

        pushMessagePresenter.present(testPushMessage)

        slot.get().title shouldBe testPushMessage.title
        JSON.stringify(slot.get().options) shouldBe JSON.stringify(expectedNotificationOptions)
    }

    @Test
    fun present_shouldCallShowNotification_withActionMappedCorrectly() = runTest {
        val testActions = listOf(
            PresentableOpenExternalUrlActionModel(
                id = "actionId1",
                title = "actionTitle1",
                url = "actionUrl1"
            ),
            PresentableOpenExternalUrlActionModel(
                id = "actionId2",
                title = "actionTitle2",
                url = "actionUrl2"
            )
        )
        val testPushMessage = getTestJsPushMessage(actions = testActions)
        val expectedNotificationOptions: NotificationOptions =
            getTestNotificationOptions(testPushMessage, arrayOf(
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

        slot.get().title shouldBe testPushMessage.title
        JSON.stringify(slot.get().options) shouldBe JSON.stringify(expectedNotificationOptions)
    }

    private fun getTestJsPushMessage(
        icon: String? = null,
        image: String? = null,
        actions: List<PresentableActionModel> = emptyList(),
        pushToInApp: PushToInApp? = null
    ): JsPushMessage {
        return JsPushMessage(
            messageId = "messageId",
            title = "title",
            body = "body",
            iconUrlString = icon,
            imageUrlString = image,
            data = PushData(
                sid = "sid",
                campaignId = "campaignId",
                platformData = JsPlatformData(
                    "testApplicationCode"
                ),
                actions = actions,
                pushToInApp = pushToInApp
            ),
        )
    }

    private fun getTestNotificationOptions(
        testPushMessage: JsPushMessage,
        notificationActions: Array<NotificationAction>
    ): NotificationOptions {
        val notificationOptions: NotificationOptions =
            js("{}").unsafeCast<NotificationOptions>().apply {
                body = testPushMessage.body
                icon = testPushMessage.iconUrlString
                badge = testPushMessage.imageUrlString
                actions = notificationActions
                data = JsonUtil.json.encodeToString<JsPushMessage>(testPushMessage)
            }
        return notificationOptions
    }
}