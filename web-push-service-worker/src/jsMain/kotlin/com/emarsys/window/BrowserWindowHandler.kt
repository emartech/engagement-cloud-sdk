package com.emarsys.window

import com.emarsys.self
import web.serviceworker.ClientQueryOptions
import web.serviceworker.ClientTypes
import web.serviceworker.WindowClient

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