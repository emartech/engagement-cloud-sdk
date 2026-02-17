package com.sap.ec.sample

import androidx.compose.ui.Modifier

actual fun Modifier.safeAreaPadding(): Modifier {
    return this
}
