import com.sap.ec.ServiceWorkerOptions
import com.sap.ec.config.SdkConfig
import kotlinx.serialization.Serializable

@Serializable
data class JsEngagementCloudSDKConfig(
    override val applicationCode: String,
    val serviceWorkerOptions: ServiceWorkerOptions? = null,
) : SdkConfig {
    override fun copyWith(
        applicationCode: String
    ): SdkConfig {
        return copy(
            applicationCode = applicationCode
        )
    }
}