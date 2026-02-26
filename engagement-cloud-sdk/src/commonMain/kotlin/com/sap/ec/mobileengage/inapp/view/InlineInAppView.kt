package com.sap.ec.mobileengage.inapp.view

import androidx.compose.runtime.Composable
import com.sap.ec.mobileengage.inapp.InAppMessage

@Composable
internal expect fun InlineInAppView(
    message: InAppMessage,
    onClose: () -> Unit,
    onLoaded: (() -> Unit)? = null
)
