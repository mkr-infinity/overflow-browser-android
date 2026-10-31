package com.overflow.browser.browser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SearchSuggestionService {

    suspend fun suggestions(
        query: String,
        engine: SearchEngine,
        customTemplate: String
    ): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val template = engine.suggestionsUrlTemplate ?: return@withContext emptyList()
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = template.replace("%s", encoded)
        val connection = runCatching {
            URL(url).openConnection() as HttpURLConnection
        }.getOrNull() ?: return@withContext emptyList()

        try {
            connection.connectTimeout = 2500
            connection.readTimeout = 2500
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("Accept", "application/json,text/plain,*/*")
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
            )
            if (connection.responseCode !in 200..299) return@withContext emptyList()
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parse(body)
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(body: String): List<String> {
        val root = runCatching { JSONTokener(body).nextValue() }.getOrNull()
        if (root !is JSONArray || root.length() == 0) return emptyList()

        val suggestions = if (root.length() > 1 && root.opt(1) is JSONArray) {
            root.getJSONArray(1)
        } else {
            root
        }

        val result = LinkedHashSet<String>()
        for (index in 0 until suggestions.length()) {
            when (val item = suggestions.opt(index)) {
                is String -> result.add(item)
                is JSONObject -> {
                    val phrase = item.optString("phrase")
                    if (phrase.isNotBlank()) result.add(phrase)
                }
                is JSONArray -> {
                    for (nested in 0 until item.length()) {
                        val value = item.optString(nested)
                        if (value.isNotBlank()) result.add(value)
                    }
                }
            }
        }
        return result
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length <= 120 }
            .take(8)
    }
}
