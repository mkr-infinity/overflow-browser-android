package com.overflow.browser.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class SessionTab(
    val id: String,
    val url: String,
    val title: String,
    val isDesktopMode: Boolean
)

data class SessionState(
    val tabs: List<SessionTab>,
    val activeIndex: Int
)

class SessionRepository(context: Context) {

    private val file = File(context.filesDir, "session.json")
    private val mutex = Mutex()

    suspend fun load(): SessionState? = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                if (!file.exists()) null else parse(file.readText())
            }.getOrNull()
        }
    }

    suspend fun save(state: SessionState) = withContext(Dispatchers.IO) {
        mutex.withLock {
            file.parentFile?.mkdirs()
            file.writeText(serialize(state))
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        mutex.withLock {
            file.delete()
            Unit
        }
    }

    private fun serialize(state: SessionState): String {
        val tabs = JSONArray()
        state.tabs.forEach { tab ->
            tabs.put(
                JSONObject()
                    .put("id", tab.id)
                    .put("url", tab.url)
                    .put("title", tab.title)
                    .put("desktop", tab.isDesktopMode)
            )
        }
        return JSONObject()
            .put("activeIndex", state.activeIndex)
            .put("tabs", tabs)
            .toString()
    }

    private fun parse(text: String): SessionState? {
        val root = JSONObject(text)
        val array = root.optJSONArray("tabs") ?: return null
        val tabs = ArrayList<SessionTab>(array.length())
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val url = item.optString("url")
            if (url.isBlank()) continue
            tabs.add(
                SessionTab(
                    id = item.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                    url = url,
                    title = item.optString("title"),
                    isDesktopMode = item.optBoolean("desktop", false)
                )
            )
        }
        if (tabs.isEmpty()) return null
        val activeIndex = root.optInt("activeIndex", 0).coerceIn(0, tabs.lastIndex)
        return SessionState(tabs, activeIndex)
    }
}
