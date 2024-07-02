package com.emarsys.core.cache

import com.emarsys.SdkConstants.CACHE_DIR_NAME
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosFileCache(private val fileManager: NSFileManager) : FileCacheApi {

    override fun get(fileName: String): ByteArray? {
        return getCacheDir()?.let { dir ->
            val path = "$dir/$fileName"
            if (exists(path)) {
                readFromFile(path)
            } else {
                null
            }
        }
    }

    override fun cache(fileName: String, file: ByteArray) {
        getCacheDir()?.let { dir ->
            val path = "$dir/$fileName"
            writeToFile(path, file)
        }
    }

    override fun remove(fileName: String) {
        getCacheDir()?.let { dir ->
            val path = "$dir/$fileName"
            fileManager.removeItemAtPath(path, null)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getCacheDir(): String? {
        return try {
            val cachesDir = NSSearchPathForDirectoriesInDomains(
                NSCachesDirectory,
                NSUserDomainMask,
                true
            ).first() as String

            val cacheDirPath = "$cachesDir/$CACHE_DIR_NAME"

            if (!exists(cacheDirPath)) {
                fileManager.createDirectoryAtPath(
                    cacheDirPath,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }
            cacheDirPath
        } catch (exception: Exception) {
            null
        }
    }

    private fun exists(path: String): Boolean {
        return fileManager.fileExistsAtPath(path)
    }

    private fun readFromFile(path: String): ByteArray? {
        val data = fileManager.contentsAtPath(path)
        return data?.bytes?.readBytes(data.length.toInt())
    }

    private fun writeToFile(path: String, bytes: ByteArray) {
        val nsData = bytes.toNSData()
        nsData?.writeToFile(path, true)
    }

    private fun ByteArray.toNSData(): NSData? = this.usePinned {
        try {
            NSData.create(bytes = it.addressOf(0), length = this.size.convert())
        } catch (exception: Exception) {
            null
        }
    }
}