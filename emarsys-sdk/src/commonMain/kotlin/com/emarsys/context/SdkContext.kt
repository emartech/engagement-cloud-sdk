import com.emarsys.api.SdkState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SdkContext {
    
    private val innerSdkState: MutableStateFlow<SdkState> = MutableStateFlow(SdkState.inactive) 
    
    val sdkState: StateFlow<SdkState> = innerSdkState.asStateFlow()
    
    val sdkScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    fun setSdkState(sdkState: SdkState) {
        innerSdkState.value = sdkState
    }
    
}
