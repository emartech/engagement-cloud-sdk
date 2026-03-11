package com.sap.ec.core.providers.inputmode

internal actual class InputModeProvider : InputModeProviderApi {
    actual override fun hasTouchSupport(): Boolean = true
}
