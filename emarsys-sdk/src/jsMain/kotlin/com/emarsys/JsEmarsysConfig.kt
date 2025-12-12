import com.emarsys.ServiceWorkerOptions
import com.emarsys.config.SdkConfig
import kotlinx.serialization.Serializable

@Serializable
data class JsEmarsysConfig(
    override val applicationCode: String? = null,
    val serviceWorkerOptions: ServiceWorkerOptions? = null
) : SdkConfig {
    override fun copyWith(
        applicationCode: String?
    ): SdkConfig {
        return copy(
            applicationCode = applicationCode
        )
    }
}