import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import io.ktor.http.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SdkContext : SdkContextApi {

    private val innerSdkState: MutableStateFlow<SdkState> = MutableStateFlow(SdkState.inactive)

    override val sdkState: StateFlow<SdkState> = innerSdkState.asStateFlow()

    override val sdkDispatcher: CoroutineDispatcher = Dispatchers.Default
    override var config: EmarsysConfig? = null
    override fun setSdkState(sdkState: SdkState) {
        innerSdkState.value = sdkState
    }

    override fun createUrl(
        baseUrl: String,
        version: String,
        withAppCode: Boolean,
        path: String?
    ): Url {
        var url = "$baseUrl/$version"
        if (withAppCode) {
            require(config != null)
            require(config!!.applicationCode != null)

            url += "/apps/${config!!.applicationCode}"
        }
        if (path != null) {
            url += path
        }
        return URLBuilder(url).build()
    }

}

