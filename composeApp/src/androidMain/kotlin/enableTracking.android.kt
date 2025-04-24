import com.emarsys.AndroidEmarsysConfig
import com.emarsys.Emarsys

actual suspend fun enableTracking() {
    Emarsys.enableTracking(AndroidEmarsysConfig("EMSA4-90136"))
}