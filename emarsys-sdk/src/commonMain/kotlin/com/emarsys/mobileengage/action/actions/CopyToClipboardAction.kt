package com.emarsys.mobileengage.action.actions

import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import com.emarsys.mobileengage.action.models.CopyToClipboardActionModel

class CopyToClipboardAction(
    private val action: CopyToClipboardActionModel,
    private val clipBoardHandler: ClipboardHandlerApi
) : Action<String> {
    override suspend fun invoke(value: String?) {
        clipBoardHandler.copyToClipboard(action.text)
    }
}