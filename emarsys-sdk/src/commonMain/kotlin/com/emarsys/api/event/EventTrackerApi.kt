import com.emarsys.api.AutoRegisterable
import com.emarsys.api.event.model.CustomEvent

interface EventTrackerApi: AutoRegisterable {
    suspend fun trackEvent(event: CustomEvent): Result<Unit>
}