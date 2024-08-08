package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.mobileengage.push.model.JsNotificationAction
import com.emarsys.mobileengage.push.model.JsNotificationOptions
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushMessagePresenterTests {

    companion object {
        private const val ICON = "icon"
        private const val IMAGE = "image"
    }

    private lateinit var mockServiceWorkerRegistrationWrapper: ServiceWorkerRegistrationWrapper
    private lateinit var pushMessagePresenter: PushMessagePresenter

    @BeforeTest
    fun setup() = runTest {
        mockServiceWorkerRegistrationWrapper = mock<ServiceWorkerRegistrationWrapper> {
            everySuspend {
                showNotification(
                    any<String>(),
                    any<JsNotificationOptions>()
                )
            } returns Unit
        }

        pushMessagePresenter = PushMessagePresenter(mockServiceWorkerRegistrationWrapper)
    }

    @Test
    fun present_shouldCallShowNotification_withOnlyTitleAndBody_whenNothingElseIsPresent() =
        runTest {
            val testPushMessage = getTestJsPushMessage()

            pushMessagePresenter.present(testPushMessage)

            verifySuspend {
                mockServiceWorkerRegistrationWrapper.showNotification(
                    testPushMessage.title,
                    JsNotificationOptions(
                        testPushMessage.body,
                        null,
                        null,
                        emptyList()
                    )
                )
            }
        }

    @Test
    fun present_shouldCallShowNotification_withIcon_whenPresent() = runTest {
        val testPushMessage = getTestJsPushMessage(icon = ICON)

        pushMessagePresenter.present(testPushMessage)

        verifySuspend {
            mockServiceWorkerRegistrationWrapper.showNotification(
                testPushMessage.title,
                JsNotificationOptions(
                    testPushMessage.body,
                    ICON,
                    null,
                    emptyList()
                )
            )
        }
    }

    @Test
    fun present_shouldCallShowNotification_withImage_whenPresent() = runTest {
        val testPushMessage = getTestJsPushMessage(image = IMAGE)

        pushMessagePresenter.present(testPushMessage)

        verifySuspend {
            mockServiceWorkerRegistrationWrapper.showNotification(
                testPushMessage.title,
                JsNotificationOptions(
                    testPushMessage.body,
                    null,
                    IMAGE,
                    emptyList()
                )
            )
        }
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

        pushMessagePresenter.present(testPushMessage)

        verifySuspend {
            mockServiceWorkerRegistrationWrapper.showNotification(
                testPushMessage.title,
                JsNotificationOptions(
                    testPushMessage.body,
                    null,
                    null,
                    listOf(
                        JsNotificationAction("actionId1", "actionTitle1"),
                        JsNotificationAction("actionId2", "actionTitle2")
                    )
                )
            )
        }
    }

    private fun getTestJsPushMessage(
        icon: String? = null,
        image: String? = null,
        actions: List<PresentableActionModel> = emptyList()
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
                actions = actions
            )
        )
    }
}