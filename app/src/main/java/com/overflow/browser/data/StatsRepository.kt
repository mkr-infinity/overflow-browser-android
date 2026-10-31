package com.overflow.browser.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Lightweight, session-persisted privacy counters. Values are flushed to disk
 * when the browser pauses instead of on every blocked request.
 */
class StatsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("overflow_stats", Context.MODE_PRIVATE)

    private val _adsBlocked = MutableStateFlow(prefs.getLong(KEY_ADS_BLOCKED, 0L))
    val adsBlocked: StateFlow<Long> = _adsBlocked.asStateFlow()

    private val _pagesLoaded = MutableStateFlow(prefs.getLong(KEY_PAGES_LOADED, 0L))
    val pagesLoaded: StateFlow<Long> = _pagesLoaded.asStateFlow()

    fun incrementAdsBlocked(count: Int = 1) {
        if (count > 0) _adsBlocked.update { it + count }
    }

    fun incrementPagesLoaded() {
        _pagesLoaded.update { it + 1 }
    }

    fun persist() {
        prefs.edit()
            .putLong(KEY_ADS_BLOCKED, _adsBlocked.value)
            .putLong(KEY_PAGES_LOADED, _pagesLoaded.value)
            .apply()
    }

    fun reset() {
        _adsBlocked.value = 0L
        _pagesLoaded.value = 0L
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ADS_BLOCKED = "ads_blocked"
        const val KEY_PAGES_LOADED = "pages_loaded"
    }
}
