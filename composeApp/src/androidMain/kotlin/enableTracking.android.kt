import com.emarsys.AndroidEmarsys
import com.emarsys.AndroidEmarsysConfig

actual suspend fun enableTracking() {
    AndroidEmarsys.enableTracking(AndroidEmarsysConfig("EMSA4-90136"))
}