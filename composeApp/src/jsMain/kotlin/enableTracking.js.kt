import com.emarsys.Emarsys

actual suspend fun enableTracking() {
    Emarsys.enableTracking(JsEmarsysConfig("EMSE3-B4341"))
}