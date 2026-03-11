package com.sap.ec.core.providers.inputmode

internal actual class InputModeProvider : InputModeProviderApi {
    actual override fun hasTouchSupport(): Boolean {
        return try {
            js("('ontouchstart' in window || navigator.maxTouchPoints > 0)") as Boolean
        } catch (_: Exception) {
            false
        }
    }
}
