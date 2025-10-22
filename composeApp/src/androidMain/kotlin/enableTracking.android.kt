import com.emarsys.AndroidEmarsys
import com.emarsys.AndroidEmarsysConfig

actual suspend fun enableTracking() {
    AndroidEmarsys.setup.enableTracking(AndroidEmarsysConfig("EMSA4-90136"))
}