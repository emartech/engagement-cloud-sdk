import com.sap.ec.api.config.JSConfigApi
import com.sap.ec.api.contact.JSContactApi
import com.sap.ec.api.deeplink.JSDeepLinkApi
import com.sap.ec.api.embeddedmessaging.JsEmbeddedMessagingApi
import com.sap.ec.api.event.model.EngagementCloudEvent
import com.sap.ec.api.events.EventEmitterApi
import com.sap.ec.api.events.JsApiEvent
import com.sap.ec.api.events.SdkEventSubscription
import com.sap.ec.api.events.SdkEventSubscriptionApi
import com.sap.ec.api.push.JSPushApi
import com.sap.ec.api.setup.JsSetupApi
import com.sap.ec.api.tracking.JSTrackingApi
import com.sap.ec.di.CoroutineScopeTypes
import com.sap.ec.di.EventFlowTypes
import com.sap.ec.di.SdkKoinIsolationContext
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.embeddedmessaging.ui.initializeCustomElements
import com.sap.ec.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import web.window.window

@OptIn(ExperimentalWasmJsInterop::class)
fun main() {
    val loaderUsed = (window["engagementCloudSdkLoaderUsed"] as? Boolean) ?: false
    if (!loaderUsed) {
        SdkKoinIsolationContext.init()
        JSEngagementCloud.init()
    }
}

typealias EngagementCloudSdkEventListener = (JsApiEvent) -> Unit

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("EngagementCloud")
object JSEngagementCloud {
    private val applicationScope by lazy { koin.get<CoroutineScope>(named(CoroutineScopeTypes.Application)) }
    private val sdkPublicEvents by lazy { koin.get<Flow<EngagementCloudEvent>>(named(EventFlowTypes.Public)) }
    val events: EventEmitterApi by lazy { koin.get<EventEmitterApi>() }
    val setup: JsSetupApi by lazy { koin.get<JsSetupApi>() }
    val config: JSConfigApi by lazy { koin.get<JSConfigApi>() }
    val contact: JSContactApi by lazy { koin.get<JSContactApi>() }
    val event: JSTrackingApi by lazy { koin.get<JSTrackingApi>() }
    val push: JSPushApi by lazy { koin.get<JSPushApi>() }
    val deepLink: JSDeepLinkApi by lazy { koin.get<JSDeepLinkApi>() }
    val embeddedMessaging: JsEmbeddedMessagingApi by lazy { koin.get<JsEmbeddedMessagingApi>() }

    suspend fun initializeSdk() {
        SdkKoinIsolationContext.initLaunch()
        init()
    }

    internal fun init() {
        initializeCustomElements()
    }

    fun registerEventListener(eventListener: EngagementCloudSdkEventListener): SdkEventSubscriptionApi {
        val job = applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkPublicEvents.collect {
                eventListener(
                    JSON.parse<JsApiEvent>(JsonUtil.json.encodeToString(it))
                )
            }
        }
        return SdkEventSubscription(job)
    }

}