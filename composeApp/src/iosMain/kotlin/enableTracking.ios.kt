import com.emarsys.Emarsys
import com.emarsys.IosEmarsysConfig

actual suspend fun enableTracking() {
    Emarsys.enableTracking(IosEmarsysConfig("EMSE3-B4341"))
}