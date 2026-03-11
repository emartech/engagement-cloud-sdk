package com.sap.ec.core.providers.inputmode

internal expect class InputModeProvider : InputModeProviderApi {
    override fun hasTouchSupport(): Boolean
}
