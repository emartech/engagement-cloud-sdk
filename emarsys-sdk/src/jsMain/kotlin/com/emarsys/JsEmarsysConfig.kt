import com.emarsys.config.SdkConfig
import kotlinx.serialization.Serializable


/**
 * Configuration class for enabling the Emarsys SDK on the Web.
 *
 * @property applicationCode The application code of your application.
 * @property merchantId The merchant ID.
 * @property serviceWorkerOptions Options for configuring the service worker for receiving web push messages.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class JsEmarsysConfig(
    override val applicationCode: String? = null,
    override val merchantId: String? = null,
    override val sharedSecret: String? = null,
    val serviceWorkerOptions: ServiceWorkerOptions? = null
) : SdkConfig {
    override fun copyWith(
        applicationCode: String?,
        merchantId: String?,
        sharedSecret: String?
    ): SdkConfig {
        return copy(
            applicationCode = applicationCode,
            merchantId = merchantId,
            sharedSecret = sharedSecret
        )
    }
}

/**
 * Configuration class for the Web Push service worker.
 *
 * @property applicationServerKey The application code of your application.
 * @property serviceWorkerPath The path where the service worker code is located.
 * @property serviceWorkerScope The scope of the service worker, which defines the range of URLs it controls.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ServiceWorkerOptions(
    val applicationServerKey: String,
    val serviceWorkerPath: String,
    val serviceWorkerScope: String? = null,
)