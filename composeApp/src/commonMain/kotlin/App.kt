import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.emarsys.Emarsys
import com.emarsys.api.event.model.CustomEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val eventName = mutableStateOf("")
    MaterialTheme {
        Column {
            Text("Hello Team SDK!")
            Button(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    Emarsys.initialize()
                }
            }) {
                Text("Init SDK")
            }

            Button(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    enableTracking()
                    Emarsys.contact.linkContact(100027299, "test@test.com")
                }
            }) {
                Text("enable SDK & link contact")
            }

            TextField(value = eventName.value, onValueChange = {
                eventName.value = it
            })
            Button(onClick = {
                if (eventName.value.isNotBlank()) {
                    CoroutineScope(Dispatchers.Default).launch {
                        Emarsys.tracking.trackCustomEvent(CustomEvent(eventName.value, null))

                    }
                }
            }) {
                Text("trackCustomEvent")
            }
        }
    }
}