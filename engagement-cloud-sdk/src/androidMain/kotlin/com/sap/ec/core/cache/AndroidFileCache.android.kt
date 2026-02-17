package com.sap.ec.core.cache

import android.content.Context
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

class AndroidFileCache(
    private val context: Context,
    private val fileSystem: FileSystem
) : FileCacheApi {

    override fun get(fileName: String): ByteArray? {
        val cacheDir = getCacheDir()?.toPath()
        return if (cacheDir != null && fileSystem.exists(cacheDir.div(fileName))) {
            fileSystem.source(cacheDir / fileName).use {
                it.buffer().readByteArray()
            }
        } else {
            null
        }
    }

    override fun cache(fileName: String, file: ByteArray) {
        val cacheDir = getCacheDir()
        cacheDir?.let { dir ->
            val path = dir.toPath() / fileName
            fileSystem.sink(path).buffer().use {
                it.write(file)
            }
        }
    }

    override fun remove(fileName: String) {
        val cacheDir = getCacheDir()
        cacheDir?.let { dir ->
            val path = dir.toPath() / fileName
            if (fileSystem.exists(path)) {
                fileSystem.delete(path)
            }
        }
    }

    private fun getCacheDir(): String? {
        val cacheDir = context.cacheDir
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir.absolutePath
    }
}