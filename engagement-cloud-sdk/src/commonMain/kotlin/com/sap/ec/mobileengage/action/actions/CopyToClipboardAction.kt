package com.sap.ec.mobileengage.action.actions

import com.sap.ec.core.actions.clipboard.ClipboardHandlerApi
import com.sap.ec.mobileengage.action.models.CopyToClipboardActionModel

class CopyToClipboardAction(
    private val action: CopyToClipboardActionModel,
    private val clipBoardHandler: ClipboardHandlerApi
) : Action<String> {
    override suspend fun invoke(value: String?) {
        clipBoardHandler.copyToClipboard(action.text)
    }
}