package com.emarsys.networking.ktor.plugin

import com.emarsys.session.SessionContext

data class EmarsysAuthPluginConfig(
    var sessionContext: SessionContext? = null
)
