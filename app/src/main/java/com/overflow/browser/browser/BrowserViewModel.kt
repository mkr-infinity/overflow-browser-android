package com.overflow.browser.browser

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewDatabase
import android.widget.FrameLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.webkit.WebSettingsCompat
import com.overflow.browser.AppContainer
import com.overflow.browser.data.Bookmark
import com.overflow.browser.data.BrowserSettings
import com.overflow.browser.data.DownloadRecord
import com.overflow.browser.data.SessionState
import com.overflow.browser.data.SessionTab
import com.overflow.browser.data.ToolbarPosition
import com.overflow.browser.ui.theme.ThemeMode
import com.overflow.browser.util.UrlUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserViewModel(
    application: Application,
    private val container: AppContainer
) : AndroidViewModel(application) {

    private val appContext: Context
        get() = getApplication<Application>().applicationContext

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    val bookmarks = container.bookmarkRepository.bookmarks
    val history = container.historyRepository.history
    val downloads = container.downloadRepository.records
    val adsBlocked = container.statsRepository.adsBlocked
    val pagesLoaded = container.statsRepository.pagesLoaded

    private val webViews = mutableMapOf<String, WebView>()
    private val webViewLastUsed = mutableMapOf<String, Long>()
    private var containerRef: FrameLayout? = null
    private var attachedTabId: String? = null
    private var initialized = false
    private var sessionSaveJob: Job? = null
    private var suggestionJob: Job? = null
    private var pendingIntentUrl: String? = null
    private var findListener: WebView.FindListener? = null

    var chromeDelegate: ChromeDelegate? = null

    init {
        viewModelScope.launch {
            container.settingsRepository.settings.collect { newSettings ->
                val oldSettings = _uiState.value.settings
                _uiState.update { it.copy(settings = newSettings) }
                if (!initialized) {
                    initialized = true
                    restoreSession(newSettings)
                } else {
                    applySettings(oldSettings, newSettings)
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // Lifecycle and WebView plumbing
    // ---------------------------------------------------------------------

    private suspend fun restoreSession(settings: BrowserSettings) {
        val session = if (settings.restoreTabs) {
            runCatching { container.sessionRepository.load() }.getOrNull()
        } else {
            null
        }
        val restoredTabs = session?.tabs
            ?.map {
                BrowserTab(
                    id = it.id,
                    url = it.url,
                    title = it.title,
                    isDesktopMode = it.isDesktopMode
                )
            }
            .orEmpty()
        val tabs = restoredTabs.ifEmpty { listOf(BrowserTab()) }
        val activeIndex = (session?.activeIndex ?: 0).coerceIn(0, tabs.lastIndex)
        _uiState.update {
            it.copy(tabs = tabs, activeTabId = tabs[activeIndex].id, isReady = true)
        }

        val active = tabs[activeIndex]
        if (!active.isHome) {
            ensureWebView(active, initialUrl = active.url)
            attachActiveWebView(containerRef)
        }
        if (settings.safeBrowsing) startSafeBrowsing()

        pendingIntentUrl?.let { url ->
            pendingIntentUrl = null
            openUrlFromIntent(url)
        }
    }

    private fun applySettings(old: BrowserSettings, new: BrowserSettings) {
        if (old.javascriptEnabled != new.javascriptEnabled ||
            old.imagesEnabled != new.imagesEnabled ||
            old.cookiesEnabled != new.cookiesEnabled ||
            old.darkWebPages != new.darkWebPages ||
            old.safeBrowsing != new.safeBrowsing
        ) {
            webViews.values.forEach(::applyWebSettings)
            if (old.darkWebPages != new.darkWebPages) activeWebView()?.reload()
        }
        if (!old.safeBrowsing && new.safeBrowsing) startSafeBrowsing()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun applyWebSettings(webView: WebView) {
        val settings = _uiState.value.settings
        val tab = _uiState.value.tabs.firstOrNull { it.id == webView.tag }
        webView.settings.apply {
            javaScriptEnabled = settings.javascriptEnabled
            loadsImagesAutomatically = settings.imagesEnabled
            blockNetworkImage = !settings.imagesEnabled
            userAgentString = if (tab?.isDesktopMode == true) {
                DESKTOP_USER_AGENT
            } else {
                WebSettings.getDefaultUserAgent(appContext)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = settings.safeBrowsing
            }
        }
        CookieManager.getInstance().setAcceptCookie(settings.cookiesEnabled)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, settings.cookiesEnabled)
        runCatching {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(
                webView.settings,
                settings.darkWebPages
            )
        }
    }

    private fun startSafeBrowsing() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { WebView.startSafeBrowsing(appContext) { _ -> } }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(tab: BrowserTab): WebView {
        val webView = WebView(appContext)
        webView.tag = tab.id
        webView.settings.apply {
            javaScriptEnabled = _uiState.value.settings.javascriptEnabled
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = _uiState.value.settings.imagesEnabled
            blockNetworkImage = !_uiState.value.settings.imagesEnabled
            mediaPlaybackRequiresUserGesture = true
            allowFileAccess = false
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            textZoom = 100
            setSupportZoom(true)
        }
        applyWebSettings(webView)
        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        webView.webViewClient = OverflowWebViewClient(
            tabId = tab.id,
            settingsProvider = { _uiState.value.settings },
            adBlocker = container.adBlocker,
            onPageStarted = ::handlePageStarted,
            onPageFinished = ::handlePageFinished,
            onUrlChanged = ::handleUrlChanged,
            onError = ::handleError,
            onBlocked = { count -> container.statsRepository.incrementAdsBlocked(count) },
            onExternalUrl = ::openExternalUri,
            onRendererGone = ::handleRendererGone
        )
        webView.webChromeClient = OverflowChromeClient(
            tabId = tab.id,
            onProgress = ::handleProgress,
            onTitle = ::handleTitle,
            onFavicon = ::handleFavicon,
            onJsDialog = ::showJsDialog,
            onFileChooser = { target, callback, params ->
                chromeDelegate?.onShowFileChooser(target, callback, params) ?: false
            },
            onPermissionRequest = { request ->
                chromeDelegate?.onPermissionRequest(request) ?: request.deny()
            },
            onShowCustomView = { view, callback ->
                val delegate = chromeDelegate
                if (delegate != null) {
                    delegate.onShowCustomView(view, callback)
                } else {
                    callback.onCustomViewHidden()
                }
            },
            onHideCustomView = { chromeDelegate?.onHideCustomView() }
        )
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val downloadId = container.downloadHandler.enqueue(
                url = url,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType
            )
            if (downloadId != null) {
                viewModelScope.launch {
                    container.downloadRepository.add(
                        DownloadRecord(
                            id = downloadId,
                            fileName = URLUtil.guessFileName(url, contentDisposition, mimeType),
                            url = url,
                            mimeType = mimeType,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
                showSnackbar("Download started")
            } else {
                showSnackbar("Download could not be started")
            }
        }
        webView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (_uiState.value.settings.hideToolbarOnScroll) {
                val delta = scrollY - oldScrollY
                if (delta > 14 && _uiState.value.toolbarVisible) {
                    _uiState.update { it.copy(toolbarVisible = false) }
                } else if (delta < -14 && !_uiState.value.toolbarVisible) {
                    _uiState.update { it.copy(toolbarVisible = true) }
                }
            }
        }
        return webView
    }

    private fun ensureWebView(tab: BrowserTab, initialUrl: String? = tab.url): WebView {
        webViews[tab.id]?.let {
            webViewLastUsed[tab.id] = System.currentTimeMillis()
            return it
        }
        val webView = createWebView(tab)
        webViews[tab.id] = webView
        webViewLastUsed[tab.id] = System.currentTimeMillis()
        if (!initialUrl.isNullOrBlank() &&
            initialUrl != UrlUtils.HOME_URL &&
            initialUrl != UrlUtils.BLANK_URL
        ) {
            webView.loadUrl(initialUrl)
        }
        trimWebViews(tab.id)
        return webView
    }

    private fun trimWebViews(activeId: String) {
        val maxLiveWebViews = 6
        while (webViews.size > maxLiveWebViews) {
            val candidate = webViewLastUsed.entries
                .filter { it.key != activeId }
                .minByOrNull { it.value }
                ?.key
                ?: break
            webViews.remove(candidate)?.let { webView ->
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.stopLoading()
                webView.destroy()
            }
            webViewLastUsed.remove(candidate)
        }
    }

    fun attachContainer(container: FrameLayout) {
        containerRef = container
        attachActiveWebView(container)
    }

    fun detachContainer(container: FrameLayout) {
        if (containerRef === container) {
            container.removeAllViews()
            containerRef = null
            attachedTabId = null
        }
    }

    fun attachActiveWebView(container: FrameLayout? = containerRef) {
        val target = container ?: return
        val state = _uiState.value
        val tab = state.tabs.firstOrNull { it.id == state.activeTabId }
        if (tab == null || tab.isHome) {
            target.removeAllViews()
            attachedTabId = null
            return
        }
        val webView = webViews[tab.id] ?: ensureWebView(tab, initialUrl = tab.url)
        if (webView.parent !== target) {
            (webView.parent as? ViewGroup)?.removeView(webView)
            target.removeAllViews()
            target.addView(
                webView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
        if (attachedTabId != tab.id) {
            webView.onResume()
            attachedTabId = tab.id
        }
    }

    // ---------------------------------------------------------------------
    // Tabs and navigation
    // ---------------------------------------------------------------------

    fun newTab(url: String? = null, incognito: Boolean = false, select: Boolean = true) {
        val settings = _uiState.value.settings
        val tab = BrowserTab(
            title = "New tab",
            isIncognito = incognito,
            isDesktopMode = settings.desktopModeDefault
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + tab,
                activeTabId = if (select) tab.id else state.activeTabId,
                overlay = if (select) BrowserOverlay.NONE else state.overlay,
                isMenuOpen = false,
                toolbarVisible = true
            )
        }
        if (select) {
            if (url != null) {
                loadUrlInTab(tab.id, url)
            } else {
                attachActiveWebView(containerRef)
            }
        } else if (url != null) {
            val normalized = UrlUtils.toUrl(
                url,
                settings.searchEngine,
                settings.customSearchUrl,
                settings.httpsOnly
            )
            _uiState.update { state ->
                state.copy(
                    tabs = state.tabs.map {
                        if (it.id == tab.id) {
                            it.copy(url = normalized, title = UrlUtils.prettyUrl(normalized))
                        } else {
                            it
                        }
                    }
                )
            }
            ensureWebView(
                _uiState.value.tabs.first { it.id == tab.id },
                initialUrl = normalized
            )
        }
        scheduleSessionSave()
    }

    fun newPrivateTab() = newTab(incognito = true)

    fun loadUrlInTab(tabId: String, input: String) {
        val settings = _uiState.value.settings
        val url = UrlUtils.toUrl(
            input,
            settings.searchEngine,
            settings.customSearchUrl,
            settings.httpsOnly
        )
        if (url == UrlUtils.HOME_URL) {
            goHomeInTab(tabId)
            return
        }
        if (_uiState.value.tabs.none { it.id == tabId }) return

        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tabId) {
                        it.copy(
                            url = url,
                            title = UrlUtils.prettyUrl(url),
                            isLoading = true,
                            progress = 0,
                            error = null
                        )
                    } else {
                        it
                    }
                },
                activeTabId = tabId,
                overlay = BrowserOverlay.NONE,
                isMenuOpen = false,
                isSearchOpen = false,
                searchQuery = "",
                suggestions = emptyList(),
                toolbarVisible = true
            )
        }
        val updated = _uiState.value.tabs.first { it.id == tabId }
        val webView = ensureWebView(updated, initialUrl = null)
        webView.loadUrl(url)
        attachActiveWebView(containerRef)
        scheduleSessionSave()
    }

    fun openUrlFromIntent(input: String) {
        if (!_uiState.value.isReady) {
            pendingIntentUrl = input
            return
        }
        val active = _uiState.value.activeTab
        if (active == null) {
            newTab(input)
        } else {
            loadUrlInTab(active.id, input)
        }
    }

    fun selectTab(tabId: String) {
        val state = _uiState.value
        val tab = state.tabs.firstOrNull { it.id == tabId } ?: return
        if (state.activeTabId == tabId) {
            _uiState.update { it.copy(overlay = BrowserOverlay.NONE, isMenuOpen = false) }
            return
        }
        state.activeTabId?.let { webViews[it]?.onPause() }
        _uiState.update {
            it.copy(
                activeTabId = tabId,
                overlay = BrowserOverlay.NONE,
                isMenuOpen = false,
                toolbarVisible = true
            )
        }
        if (!tab.isHome) {
            ensureWebView(tab, initialUrl = tab.url)
            attachActiveWebView(containerRef)
            webViews[tabId]?.onResume()
        } else {
            attachActiveWebView(containerRef)
        }
        scheduleSessionSave()
    }

    fun closeTab(tabId: String) {
        val state = _uiState.value
        val index = state.tabs.indexOfFirst { it.id == tabId }
        if (index < 0) return
        val wasActive = state.activeTabId == tabId
        webViews.remove(tabId)?.let { webView ->
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.stopLoading()
            webView.destroy()
        }
        webViewLastUsed.remove(tabId)

        val remaining = state.tabs.toMutableList().apply { removeAt(index) }
        if (remaining.isEmpty()) {
            val newTab = BrowserTab()
            _uiState.update {
                it.copy(
                    tabs = listOf(newTab),
                    activeTabId = newTab.id,
                    overlay = BrowserOverlay.NONE
                )
            }
            attachActiveWebView(containerRef)
        } else {
            val newIndex = if (wasActive) {
                (index - 1).coerceAtLeast(0)
            } else {
                remaining.indexOfFirst { it.id == state.activeTabId }.coerceAtLeast(0)
            }
            val newActiveId = remaining[newIndex].id
            _uiState.update { it.copy(tabs = remaining, activeTabId = newActiveId) }
            if (wasActive) {
                val newActive = remaining[newIndex]
                if (!newActive.isHome) ensureWebView(newActive, initialUrl = newActive.url)
                attachActiveWebView(containerRef)
            }
        }
        scheduleSessionSave()
    }

    fun closeAllTabs(incognitoOnly: Boolean = false) {
        val state = _uiState.value
        val toClose = state.tabs.filter { !incognitoOnly || it.isIncognito }
        if (toClose.isEmpty()) return
        val closeIds = toClose.map { it.id }.toSet()

        toClose.forEach { tab ->
            webViews.remove(tab.id)?.let { webView ->
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.stopLoading()
                webView.destroy()
            }
            webViewLastUsed.remove(tab.id)
        }

        val remaining = state.tabs.filterNot { it.id in closeIds }
        if (remaining.isEmpty()) {
            val newTab = BrowserTab()
            _uiState.update {
                it.copy(
                    tabs = listOf(newTab),
                    activeTabId = newTab.id,
                    overlay = BrowserOverlay.NONE
                )
            }
            attachActiveWebView(containerRef)
        } else {
            val activeId = remaining.firstOrNull { it.id == state.activeTabId }?.id
                ?: remaining.first().id
            _uiState.update { it.copy(tabs = remaining, activeTabId = activeId) }
            val active = remaining.first { it.id == activeId }
            if (!active.isHome) ensureWebView(active, initialUrl = active.url)
            attachActiveWebView(containerRef)
        }
        scheduleSessionSave()
    }

    fun goHome() {
        _uiState.value.activeTab?.let { goHomeInTab(it.id) }
    }

    private fun goHomeInTab(tabId: String) {
        val homepage = _uiState.value.settings.homepage
        if (homepage != UrlUtils.HOME_URL) {
            loadUrlInTab(tabId, homepage)
            return
        }
        webViews[tabId]?.let { webView ->
            webView.stopLoading()
            webView.loadUrl(UrlUtils.BLANK_URL)
        }
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tabId) {
                        it.copy(
                            url = UrlUtils.HOME_URL,
                            title = "New tab",
                            isLoading = false,
                            progress = 0,
                            canGoBack = false,
                            canGoForward = false,
                            error = null,
                            favicon = null
                        )
                    } else {
                        it
                    }
                },
                isMenuOpen = false,
                overlay = BrowserOverlay.NONE,
                toolbarVisible = true
            )
        }
        attachActiveWebView(containerRef)
        scheduleSessionSave()
    }

    fun goBack() {
        activeWebView()?.let { if (it.canGoBack()) it.goBack() }
    }

    fun goForward() {
        activeWebView()?.let { if (it.canGoForward()) it.goForward() }
    }

    fun reload() {
        val tab = _uiState.value.activeTab ?: return
        if (tab.isHome) {
            goHome()
            return
        }
        val webView = webViews[tab.id] ?: ensureWebView(tab, initialUrl = tab.url)
        webView.reload()
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tab.id) it.copy(error = null, isLoading = true) else it
                }
            )
        }
    }

    fun stopLoading() {
        activeWebView()?.stopLoading()
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == state.activeTabId) it.copy(isLoading = false) else it
                }
            )
        }
    }

    fun toggleDesktopMode() {
        val tab = _uiState.value.activeTab ?: return
        val enabled = !tab.isDesktopMode
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tab.id) it.copy(isDesktopMode = enabled) else it
                }
            )
        }
        webViews[tab.id]?.let { webView ->
            applyWebSettings(webView)
            webView.reload()
        }
        showSnackbar(if (enabled) "Desktop site enabled" else "Mobile site enabled")
        scheduleSessionSave()
    }

    // ---------------------------------------------------------------------
    // Search, find, reader, sharing
    // ---------------------------------------------------------------------

    fun openSearch() {
        val tab = _uiState.value.activeTab
        _uiState.update {
            it.copy(
                isSearchOpen = true,
                searchQuery = if (tab?.isHome == true) "" else tab?.url.orEmpty(),
                suggestions = emptyList(),
                isMenuOpen = false
            )
        }
    }

    fun closeSearch() {
        suggestionJob?.cancel()
        _uiState.update {
            it.copy(isSearchOpen = false, searchQuery = "", suggestions = emptyList())
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        requestSuggestions(query)
    }

    fun submitSearch(query: String = _uiState.value.searchQuery) {
        val value = query.trim()
        if (value.isEmpty()) return
        val tabId = _uiState.value.activeTabId
        if (tabId == null) {
            newTab(value)
        } else {
            loadUrlInTab(tabId, value)
        }
        closeSearch()
    }

    private fun requestSuggestions(query: String) {
        suggestionJob?.cancel()
        if (!_uiState.value.settings.searchSuggestions ||
            query.isBlank() ||
            UrlUtils.looksLikeUrl(query)
        ) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }
        suggestionJob = viewModelScope.launch {
            delay(250)
            val settings = _uiState.value.settings
            val result = container.searchSuggestionService.suggestions(
                query,
                settings.searchEngine,
                settings.customSearchUrl
            )
            if (_uiState.value.isSearchOpen) {
                _uiState.update { it.copy(suggestions = result) }
            }
        }
    }

    fun openFindInPage() {
        val webView = activeWebView() ?: return
        findListener?.let { webView.setFindListener(null) }
        val listener = WebView.FindListener { activeMatchOrdinal, numberOfMatches, _ ->
            _uiState.update {
                it.copy(findCurrent = activeMatchOrdinal, findMatches = numberOfMatches)
            }
        }
        findListener = listener
        webView.setFindListener(listener)
        _uiState.update {
            it.copy(
                isFindOpen = true,
                findQuery = "",
                findMatches = 0,
                findCurrent = 0,
                isMenuOpen = false
            )
        }
    }

    fun updateFindQuery(query: String) {
        _uiState.update { it.copy(findQuery = query) }
        val webView = activeWebView() ?: return
        if (query.isBlank()) {
            webView.clearMatches()
            _uiState.update { it.copy(findMatches = 0, findCurrent = 0) }
        } else {
            webView.findAllAsync(query)
        }
    }

    fun findNext(forward: Boolean) {
        activeWebView()?.findNext(forward)
    }

    fun closeFindInPage() {
        activeWebView()?.let {
            it.clearMatches()
            it.setFindListener(null)
        }
        findListener = null
        _uiState.update {
            it.copy(isFindOpen = false, findQuery = "", findMatches = 0, findCurrent = 0)
        }
    }

    fun openReader() {
        val webView = activeWebView() ?: return
        webView.evaluateJavascript(ReaderMode.extractionScript) { raw ->
            val content = ReaderMode.parse(raw)
            if (content == null) {
                showSnackbar("Reader mode could not find an article here")
            } else {
                _uiState.update { it.copy(readerContent = content, isMenuOpen = false) }
            }
        }
    }

    fun closeReader() {
        _uiState.update { it.copy(readerContent = null) }
    }

    fun setReaderFontScale(scale: Float) {
        updateSettings {
            container.settingsRepository.setReaderFontScale(scale.coerceIn(0.8f, 1.6f))
        }
    }

    fun toggleBookmark() {
        val tab = _uiState.value.activeTab ?: return
        if (tab.isHome) return
        viewModelScope.launch {
            val existing = bookmarks.value.firstOrNull { it.url == tab.url }
            if (existing != null) {
                container.bookmarkRepository.remove(existing.id)
                showSnackbar("Bookmark removed")
            } else {
                container.bookmarkRepository.add(tab.displayTitle, tab.url)
                showSnackbar("Bookmark added")
            }
        }
    }

    fun addBookmark(title: String, url: String) {
        viewModelScope.launch {
            container.bookmarkRepository.add(title, url)
            showSnackbar("Bookmark added")
        }
    }

    fun updateBookmark(bookmark: Bookmark, title: String, url: String) {
        viewModelScope.launch {
            container.bookmarkRepository.update(
                bookmark.copy(title = title.ifBlank { url }, url = url)
            )
            showSnackbar("Bookmark updated")
        }
    }

    fun removeBookmark(id: String) {
        viewModelScope.launch {
            container.bookmarkRepository.remove(id)
            showSnackbar("Bookmark removed")
        }
    }

    fun shareCurrentPage() {
        val tab = _uiState.value.activeTab ?: return
        if (tab.isHome) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, tab.displayTitle)
            putExtra(Intent.EXTRA_TEXT, "${tab.displayTitle}\n${tab.url}")
        }
        val chooser = Intent.createChooser(intent, "Share page")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { appContext.startActivity(chooser) }
    }

    fun copyCurrentUrl() {
        val tab = _uiState.value.activeTab ?: return
        if (tab.isHome) return
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText("URL", tab.url))
        showSnackbar("Link copied")
    }

    fun openInExternalBrowser() {
        val tab = _uiState.value.activeTab ?: return
        if (tab.isHome) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tab.url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { appContext.startActivity(intent) }
            .onFailure { showSnackbar("No app can open this link") }
    }

    private fun openExternalUri(uri: Uri) {
        val intent = runCatching {
            if (uri.scheme == "intent") {
                Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
            } else {
                Intent(Intent.ACTION_VIEW, uri)
            }
        }.getOrNull()
        if (intent == null) {
            showSnackbar("No app can open this link")
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { appContext.startActivity(intent) }
            .onFailure { showSnackbar("No app can open this link") }
    }

    // ---------------------------------------------------------------------
    // Downloads
    // ---------------------------------------------------------------------

    fun downloadInfo(id: Long): DownloadInfo? = container.downloadHandler.query(id)

    fun openDownload(id: Long) {
        val intent = container.downloadHandler.open(id)
        if (intent == null) {
            showSnackbar("Download is not ready yet")
            return
        }
        runCatching { appContext.startActivity(intent) }
            .onFailure { showSnackbar("No app can open this file") }
    }

    fun shareDownload(id: Long) {
        val intent = container.downloadHandler.share(id)
        if (intent == null) {
            showSnackbar("Download is not ready yet")
            return
        }
        runCatching { appContext.startActivity(intent) }
            .onFailure { showSnackbar("No app can share this file") }
    }

    fun removeDownload(record: DownloadRecord) {
        container.downloadHandler.remove(record.id)
        viewModelScope.launch {
            container.downloadRepository.remove(record.id)
            showSnackbar("Download removed")
        }
    }

    // ---------------------------------------------------------------------
    // Data and settings
    // ---------------------------------------------------------------------

    fun clearBrowsingData() {
        viewModelScope.launch {
            container.historyRepository.clear()
            container.statsRepository.reset()
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            WebStorage.getInstance().deleteAllData()
            WebViewDatabase.getInstance(appContext).clearFormData()
            WebViewDatabase.getInstance(appContext).clearHttpAuthUsernamePassword()
            webViews.values.forEach {
                it.clearCache(true)
                it.clearHistory()
            }
            showSnackbar("Browsing data cleared")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            container.historyRepository.clear()
            showSnackbar("History cleared")
        }
    }

    fun removeHistoryEntry(id: String) {
        viewModelScope.launch { container.historyRepository.remove(id) }
    }

    fun setThemeMode(value: ThemeMode) = updateSettings {
        container.settingsRepository.setThemeMode(value)
    }

    fun setDynamicColor(value: Boolean) = updateSettings {
        container.settingsRepository.setDynamicColor(value)
    }

    fun setToolbarPosition(value: ToolbarPosition) = updateSettings {
        container.settingsRepository.setToolbarPosition(value)
    }

    fun setHideToolbarOnScroll(value: Boolean) = updateSettings {
        container.settingsRepository.setHideToolbarOnScroll(value)
    }

    fun setAdBlockEnabled(value: Boolean) = updateSettings {
        container.settingsRepository.setAdBlockEnabled(value)
    }

    fun setJavascriptEnabled(value: Boolean) = updateSettings {
        container.settingsRepository.setJavascriptEnabled(value)
    }

    fun setImagesEnabled(value: Boolean) = updateSettings {
        container.settingsRepository.setImagesEnabled(value)
    }

    fun setCookiesEnabled(value: Boolean) = updateSettings {
        container.settingsRepository.setCookiesEnabled(value)
    }

    fun setHttpsOnly(value: Boolean) = updateSettings {
        container.settingsRepository.setHttpsOnly(value)
    }

    fun setSafeBrowsing(value: Boolean) = updateSettings {
        container.settingsRepository.setSafeBrowsing(value)
    }

    fun setSearchSuggestions(value: Boolean) = updateSettings {
        container.settingsRepository.setSearchSuggestions(value)
    }

    fun setRestoreTabs(value: Boolean) = updateSettings {
        container.settingsRepository.setRestoreTabs(value)
    }

    fun setDesktopModeDefault(value: Boolean) = updateSettings {
        container.settingsRepository.setDesktopModeDefault(value)
    }

    fun setDarkWebPages(value: Boolean) = updateSettings {
        container.settingsRepository.setDarkWebPages(value)
    }

    fun setSearchEngineId(value: String) = updateSettings {
        container.settingsRepository.setSearchEngineId(value)
    }

    fun setCustomSearchUrl(value: String) = updateSettings {
        container.settingsRepository.setCustomSearchUrl(value)
    }

    fun setHomepage(value: String) = updateSettings {
        container.settingsRepository.setHomepage(value)
    }

    fun setOpenLinksInNewTab(value: Boolean) = updateSettings {
        container.settingsRepository.setOpenLinksInNewTab(value)
    }

    private fun updateSettings(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    // ---------------------------------------------------------------------
    // UI helpers and lifecycle
    // ---------------------------------------------------------------------

    fun openOverlay(overlay: BrowserOverlay) {
        _uiState.update { it.copy(overlay = overlay, isMenuOpen = false) }
    }

    fun closeOverlay() {
        _uiState.update { it.copy(overlay = BrowserOverlay.NONE) }
    }

    fun openMenu() {
        _uiState.update { it.copy(isMenuOpen = true) }
    }

    fun closeMenu() {
        _uiState.update { it.copy(isMenuOpen = false) }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = fullscreen) }
    }

    fun handleBack(): Boolean {
        val state = _uiState.value
        if (state.jsDialog != null) {
            dismissJsDialog()
            return true
        }
        if (state.isFindOpen) {
            closeFindInPage()
            return true
        }
        if (state.readerContent != null) {
            closeReader()
            return true
        }
        if (state.isSearchOpen) {
            closeSearch()
            return true
        }
        if (state.isMenuOpen) {
            closeMenu()
            return true
        }
        if (state.overlay != BrowserOverlay.NONE) {
            closeOverlay()
            return true
        }
        val webView = activeWebView()
        if (webView?.canGoBack() == true) {
            webView.goBack()
            return true
        }
        val tab = state.activeTab
        if (tab != null && !tab.isHome) {
            goHome()
            return true
        }
        return false
    }

    fun onPause() {
        activeWebView()?.onPause()
        container.statsRepository.persist()
        saveSession()
    }

    fun onResume() {
        activeWebView()?.onResume()
    }

    fun onFinish() {
        container.statsRepository.persist()
        destroyWebViews()
    }

    override fun onCleared() {
        destroyWebViews()
        super.onCleared()
    }

    private fun destroyWebViews() {
        webViews.values.forEach { webView ->
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.stopLoading()
            webView.destroy()
        }
        webViews.clear()
        webViewLastUsed.clear()
    }

    private fun saveSession() {
        viewModelScope.launch { persistSession() }
    }

    private fun scheduleSessionSave() {
        sessionSaveJob?.cancel()
        sessionSaveJob = viewModelScope.launch {
            delay(600)
            persistSession()
        }
    }

    private suspend fun persistSession() {
        val state = _uiState.value
        if (!state.settings.restoreTabs) return
        val tabs = state.tabs.filterNot { it.isIncognito }
        if (tabs.isEmpty()) return
        val activeIndex = tabs.indexOfFirst { it.id == state.activeTabId }.coerceAtLeast(0)
        container.sessionRepository.save(
            SessionState(
                tabs = tabs.map {
                    SessionTab(
                        id = it.id,
                        url = it.url,
                        title = it.title,
                        isDesktopMode = it.isDesktopMode
                    )
                },
                activeIndex = activeIndex
            )
        )
    }

    private fun activeWebView(): WebView? =
        _uiState.value.activeTabId?.let { webViews[it] }

    private fun handleProgress(tabId: String, progress: Int) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tabId) {
                        it.copy(progress = progress, isLoading = progress < 100)
                    } else {
                        it
                    }
                }
            )
        }
    }

    private fun handleTitle(tabId: String, title: String) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tabId) it.copy(title = title) else it
                }
            )
        }
    }

    private fun handleFavicon(tabId: String, icon: android.graphics.Bitmap?) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tabId) it.copy(favicon = icon) else it
                }
            )
        }
    }

    private fun handlePageStarted(tabId: String, url: String) {
        if (url == UrlUtils.BLANK_URL) return
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tabId) {
                        it.copy(
                            url = url,
                            title = UrlUtils.prettyUrl(url),
                            isLoading = true,
                            progress = 0,
                            error = null
                        )
                    } else {
                        it
                    }
                },
                toolbarVisible = true
            )
        }
    }

    private fun handlePageFinished(tabId: String, url: String) {
        if (url == UrlUtils.BLANK_URL) return
        val webView = webViews[tabId]
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) {
                        tab.copy(
                            url = url,
                            title = webView?.title?.takeIf { it.isNotBlank() } ?: tab.title,
                            isLoading = false,
                            progress = 100,
                            canGoBack = webView?.canGoBack() == true,
                            canGoForward = webView?.canGoForward() == true,
                            error = null
                        )
                    } else {
                        tab
                    }
                },
                toolbarVisible = true
            )
        }
        val tab = _uiState.value.tabs.firstOrNull { it.id == tabId } ?: return
        if (!tab.isIncognito && UrlUtils.isHttpUrl(url)) {
            viewModelScope.launch {
                container.historyRepository.record(
                    webView?.title ?: tab.displayTitle,
                    url
                )
            }
            container.statsRepository.incrementPagesLoaded()
        }
        if (_uiState.value.settings.adBlockEnabled) {
            webView?.let { container.adBlocker.injectCosmeticFilters(it) }
        }
        scheduleSessionSave()
    }

    private fun handleUrlChanged(tabId: String, url: String) {
        if (url == UrlUtils.BLANK_URL) return
        val webView = webViews[tabId]
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) {
                        tab.copy(
                            url = url,
                            canGoBack = webView?.canGoBack() == true,
                            canGoForward = webView?.canGoForward() == true
                        )
                    } else {
                        tab
                    }
                }
            )
        }
    }

    private fun handleError(tabId: String, message: String) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tabId) {
                        it.copy(isLoading = false, progress = 0, error = message)
                    } else {
                        it
                    }
                }
            )
        }
    }

    private fun handleRendererGone(tabId: String) {
        webViews.remove(tabId)?.let { webView ->
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
        webViewLastUsed.remove(tabId)
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tabId) {
                        it.copy(
                            isLoading = false,
                            error = "The page stopped responding. Reload to try again."
                        )
                    } else {
                        it
                    }
                }
            )
        }
    }

    private fun showJsDialog(dialog: JsDialogState) {
        _uiState.value.jsDialog?.let { previous ->
            val prompt = previous.promptResult
            if (prompt != null) prompt.cancel() else previous.result.cancel()
        }
        _uiState.update { it.copy(jsDialog = dialog) }
    }

    fun confirmJsDialog(input: String) {
        val dialog = _uiState.value.jsDialog ?: return
        when (dialog.type) {
            JsDialogType.ALERT, JsDialogType.CONFIRM -> dialog.result.confirm()
            JsDialogType.PROMPT -> dialog.promptResult?.confirm(input) ?: dialog.result.confirm()
        }
        _uiState.update { it.copy(jsDialog = null) }
    }

    fun dismissJsDialog() {
        val dialog = _uiState.value.jsDialog ?: return
        val prompt = dialog.promptResult
        if (prompt != null) prompt.cancel() else dialog.result.cancel()
        _uiState.update { it.copy(jsDialog = null) }
    }

    private companion object {
        const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    }
}
