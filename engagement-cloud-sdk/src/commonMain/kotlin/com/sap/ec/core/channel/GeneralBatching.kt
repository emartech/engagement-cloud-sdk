package com.sap.ec.core.channel


import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


fun <T> Flow<T>.batched(
    batchSize: Int,
    batchIntervalMillis: Long,
): Flow<List<T>> = channelFlow {
    val buffer = mutableListOf<T>()
    val mutex = Mutex()
    var job: Job? = null
    val flushBuffer: suspend () -> Unit = {
        mutex.withLock {
            if (buffer.isNotEmpty()) {
                send(buffer.toList())
                buffer.clear()
            }
        }
    }
    collect { item ->
        mutex.withLock {
            buffer.add(item)
        }
        if (job == null) {
            job = launch {
                delay(batchIntervalMillis)
                flushBuffer()
            }
        }

        if (buffer.size >= batchSize) {
            job?.cancelAndJoin()
            job = null
            flushBuffer()
        }
    }
}

