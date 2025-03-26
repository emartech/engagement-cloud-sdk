import com.emarsys.Emarsys
import com.emarsys.JsEmarsysConfig

actual suspend fun enableTracking() {
    Emarsys.enableTracking(JsEmarsysConfig("EMSE3-B434"))
}