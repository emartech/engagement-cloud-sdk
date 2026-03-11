package com.sap.ec.mobileengage.embeddedmessaging.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.sap.ec.mobileengage.embeddedmessaging.ui.toReadTagId
import com.sap.ec.mobileengage.inapp.view.InlineInAppView
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Div
import web.dom.ElementId
import web.dom.document
import web.intersection.IntersectionObserver
import web.intersection.IntersectionObserverInit

@Composable
internal fun MessageDetailView(
    viewModel: MessageItemViewModelApi,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()

    viewModel.richContentUrl?.let {
        Div({
            id(viewModel.id.toReadTagId())
            classes(EmbeddedMessagingStyleSheet.detailViewContainer)
        }) {
            if (!viewModel.isRead) {
                DisposableEffect(Unit) {
                    val observer =
                        observeDetailedMessageVisibility(
                            viewModel,
                            scope
                        )
                    onDispose {
                        observer.disconnect()
                    }
                }
            }
            Div({
                classes(EmbeddedMessagingStyleSheet.detailContent)
            }) {
                InlineInAppView(
                    it,
                    viewModel.trackingInfo,
                    onClose = { onClose() }
                )
            }
        }
    }
}

private fun observeDetailedMessageVisibility(
    viewModel: MessageItemViewModelApi,
    scope: CoroutineScope
): IntersectionObserver {
    val reportReadAfterMillis = 3000L
    val target = document.getElementById(ElementId(viewModel.id.toReadTagId()))
    var applyReadTagJob: Job? = null

    val observer = IntersectionObserver(
        callback = { entries, _ ->
            if (entries[0].isIntersecting) {
                applyReadTagJob = scope.launch {
                    delay(reportReadAfterMillis)
                    viewModel.tagMessageRead()
                }
            } else {
                applyReadTagJob?.cancel()
                applyReadTagJob = null
            }
        },
        options = js("{}").unsafeCast<IntersectionObserverInit>().apply {
            threshold = arrayOf(0.5)
        }
    )

    if (target != null) {
        observer.observe(target)
    }

    return observer
}
