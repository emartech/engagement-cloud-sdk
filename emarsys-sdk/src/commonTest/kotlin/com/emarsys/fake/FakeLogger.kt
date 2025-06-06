package com.emarsys.fake

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import kotlinx.serialization.json.JsonObject

class FakeLogger: Logger {
    override suspend fun info(
        logEntry: LogEntry,
        isRemoteLog: Boolean
    ) {
    }

    override suspend fun info(message: String, isRemoteLog: Boolean) {
    }

    override suspend fun info(
        message: String,
        throwable: Throwable,
        isRemoteLog: Boolean
    ) {
    }

    override suspend fun info(
        message: String,
        data: JsonObject,
        isRemoteLog: Boolean
    ) {
    }

    override suspend fun debug(
        logEntry: LogEntry,
        isRemoteLog: Boolean
    ) {
    }

    override suspend fun debug(message: String, isRemoteLog: Boolean) {
    }

    override suspend fun debug(
        message: String,
        data: JsonObject,
        isRemoteLog: Boolean
    ) {
    }

    override suspend fun debug(
        message: String,
        throwable: Throwable,
        isRemoteLog: Boolean
    ) {
    }

    override suspend fun error(
        logEntry: LogEntry,
        isRemoteLog: Boolean
    ) {
    }

    override suspend fun error(message: String, isRemoteLog: Boolean) {
    }

    override suspend fun error(
        message: String,
        data: JsonObject,
        isRemoteLog: Boolean
    ) {
    }

    override suspend fun error(
        message: String,
        throwable: Throwable,
        isRemoteLog: Boolean
    ) {
    }

    override suspend fun error(
        message: String,
        throwable: Throwable,
        data: JsonObject,
        isRemoteLog: Boolean
    ) {
    }

    override suspend fun metric(
        message: String,
        data: JsonObject
    ) {
    }
}