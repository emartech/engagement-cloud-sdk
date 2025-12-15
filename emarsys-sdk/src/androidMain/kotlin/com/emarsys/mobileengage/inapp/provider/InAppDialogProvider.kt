package com.emarsys.mobileengage.inapp.provider

import com.emarsys.mobileengage.inapp.view.InAppDialog

class InAppDialogProvider: InAppDialogProviderApi {
    override fun provide(): InAppDialog {
        return InAppDialog()
    }
}