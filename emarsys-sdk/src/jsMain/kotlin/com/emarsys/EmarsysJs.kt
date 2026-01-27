import com.emarsys.api.config.JSConfigApi
import com.emarsys.api.contact.JSContactApi
import com.emarsys.api.deeplink.JSDeepLinkApi
import com.emarsys.api.events.EventEmitterApi
import com.emarsys.api.events.SdkApiEvent
import com.emarsys.api.events.SdkEventSubscription
import com.emarsys.api.events.SdkEventSubscriptionApi
import com.emarsys.api.inapp.JSInAppApi
import com.emarsys.api.push.JSPushApi
import com.emarsys.api.setup.JsSetupApi
import com.emarsys.api.tracking.JSTrackingApi
import com.emarsys.core.log.Logger
import com.emarsys.di.CoroutineScopeTypes
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.event.SdkEvent
import com.emarsys.init.InitOrganizerApi
import com.emarsys.mobileengage.embeddedmessaging.ui.initializeCustomElements
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

fun main() {
    EmarsysJs.init()
}

typealias EmarsysSdkEventListener = (SdkApiEvent) -> Unit

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("Emarsys")
object EmarsysJs {
    init {
        SdkKoinIsolationContext.init()
    }
    private val applicationScope: CoroutineScope = koin.get<CoroutineScope>(named(CoroutineScopeTypes.Application))
    private val sdkPublicEvents = koin.get<Flow<SdkEvent.External.Api>>(named(EventFlowTypes.Public))
    val events = koin.get<EventEmitterApi>()
    val setup = koin.get<JsSetupApi>()
    val config = koin.get<JSConfigApi>()
    val contact = koin.get<JSContactApi>()
    val event = koin.get<JSTrackingApi>()
    val push = koin.get<JSPushApi>()
    val deepLink = koin.get<JSDeepLinkApi>()
    val inApp = koin.get<JSInAppApi>()

    internal fun init() {
        SdkKoinIsolationContext.init()

        val logger = koin.get<Logger>(parameters = { parametersOf(EmarsysJs::class.simpleName) })
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                koin.get<InitOrganizerApi>().init()
            } catch (error: Throwable) {
                logger.error(error.stackTraceToString())
            }
        }
        
        initializeCustomElements()
    }

    fun registerEventListener(eventListener: EmarsysSdkEventListener): SdkEventSubscriptionApi {
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