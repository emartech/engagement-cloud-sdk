package com.sap.ec.core.log

class LogConfigHolder: LogConfigHolderApi {
    override var remoteLogLevel: LogLevel = LogLevel.Error
    override var logBreadcrumbsQueueSize: Int = 10
}