package com.emarsys.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

fun CoroutineScope.waitUntilInactive() {
    while (!this.isActive) {
        //
    }
}