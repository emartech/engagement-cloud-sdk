package com.emarsys.mobileengage.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.embedded.ui.MessageItemView
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage


@Preview(showBackground = true)
@Composable
fun MessageItemViewPreview() {
    MaterialTheme {
        MessageItemView(providePreviewMessage())
    }
}

private fun providePreviewMessage() =
    EmbeddedMessage(
        "testId",
        "Sample Title",
        "This is a sample lead for the embedded message.",
        "https://example.com/image.png",
        BasicOpenExternalUrlActionModel(
            reporting = "Default Action",
            url = "https://example.com"
        ),
        emptyList(),
        listOf("promo", "new"),
        listOf(1, 2),
        System.currentTimeMillis(),
        System.currentTimeMillis() + 86400000,
        mapOf("key1" to "value1", "key2" to "value2"),
        "tracking_info_example"
    )
