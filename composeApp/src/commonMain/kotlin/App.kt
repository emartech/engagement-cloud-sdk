import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.emarsys.Emarsys
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
        }
    }
}