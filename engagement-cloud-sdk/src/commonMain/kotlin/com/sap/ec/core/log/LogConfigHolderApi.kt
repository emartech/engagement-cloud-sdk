package com.sap.ec.core.log

interface LogConfigHolderApi {
    var remoteLogLevel: LogLevel
    var logBreadcrumbsQueueSize: Int
}