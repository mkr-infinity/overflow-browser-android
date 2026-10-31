package com.overflow.browser.data

import android.content.Context
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

class BookmarkRepository(context: Context) {

    private val store = JsonFileStore(
        file = File(context.filesDir, "bookmarks.json"),
        encode = ::encode,
        decode = ::decode
    )

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val loadJob = scope.launch {
        _bookmarks.value = store.read()
    }

    suspend fun add(title: String, url: String): Bookmark {
        loadJob.join()
        val bookmark = Bookmark(
            title = title.ifBlank { url },
            url = url,
            createdAt = System.currentTimeMillis()
        )
        val updated = listOf(bookmark) + _bookmarks.value.filterNot { it.url == url }
        _bookmarks.value = updated
        store.write(updated)
        return bookmark
    }

    suspend fun update(bookmark: Bookmark) {
        loadJob.join()
        val updated = _bookmarks.value.map { if (it.id == bookmark.id) bookmark else it }
        _bookmarks.value = updated
        store.write(updated)
    }

    suspend fun remove(id: String) {
        loadJob.join()
        val updated = _bookmarks.value.filterNot { it.id == id }
        _bookmarks.value = updated
        store.write(updated)
    }

    suspend fun clear() {
        loadJob.join()
        _bookmarks.value = emptyList()
        store.write(emptyList())
    }

    private fun encode(items: List<Bookmark>): String {
        val array = JSONArray()
        items.forEach { bookmark ->
            array.put(
                JSONObject()
                    .put("id", bookmark.id)
                    .put("title", bookmark.title)
                    .put("url", bookmark.url)
                    .put("createdAt", bookmark.createdAt)
            )
        }
        return array.toString()
    }

    private fun decode(text: String): List<Bookmark> {
        val array = JSONArray(text)
        val result = ArrayList<Bookmark>(array.length())
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val url = item.optString("url")
            if (url.isBlank()) continue
            result.add(
                Bookmark(
                    id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                    title = item.optString("title").ifBlank { url },
                    url = url,
                    createdAt = item.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        return result
    }
}
