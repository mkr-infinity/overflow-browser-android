package com.overflow.browser.browser

import android.content.Context
import android.net.Uri
import android.webkit.WebView
import org.json.JSONObject
import java.util.Locale

/**
 * Host-based request blocking plus a small cosmetic filter. The blocker is
 * intentionally conservative: it blocks known ad/tracker hosts and their
 * subdomains, and never blocks first-party content based on URL substrings.
 */
class AdBlocker(context: Context) {

    private val hosts = HashSet<String>()

    init {
        runCatching {
            context.assets.open("adblock_hosts.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val value = line.substringBefore('#').trim().lowercase(Locale.ROOT)
                    if (value.isNotEmpty()) hosts.add(value)
                }
            }
        }
    }

    val hostCount: Int
        get() = hosts.size

    fun shouldBlock(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return false
        if (scheme != "http" && scheme != "https") return false
        val host = uri.host?.lowercase(Locale.ROOT) ?: return false
        return isBlockedHost(host)
    }

    fun isBlockedHost(host: String): Boolean {
        val normalized = host.lowercase(Locale.ROOT).removePrefix("www.")
        if (normalized in hosts) return true

        var current = normalized
        while (current.contains('.')) {
            current = current.substringAfter('.')
            if (current in hosts) return true
        }
        return false
    }

    fun injectCosmeticFilters(webView: WebView) {
        webView.evaluateJavascript(cosmeticScript, null)
    }

    private val cosmeticScript: String by lazy {
        """
        (function() {
          var id = 'overflow-cosmetic-style';
          var style = document.getElementById(id);
          if (!style) {
            style = document.createElement('style');
            style.id = id;
            (document.head || document.documentElement).appendChild(style);
          }
          style.textContent = ${JSONObject.quote(COSMETIC_CSS)};
        })();
        """.trimIndent()
    }

    private companion object {
        val COSMETIC_CSS = """
            [id^="google_ads"], [id^="div-gpt-ad"], .adsbygoogle, ins.adsbygoogle,
            [class*="advertisement"], [id*="advertisement"], [data-ad-slot],
            iframe[src*="doubleclick.net"], iframe[src*="googlesyndication.com"],
            [class*="sponsored"], [id*="sponsored"], [aria-label="Advertisement"] {
                display: none !important;
            }
        """.trimIndent()
    }
}
