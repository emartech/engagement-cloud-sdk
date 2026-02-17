package com.sap.ec.mobileengage.embeddedmessaging.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun Modifier.onScreenTime(
    minScreenTimeMs: Long,
    minFractionVisible: Float,
    onMinScreenTimeReached: () -> Unit
): Modifier {
    var isInBackground by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isInBackground = false
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        isInBackground = true
    }

    return if (isInBackground) {
        this
    } else {
        this.onVisibilityChanged(
            minScreenTimeMs,
            minFractionVisible
        ) { isVisible ->
            if (isVisible) {
                onMinScreenTimeReached()
            }
        }
    }
}