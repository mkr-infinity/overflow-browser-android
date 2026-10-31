package com.overflow.browser.browser

import java.net.URLEncoder

enum class SearchEngine(
    val id: String,
    val displayName: String,
    val searchUrlTemplate: String,
    val suggestionsUrlTemplate: String?
) {
    DUCKDUCKGO(
        id = "duckduckgo",
        displayName = "DuckDuckGo",
        searchUrlTemplate = "https://duckduckgo.com/?q=%s",
        suggestionsUrlTemplate = "https://duckduckgo.com/ac/?q=%s&type=list"
    ),
    GOOGLE(
        id = "google",
        displayName = "Google",
        searchUrlTemplate = "https://www.google.com/search?q=%s",
        suggestionsUrlTemplate = "https://suggestqueries.google.com/complete/search?client=firefox&q=%s"
    ),
    BING(
        id = "bing",
        displayName = "Bing",
        searchUrlTemplate = "https://www.bing.com/search?q=%s",
        suggestionsUrlTemplate = "https://api.bing.com/osjson.aspx?query=%s"
    ),
    BRAVE(
        id = "brave",
        displayName = "Brave Search",
        searchUrlTemplate = "https://search.brave.com/search?q=%s",
        suggestionsUrlTemplate = "https://search.brave.com/api/suggest?q=%s&source=web"
    ),
    STARTPAGE(
        id = "startpage",
        displayName = "Startpage",
        searchUrlTemplate = "https://www.startpage.com/sp/search?query=%s",
        suggestionsUrlTemplate = null
    ),
    ECOSIA(
        id = "ecosia",
        displayName = "Ecosia",
        searchUrlTemplate = "https://www.ecosia.org/search?q=%s",
        suggestionsUrlTemplate = null
    ),
    CUSTOM(
        id = "custom",
        displayName = "Custom",
        searchUrlTemplate = "",
        suggestionsUrlTemplate = null
    );

    fun searchUrl(query: String, customTemplate: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val template = if (this == CUSTOM && customTemplate.isNotBlank()) {
            customTemplate
        } else {
            searchUrlTemplate
        }
        return when {
            template.contains("%s") -> template.replace("%s", encoded)
            template.contains("?") -> "$template$encoded"
            template.isBlank() -> DUCKDUCKGO.searchUrlTemplate.replace("%s", encoded)
            else -> "$template?q=$encoded"
        }
    }

    val letter: String
        get() = displayName.firstOrNull()?.uppercase() ?: "S"

    companion object {
        fun fromId(id: String): SearchEngine =
            entries.firstOrNull { it.id == id } ?: DUCKDUCKGO
    }
}
