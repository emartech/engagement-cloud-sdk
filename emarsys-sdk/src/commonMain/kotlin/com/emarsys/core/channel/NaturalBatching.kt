package com.emarsys.core.channel

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.produceIn

fun <T> Flow<T>.naturalBatching(maxChunkSize: Int = 10): Flow<List<T>> {
    require(maxChunkSize >= 1)
    return flow {
        coroutineScope {
            val upstreamChannel = this@naturalBatching
                .buffer(maxChunkSize)
                .produceIn(this)
            try {
                while (true) {
                    val result = upstreamChannel.awaitFirstAndDrain(maxChunkSize)
                    emit(result)
                }
            } catch (ignored: Exception) {
            }
        }
    }
}

private suspend fun <T> ReceiveChannel<T>.awaitFirstAndDrain(maxChunkSize: Int): List<T> {
    val bufferChunks = ArrayList<T>(maxChunkSize)
    val first = this.receiveCatching().getOrThrow()
    bufferChunks.add(first)
    return drain(bufferChunks, maxChunkSize)
}

private fun <T> ReceiveChannel<T>.drain(bufferChunks: MutableList<T>, maxChunkSize: Int): List<T> {
    while (bufferChunks.size < maxChunkSize) {
        val element = this.tryReceive().getOrNull() ?: break
        bufferChunks.add(element)
    }
    return bufferChunks
}