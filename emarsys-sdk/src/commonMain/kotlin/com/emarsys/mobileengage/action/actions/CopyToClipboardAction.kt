package com.emarsys.mobileengage.action.actions

import com.emarsys.mobileengage.action.models.CopyToClipboardActionModel

class CopyToClipboardAction(private val action: CopyToClipboardActionModel) :Action<String> {
    override suspend fun invoke(value: String?) {
        println("Copied value: ${action.text}. Should be implemented later.")
    }
}