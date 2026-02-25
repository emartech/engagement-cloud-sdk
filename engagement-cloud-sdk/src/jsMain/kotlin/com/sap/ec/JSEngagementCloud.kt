import com.sap.ec.api.config.JSConfigApi
import com.sap.ec.api.contact.JSContactApi
import com.sap.ec.api.deeplink.JSDeepLinkApi
import com.sap.ec.api.embeddedmessaging.JsEmbeddedMessagingApi
import com.sap.ec.api.events.EventEmitterApi
import com.sap.ec.api.events.SdkApiEvent
import com.sap.ec.api.events.SdkEventSubscription
import com.sap.ec.api.events.SdkEventSubscriptionApi
import com.sap.ec.api.push.JSPushApi
import com.sap.ec.api.setup.JsSetupApi
import com.sap.ec.api.tracking.JSTrackingApi
import com.sap.ec.di.CoroutineScopeTypes
import com.sap.ec.di.EventFlowTypes
import com.sap.ec.di.SdkKoinIsolationContext
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.embeddedmessaging.ui.initializeCustomElements
import com.sap.ec.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named

fun main() {
    JSEngagementCloud.init()
}

typealias EngagementCloudSdkEventListener = (SdkApiEvent) -> Unit

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("EngagementCloud")
object JSEngagementCloud {
    init {
        SdkKoinIsolationContext.init()
    }

    private val applicationScope: CoroutineScope =
        koin.get<CoroutineScope>(named(CoroutineScopeTypes.Application))
    private val sdkPublicEvents =
        koin.get<Flow<SdkEvent.External.Api>>(named(EventFlowTypes.Public))
    val events = koin.get<EventEmitterApi>()
    val setup = koin.get<JsSetupApi>()
    val config = koin.get<JSConfigApi>()
    val contact = koin.get<JSContactApi>()
    val event = koin.get<JSTrackingApi>()
    val push = koin.get<JSPushApi>()
    val deepLink = koin.get<JSDeepLinkApi>()
    val embeddedMessaging = koin.get<JsEmbeddedMessagingApi>()

    internal fun init() {
        initializeCustomElements()
    }

    fun registerEventListener(eventListener: EngagementCloudSdkEventListener): SdkEventSubscriptionApi {
        val job = applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkPublicEvents.collect {
                eventListener(
                    JSON.parse<SdkApiEvent>(JsonUtil.json.encodeToString(it))
                )
            }
        }
        return SdkEventSubscription(job)
    }

}