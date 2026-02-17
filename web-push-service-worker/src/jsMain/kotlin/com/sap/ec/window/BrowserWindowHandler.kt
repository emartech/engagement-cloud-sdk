package com.sap.ec.window

import com.sap.ec.self
import web.serviceworker.ClientQueryOptions
import web.serviceworker.ClientTypes
import web.serviceworker.WindowClient
import web.serviceworker.matchAll
import web.serviceworker.openWindow
import web.serviceworker.window

class BrowserWindowHandler : BrowserWindowHandlerApi {

    override suspend fun findOpenWindow(): WindowClient? {
        val openWindows = self.clients.matchAll(
            options =
                ClientQueryOptions(
                    type = ClientTypes.window,
                    includeUncontrolled = true
                )
        )

        val openWindow = openWindows.find { windowClient ->
            windowClient.url.startsWith(self.location.origin)
        }
        return openWindow?.unsafeCast<WindowClient>()
    }

    override suspend fun openWindow(url: String): WindowClient? {
        return self.clients.openWindow(self.location.origin)
    }

}