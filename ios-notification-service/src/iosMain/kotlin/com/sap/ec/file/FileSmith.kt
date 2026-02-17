package com.sap.ec.iosNotificationService.file

import com.sap.ec.iosNotificationService.provider.Provider
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUUID

class FileSmith(private val uuidProvider: Provider<NSUUID>) {

    @OptIn(ExperimentalForeignApi::class)
    suspend fun tmpFileUrl(mimeType: String): NSURL? {
        return mimeType.split("/").lastOrNull()?.let { fileExtension ->
            return NSURL(fileURLWithPath = platform.Foundation.NSTemporaryDirectory()).URLByAppendingPathComponent(uuidProvider.provide().UUIDString)?.let {
                NSFileManager.defaultManager.createDirectoryAtURL(it, true, null, null)
                return it.URLByAppendingPathComponent("${uuidProvider.provide().UUIDString()}.${fileExtension}")
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    suspend fun move(fromUrl: NSURL, toUrl: NSURL) {
        NSFileManager.defaultManager.moveItemAtURL(fromUrl, toUrl, null)
    }

}
