package com.sap.ec.iosNotificationService.provider

import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration

class SessionProvider: Provider<NSURLSession> {

    private val session: NSURLSession by lazy {
        NSURLSession.sessionWithConfiguration(NSURLSessionConfiguration.defaultSessionConfiguration())
    }

    override fun provide(): NSURLSession {
        return session
    }

}
