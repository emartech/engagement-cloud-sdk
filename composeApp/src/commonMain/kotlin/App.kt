import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.emarsys.Emarsys
import com.emarsys.EmarsysConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column {

            Text("Hello Team SDK!")
            Button(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    Emarsys.trackCustomEvent("mysy3", null)

                }
            }) {
                Text("trackCustomEvent")
            }
            Button(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    Emarsys.enableTracking(EmarsysConfig("EMS11-C3FD3"))
                    Emarsys.linkContact(2575, "test2@test.com")
                }
            }) {
                Text("enable SDK & link contact")
            }
            Button(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    Emarsys.initialize()
                }
            }) {
                Text("Init SDK")
            }
        }
    }
}