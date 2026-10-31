package com.overflow.browser.util

import com.overflow.browser.browser.SearchEngine
import java.net.URI

object UrlUtils {

    const val HOME_URL = "overflow://home"
    const val BLANK_URL = "about:blank"

    fun toUrl(
        input: String,
        engine: SearchEngine,
        customSearchTemplate: String,
        httpsOnly: Boolean
    ): String {
        val value = input.trim()
        if (value.isEmpty()) return HOME_URL
        if (value == HOME_URL) return HOME_URL

        when {
            value.startsWith("http://", ignoreCase = true) -> {
                return if (httpsOnly) "https://" + value.substring(7) else value
            }
            value.startsWith("https://", ignoreCase = true) -> return value
            value.startsWith("//") -> return "https:$value"
            value.startsWith("about:", ignoreCase = true) ||
                value.startsWith("data:", ignoreCase = true) ||
                value.startsWith("file:", ignoreCase = true) ||
                value.startsWith("content:", ignoreCase = true) ||
                value.startsWith("blob:", ignoreCase = true) ||
                value.startsWith("javascript:", ignoreCase = true) -> return value
            looksLikeUrl(value) -> return "https://$value"
            else -> return engine.searchUrl(value, customSearchTemplate)
        }
    }

    fun looksLikeUrl(input: String): Boolean {
        val value = input.trim()
        if (value.isEmpty() || value.any { it.isWhitespace() }) return false
        if (value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true) ||
            value.contains("://")
        ) return true
        if (value.startsWith("localhost", ignoreCase = true)) return true

        val host = value.substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
            .substringBefore(':')
        if (!host.contains('.')) return false
        if (host.count { it == '.' } == 3 &&
            host.split('.').all { it.isNotEmpty() && it.all(Char::isDigit) }
        ) {
            return true
        }
        val tld = host.substringAfterLast('.')
        return tld.length >= 2 && tld.all { it.isLetterOrDigit() }
    }

    fun isHttpUrl(url: String): Boolean =
        url.startsWith("http://", ignoreCase = true) ||
            url.startsWith("https://", ignoreCase = true)

    fun isSecure(url: String): Boolean = url.startsWith("https://", ignoreCase = true)

    fun host(url: String): String {
        if (url.isBlank() || url == HOME_URL) return "Overflow"
        val fromUri = runCatching { URI(url).host }.getOrNull()
        val value = fromUri ?: url.substringAfter("://").substringBefore('/')
        return value.removePrefix("www.").ifBlank { "Overflow" }
    }

    fun prettyUrl(url: String): String {
        if (url.isBlank() || url == HOME_URL) return "Start page"
        if (url == BLANK_URL) return "Blank page"
        return url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .trimEnd('/')
            .ifBlank { url }
    }

    fun faviconLetter(url: String): String =
        host(url).firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "O"

    fun isSpecialUrl(url: String): Boolean =
        url == HOME_URL ||
            url.startsWith("about:", ignoreCase = true) ||
            url.startsWith("data:", ignoreCase = true) ||
            url.startsWith("file:", ignoreCase = true) ||
            url.startsWith("content:", ignoreCase = true) ||
            url.startsWith("blob:", ignoreCase = true)
}
