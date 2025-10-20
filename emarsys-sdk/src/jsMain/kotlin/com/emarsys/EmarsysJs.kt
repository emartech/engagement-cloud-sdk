import com.emarsys.api.config.JSConfigApi
import com.emarsys.api.contact.JSContactApi
import com.emarsys.api.deeplink.JSDeepLinkApi
import com.emarsys.api.events.SdkApiEvent
import com.emarsys.api.events.SdkEventSubscription
import com.emarsys.api.events.SdkEventSubscriptionApi
import com.emarsys.api.inapp.JSInAppApi
import com.emarsys.api.push.JSPushApi
import com.emarsys.api.setup.JsSetupApi
import com.emarsys.api.tracking.JSTrackingApi
import com.emarsys.di.CoroutineScopeTypes
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.event.SdkEvent
import com.emarsys.init.InitOrganizerApi
import com.emarsys.util.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import org.koin.core.qualifier.named
import kotlin.js.Promise

fun main() {
    EmarsysJs.init()
}

typealias EmarsysSdkEventListener = (SdkApiEvent) -> Unit

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("Emarsys")
object EmarsysJs {
    private lateinit var applicationScope: CoroutineScope

    private lateinit var events: Flow<SdkEvent.External.Api>
    lateinit var setup: JsSetupApi
    lateinit var config: JSConfigApi
    lateinit var contact: JSContactApi
    lateinit var event: JSTrackingApi
    lateinit var push: JSPushApi
    lateinit var deepLink: JSDeepLinkApi
    lateinit var inApp: JSInAppApi

    internal fun init() {
        SdkKoinIsolationContext.init()
        applicationScope = koin.get<CoroutineScope>(named(CoroutineScopeTypes.Application))
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            koin.get<InitOrganizerApi>().init()
        }
        setup = koin.get<JsSetupApi>()
        config = koin.get<JSConfigApi>()
        contact = koin.get<JSContactApi>()
        event = koin.get<JSTrackingApi>()
        push = koin.get<JSPushApi>()
        deepLink = koin.get<JSDeepLinkApi>()
        inApp = koin.get<JSInAppApi>()
        events = koin.get<Flow<SdkEvent.External.Api>>(named(EventFlowTypes.Public))
    }

    /**
     * Enables tracking with the provided configuration.
     *
     * @param jsEmarsysConfig The SDK configuration to use for enabling tracking.
     * @return A promise that resolves when tracking is enabled.
     */
    fun enableTracking(jsEmarsysConfig: JsEmarsysConfig): Promise<Unit> {
        return applicationScope.promise {
            setup.enableTracking(jsEmarsysConfig)
        }
    }

    /**
     * Disables tracking.
     *
     */
    fun disableTracking(): Promise<Unit> {
        return applicationScope.promise {
            setup.disableTracking()
        }
    }

    fun registerEventListener(eventListener: EmarsysSdkEventListener): SdkEventSubscriptionApi {
        val job = applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            events.collect {
                eventListener(
                    JSON.parse<SdkApiEvent>(JsonUtil.json.encodeToString(it))
                )
            }
        }
        return SdkEventSubscription(job)
    }
}