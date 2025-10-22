import com.emarsys.Emarsys
import com.emarsys.IosEmarsys
import com.emarsys.IosEmarsysConfig

actual suspend fun enableTracking() {
    IosEmarsys.setup.enableTracking(IosEmarsysConfig("EMSE3-B4341"))
}