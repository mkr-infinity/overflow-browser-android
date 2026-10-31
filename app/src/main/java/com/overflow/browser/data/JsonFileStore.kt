package com.overflow.browser.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Tiny JSON-backed list store. Browser data is small enough that a database
 * dependency would add more weight than it removes.
 */
class JsonFileStore<T>(
    private val file: File,
    private val encode: (List<T>) -> String,
    private val decode: (String) -> List<T>
) {
    private val mutex = Mutex()

    suspend fun read(): List<T> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!file.exists()) {
                emptyList()
            } else {
                runCatching { decode(file.readText()) }.getOrDefault(emptyList())
            }
        }
    }

    suspend fun write(items: List<T>) = withContext(Dispatchers.IO) {
        mutex.withLock {
            file.parentFile?.mkdirs()
            file.writeText(encode(items))
        }
    }
}
