package com.sap.ec.mobileengage.inapp.provider

import com.sap.ec.mobileengage.inapp.view.InAppDialog

class InAppDialogProvider: InAppDialogProviderApi {
    override fun provide(): InAppDialog {
        return InAppDialog()
    }
}