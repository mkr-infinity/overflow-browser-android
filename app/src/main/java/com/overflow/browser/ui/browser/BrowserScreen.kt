package com.overflow.browser.ui.browser

import android.content.Intent
import android.net.Uri
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.overflow.browser.browser.BrowserOverlay
import com.overflow.browser.browser.BrowserViewModel
import com.overflow.browser.data.Bookmark
import com.overflow.browser.data.ToolbarPosition
import com.overflow.browser.ui.about.AboutScreen
import com.overflow.browser.ui.bookmarks.BookmarkDialog
import com.overflow.browser.ui.bookmarks.BookmarksScreen
import com.overflow.browser.ui.dialog.JsDialogHost
import com.overflow.browser.ui.downloads.DownloadsScreen
import com.overflow.browser.ui.error.ErrorOverlay
import com.overflow.browser.ui.find.FindBar
import com.overflow.browser.ui.history.HistoryScreen
import com.overflow.browser.ui.home.HomeScreen
import com.overflow.browser.ui.menu.BrowserMenu
import com.overflow.browser.ui.reader.ReaderScreen
import com.overflow.browser.ui.search.SearchOverlay
import com.overflow.browser.ui.settings.SettingsActions
import com.overflow.browser.ui.settings.SettingsScreen
import com.overflow.browser.ui.tabs.TabSwitcher

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState(initial = emptyList())
    val history by viewModel.history.collectAsState(initial = emptyList())
    val downloads by viewModel.downloads.collectAsState(initial = emptyList())
    val adsBlocked by viewModel.adsBlocked.collectAsState(initial = 0L)
    val pagesLoaded by viewModel.pagesLoaded.collectAsState(initial = 0L)

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var bookmarkDialog by remember { mutableStateOf<BookmarkDialogState?>(null) }

    fun openExternal(url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    fun openUrl(url: String) {
        val tabId = state.activeTabId
        if (tabId != null) {
            viewModel.loadUrlInTab(tabId, url)
        } else {
            viewModel.newTab(url)
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeSnackbar()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val reader = state.readerContent
        if (reader != null) {
            ReaderScreen(
                content = reader,
                fontScale = state.settings.readerFontScale,
                onFontScaleChange = viewModel::setReaderFontScale,
                onClose = viewModel::closeReader,
                onShare = viewModel::shareCurrentPage,
                onOpenOriginal = {
                    viewModel.closeReader()
                    openUrl(reader.url)
                }
            )
        } else {
            when (state.overlay) {
                BrowserOverlay.NONE -> {
                    BrowserContent(
                        viewModel = viewModel,
                        state = state,
                        bookmarks = bookmarks,
                        history = history,
                        adsBlocked = adsBlocked,
                        pagesLoaded = pagesLoaded,
                        onOpenUrl = ::openUrl
                    )
                }

                BrowserOverlay.TABS -> {
                    TabSwitcher(
                        tabs = state.tabs,
                        activeTabId = state.activeTabId,
                        onSelectTab = {
                            viewModel.selectTab(it)
                            viewModel.closeOverlay()
                        },
                        onCloseTab = viewModel::closeTab,
                        onNewTab = {
                            viewModel.newTab()
                            viewModel.closeOverlay()
                        },
                        onNewPrivateTab = {
                            viewModel.newPrivateTab()
                            viewModel.closeOverlay()
                        },
                        onCloseAll = { viewModel.closeAllTabs() },
                        onClose = viewModel::closeOverlay
                    )
                }

                BrowserOverlay.BOOKMARKS -> {
                    BookmarksScreen(
                        bookmarks = bookmarks,
                        onOpen = {
                            openUrl(it.url)
                            viewModel.closeOverlay()
                        },
                        onAdd = {
                            bookmarkDialog = BookmarkDialogState(
                                bookmark = null,
                                title = "",
                                url = ""
                            )
                        },
                        onEdit = {
                            bookmarkDialog = BookmarkDialogState(
                                bookmark = it,
                                title = it.title,
                                url = it.url
                            )
                        },
                        onDelete = { viewModel.removeBookmark(it.id) },
                        onClose = viewModel::closeOverlay
                    )
                }

                BrowserOverlay.HISTORY -> {
                    HistoryScreen(
                        history = history,
                        onOpen = {
                            openUrl(it.url)
                            viewModel.closeOverlay()
                        },
                        onDelete = { viewModel.removeHistoryEntry(it.id) },
                        onClearAll = viewModel::clearHistory,
                        onClose = viewModel::closeOverlay
                    )
                }

                BrowserOverlay.DOWNLOADS -> {
                    DownloadsScreen(
                        downloads = downloads,
                        downloadInfo = viewModel::downloadInfo,
                        onOpen = { viewModel.openDownload(it.id) },
                        onShare = { viewModel.shareDownload(it.id) },
                        onDelete = viewModel::removeDownload,
                        onClose = viewModel::closeOverlay
                    )
                }

                BrowserOverlay.SETTINGS -> {
                    SettingsScreen(
                        settings = state.settings,
                        actions = SettingsActions(
                            onThemeModeChange = viewModel::setThemeMode,
                            onDynamicColorChange = viewModel::setDynamicColor,
                            onToolbarPositionChange = viewModel::setToolbarPosition,
                            onHideToolbarOnScrollChange = viewModel::setHideToolbarOnScroll,
                            onSearchEngineChange = viewModel::setSearchEngineId,
                            onCustomSearchUrlChange = viewModel::setCustomSearchUrl,
                            onHomepageChange = viewModel::setHomepage,
                            onAdBlockChange = viewModel::setAdBlockEnabled,
                            onJavascriptChange = viewModel::setJavascriptEnabled,
                            onImagesChange = viewModel::setImagesEnabled,
                            onCookiesChange = viewModel::setCookiesEnabled,
                            onHttpsOnlyChange = viewModel::setHttpsOnly,
                            onSafeBrowsingChange = viewModel::setSafeBrowsing,
                            onSearchSuggestionsChange = viewModel::setSearchSuggestions,
                            onRestoreTabsChange = viewModel::setRestoreTabs,
                            onDesktopModeDefaultChange = viewModel::setDesktopModeDefault,
                            onDarkWebPagesChange = viewModel::setDarkWebPages,
                            onReaderFontScaleChange = viewModel::setReaderFontScale,
                            onOpenLinksInNewTabChange = viewModel::setOpenLinksInNewTab,
                            onClearHistory = viewModel::clearHistory,
                            onClearCookies = viewModel::clearBrowsingData,
                            onOpenAbout = { viewModel.openOverlay(BrowserOverlay.ABOUT) },
                            onOpenGitHub = {
                                openExternal("https://github.com/usercoboams/overflow-browser-android")
                            },
                            onOpenLicense = {
                                openExternal("https://www.apache.org/licenses/LICENSE-2.0")
                            }
                        ),
                        onClose = viewModel::closeOverlay
                    )
                }

                BrowserOverlay.ABOUT -> {
                    AboutScreen(
                        onClose = viewModel::closeOverlay,
                        onOpenGitHub = {
                            openExternal("https://github.com/usercoboams/overflow-browser-android")
                        },
                        onOpenLicense = {
                            openExternal("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    )
                }
            }
        }

        if (state.isSearchOpen) {
            SearchOverlay(
                query = state.searchQuery,
                suggestions = state.suggestions,
                engineName = state.settings.searchEngine.displayName,
                onQueryChange = viewModel::updateSearchQuery,
                onSubmit = { viewModel.submitSearch(it) },
                onClose = viewModel::closeSearch
            )
        }

        if (state.isMenuOpen) {
            BrowserMenu(
                tab = state.activeTab,
                onDismiss = viewModel::closeMenu,
                onNewTab = {
                    viewModel.newTab()
                    viewModel.closeMenu()
                },
                onNewPrivateTab = {
                    viewModel.newPrivateTab()
                    viewModel.closeMenu()
                },
                onBookmarks = { viewModel.openOverlay(BrowserOverlay.BOOKMARKS) },
                onHistory = { viewModel.openOverlay(BrowserOverlay.HISTORY) },
                onDownloads = { viewModel.openOverlay(BrowserOverlay.DOWNLOADS) },
                onFindInPage = {
                    viewModel.closeMenu()
                    viewModel.openFindInPage()
                },
                onToggleDesktop = {
                    viewModel.toggleDesktopMode()
                    viewModel.closeMenu()
                },
                onReaderMode = {
                    viewModel.closeMenu()
                    viewModel.openReader()
                },
                onShare = {
                    viewModel.closeMenu()
                    viewModel.shareCurrentPage()
                },
                onCopyLink = {
                    viewModel.closeMenu()
                    viewModel.copyCurrentUrl()
                },
                onOpenExternal = {
                    viewModel.closeMenu()
                    viewModel.openInExternalBrowser()
                },
                onSettings = { viewModel.openOverlay(BrowserOverlay.SETTINGS) }
            )
        }

        val jsDialog = state.jsDialog
        if (jsDialog != null) {
            JsDialogHost(
                dialog = jsDialog,
                onConfirm = viewModel::confirmJsDialog,
                onDismiss = viewModel::dismissJsDialog
            )
        }

        val dialogState = bookmarkDialog
        if (dialogState != null) {
            BookmarkDialog(
                initialTitle = dialogState.title,
                initialUrl = dialogState.url,
                isEditing = dialogState.bookmark != null,
                onDismiss = { bookmarkDialog = null },
                onSave = { title, url ->
                    val editing = dialogState.bookmark
                    if (editing != null) {
                        viewModel.updateBookmark(editing, title, url)
                    } else {
                        viewModel.addBookmark(title, url)
                    }
                    bookmarkDialog = null
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun BrowserContent(
    viewModel: BrowserViewModel,
    state: com.overflow.browser.browser.BrowserUiState,
    bookmarks: List<Bookmark>,
    history: List<com.overflow.browser.data.HistoryEntry>,
    adsBlocked: Long,
    pagesLoaded: Long,
    onOpenUrl: (String) -> Unit
) {
    val activeTab = state.activeTab
    val showToolbar = state.toolbarVisible && !state.isFullscreen
    val toolbarAtTop = state.settings.toolbarPosition == ToolbarPosition.TOP

    Column(modifier = Modifier.fillMaxSize()) {
        if (toolbarAtTop) {
            AnimatedVisibility(visible = showToolbar) {
                BrowserToolbar(
                    tab = activeTab,
                    tabCount = state.tabs.size,
                    isLoading = activeTab?.isLoading == true,
                    canGoBack = activeTab?.canGoBack == true,
                    canGoForward = activeTab?.canGoForward == true,
                    onBack = viewModel::goBack,
                    onForward = viewModel::goForward,
                    onReload = viewModel::reload,
                    onStop = viewModel::stopLoading,
                    onOpenSearch = viewModel::openSearch,
                    onOpenTabs = { viewModel.openOverlay(BrowserOverlay.TABS) },
                    onOpenMenu = viewModel::openMenu
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (activeTab == null || activeTab.isHome) {
                HomeScreen(
                    settings = state.settings,
                    bookmarks = bookmarks,
                    history = history,
                    adsBlocked = adsBlocked,
                    pagesLoaded = pagesLoaded,
                    onOpenSearch = viewModel::openSearch,
                    onOpenUrl = onOpenUrl,
                    onOpenBookmark = { onOpenUrl(it.url) },
                    onOpenHistory = { onOpenUrl(it.url) },
                    onOpenBookmarks = { viewModel.openOverlay(BrowserOverlay.BOOKMARKS) },
                    onOpenHistoryScreen = { viewModel.openOverlay(BrowserOverlay.HISTORY) }
                )
            } else {
                WebViewHost(viewModel = viewModel)

                if (state.isFindOpen) {
                    FindBar(
                        query = state.findQuery,
                        matches = state.findMatches,
                        current = state.findCurrent,
                        onQueryChange = viewModel::updateFindQuery,
                        onNext = { viewModel.findNext(true) },
                        onPrevious = { viewModel.findNext(false) },
                        onClose = viewModel::closeFindInPage,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                val error = activeTab.error
                if (error != null) {
                    ErrorOverlay(
                        message = error,
                        onRetry = viewModel::reload,
                        onGoHome = viewModel::goHome
                    )
                }
            }
        }

        if (!toolbarAtTop) {
            AnimatedVisibility(visible = showToolbar) {
                BrowserToolbar(
                    tab = activeTab,
                    tabCount = state.tabs.size,
                    isLoading = activeTab?.isLoading == true,
                    canGoBack = activeTab?.canGoBack == true,
                    canGoForward = activeTab?.canGoForward == true,
                    onBack = viewModel::goBack,
                    onForward = viewModel::goForward,
                    onReload = viewModel::reload,
                    onStop = viewModel::stopLoading,
                    onOpenSearch = viewModel::openSearch,
                    onOpenTabs = { viewModel.openOverlay(BrowserOverlay.TABS) },
                    onOpenMenu = viewModel::openMenu
                )
            }
        }
    }
}

@Composable
private fun WebViewHost(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val container = remember { mutableStateOf<FrameLayout?>(null) }
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            FrameLayout(context).also {
                it.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                container.value = it
                viewModel.attachContainer(it)
            }
        },
        update = { viewModel.attachActiveWebView(it) }
    )
    DisposableEffect(Unit) {
        onDispose {
            container.value?.let(viewModel::detachContainer)
        }
    }
}

private data class BookmarkDialogState(
    val bookmark: Bookmark?,
    val title: String,
    val url: String
)
