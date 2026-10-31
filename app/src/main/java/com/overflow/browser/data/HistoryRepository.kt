package com.overflow.browser.data

import android.content.Context
import com.overflow.browser.util.UrlUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class HistoryRepository(context: Context) {

    private val store = JsonFileStore(
        file = File(context.filesDir, "history.json"),
        encode = ::encode,
        decode = ::decode
    )

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val loadJob = scope.launch {
        _history.value = store.read()
    }

    suspend fun record(title: String, url: String) {
        if (!UrlUtils.isHttpUrl(url)) return
        loadJob.join()
        val entry = HistoryEntry(
            title = title.ifBlank { UrlUtils.host(url) },
            url = url,
            visitedAt = System.currentTimeMillis()
        )
        val updated = (listOf(entry) + _history.value.filterNot { it.url == url })
            .take(MAX_ENTRIES)
        _history.value = updated
        store.write(updated)
    }

    suspend fun remove(id: String) {
        loadJob.join()
        val updated = _history.value.filterNot { it.id == id }
        _history.value = updated
        store.write(updated)
    }

    suspend fun clear() {
        loadJob.join()
        _history.value = emptyList()
        store.write(emptyList())
    }

    private fun encode(items: List<HistoryEntry>): String {
        val array = JSONArray()
        items.forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("title", entry.title)
                    .put("url", entry.url)
                    .put("visitedAt", entry.visitedAt)
            )
        }
        return array.toString()
    }

    private fun decode(text: String): List<HistoryEntry> {
        val array = JSONArray(text)
        val result = ArrayList<HistoryEntry>(array.length())
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val url = item.optString("url")
            if (url.isBlank()) continue
            result.add(
                HistoryEntry(
                    id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                    title = item.optString("title").ifBlank { UrlUtils.host(url) },
                    url = url,
                    visitedAt = item.optLong("visitedAt", System.currentTimeMillis())
                )
            )
        }
        return result
    }

    private companion object {
        const val MAX_ENTRIES = 1000
    }
}
