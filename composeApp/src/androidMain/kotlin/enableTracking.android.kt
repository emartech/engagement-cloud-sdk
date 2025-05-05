import com.emarsys.AndroidEmarsys
import com.emarsys.AndroidEmarsysConfig
import com.emarsys.Emarsys

actual suspend fun enableTracking() {
    AndroidEmarsys.enableTracking(AndroidEmarsysConfig("EMSA4-90136"))
}