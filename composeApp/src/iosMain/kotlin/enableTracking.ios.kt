import com.emarsys.Emarsys
import com.emarsys.EmarsysConfig

actual suspend fun enableTracking() {
    Emarsys.enableTracking(EmarsysConfig("EMSE3-B434"))
}