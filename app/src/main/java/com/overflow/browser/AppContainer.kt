package com.overflow.browser

import android.content.Context
import com.overflow.browser.browser.AdBlocker
import com.overflow.browser.browser.DownloadHandler
import com.overflow.browser.browser.SearchSuggestionService
import com.overflow.browser.data.BookmarkRepository
import com.overflow.browser.data.DownloadRepository
import com.overflow.browser.data.HistoryRepository
import com.overflow.browser.data.SessionRepository
import com.overflow.browser.data.SettingsRepository
import com.overflow.browser.data.StatsRepository

/**
 * Small, explicit dependency container. Everything here is local to the device.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val settingsRepository = SettingsRepository(appContext)
    val bookmarkRepository = BookmarkRepository(appContext)
    val historyRepository = HistoryRepository(appContext)
    val downloadRepository = DownloadRepository(appContext)
    val sessionRepository = SessionRepository(appContext)
    val statsRepository = StatsRepository(appContext)
    val adBlocker = AdBlocker(appContext)
    val searchSuggestionService = SearchSuggestionService()
    val downloadHandler = DownloadHandler(appContext)
}
