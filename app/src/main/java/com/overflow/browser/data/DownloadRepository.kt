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

class DownloadRepository(context: Context) {

    private val store = JsonFileStore(
        file = File(context.filesDir, "downloads.json"),
        encode = ::encode,
        decode = ::decode
    )

    private val _records = MutableStateFlow<List<DownloadRecord>>(emptyList())
    val records: StateFlow<List<DownloadRecord>> = _records.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val loadJob = scope.launch {
        _records.value = store.read()
    }

    suspend fun add(record: DownloadRecord) {
        loadJob.join()
        val updated = listOf(record) + _records.value.filterNot { it.id == record.id }
        _records.value = updated
        store.write(updated)
    }

    suspend fun remove(id: Long) {
        loadJob.join()
        val updated = _records.value.filterNot { it.id == id }
        _records.value = updated
        store.write(updated)
    }

    suspend fun clear() {
        loadJob.join()
        _records.value = emptyList()
        store.write(emptyList())
    }

    private fun encode(items: List<DownloadRecord>): String {
        val array = JSONArray()
        items.forEach { record ->
            array.put(
                JSONObject()
                    .put("id", record.id)
                    .put("fileName", record.fileName)
                    .put("url", record.url)
                    .put("mimeType", record.mimeType ?: "")
                    .put("createdAt", record.createdAt)
            )
        }
        return array.toString()
    }

    private fun decode(text: String): List<DownloadRecord> {
        val array = JSONArray(text)
        val result = ArrayList<DownloadRecord>(array.length())
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optLong("id", -1L)
            if (id <= 0L) continue
            result.add(
                DownloadRecord(
                    id = id,
                    fileName = item.optString("fileName").ifBlank { "Download" },
                    url = item.optString("url"),
                    mimeType = item.optString("mimeType").takeIf { it.isNotBlank() },
                    createdAt = item.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        return result
    }
}
