import com.emarsys.AndroidEmarsysConfig
import com.emarsys.Emarsys

actual suspend fun enableTracking() {
    Emarsys.enableTracking(AndroidEmarsysConfig("EMSE3-B4341"))
}