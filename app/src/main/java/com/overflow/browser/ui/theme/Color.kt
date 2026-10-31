package com.overflow.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentTab = state.tabs.getOrElse(state.currentTabIndex) { state.tabs.first() }
    var urlInput by remember(currentTab.url) { mutableStateOf(currentTab.url) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        trailingIcon = {
                            if (state.isPrivateMode) {
                                Icon(Icons.Default.VisibilityOff, contentDescription = null)
                            } else {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.addTab() }) { Icon(Icons.Default.Add, contentDescription = "New tab") }
                IconButton(onClick = { viewModel.togglePrivateMode() }) { Icon(if (state.isPrivateMode) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Private mode") }
                IconButton(onClick = { /* settings */ }) { Icon(Icons.Default.MoreVert, contentDescription = "Menu") }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* back */ }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            IconButton(onClick = { /* forward */ }) { Icon(Icons.Default.ArrowForward, contentDescription = "Forward") }
            IconButton(onClick = { /* refresh */ }) { Icon(Icons.Default.Refresh, contentDescription = "Reload") }
            IconButton(onClick = { /* home */ }) { Icon(Icons.Default.Home, contentDescription = "Home") }
            IconButton(onClick = { viewModel.toggleAdBlock() }) { Icon(Icons.Default.Shield, contentDescription = "Ad blocker") }
            IconButton(onClick = { viewModel.toggleTrackerProtection() }) { Icon(Icons.Default.Tab, contentDescription = "Tracker protection") }
            Button(onClick = {
                viewModel.openUrl(urlInput)
            }) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Go")
            }
        }

        if (currentTab.url == "about:home") {
            HomeScreen(
                onSearch = { query ->
                    viewModel.openUrl(query)
                    urlInput = query
                },
                onOpenUrl = { url ->
                    viewModel.openUrl(url)
                    urlInput = url
                }
            )
        } else {
            val webViewRef = rememberWebView(context, state, viewModel)
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { webViewRef },
                update = { webView ->
                    if (webView.url != currentTab.url) {
                        webView.loadUrl(currentTab.url)
                    }
                }
            )
        }

        if (state.tabs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tabs: ${state.tabs.size}")
                Text("Private: ${if (state.isPrivateMode) "On" else "Off"}")
            }
        }
    }
}

@Composable
fun HomeScreen(
    onSearch: (String) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val quickLinks = listOf(
        "https://www.google.com",
        "https://www.youtube.com",
        "https://www.github.com",
        "https://www.wikipedia.org"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        placeholder = { Text("Search the web") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { onSearch("overflow") }) {
                        Text("Search now")
                    }
                }
            }
        }

        item {
            Text("Quick links", fontWeight = FontWeight.Bold)
        }

        items(quickLinks) { link ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenUrl(link) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(link, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun rememberWebView(
    context: Context,
    state: BrowserUiState,
    viewModel: BrowserViewModel
): WebView {
    val currentTab = state.tabs.getOrElse(state.currentTabIndex) { state.tabs.first() }
    return remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webChromeClient = WebChromeClient()
            webViewClient = OverflowWebViewClient(
                onPageStarted = { url ->
                    viewModel.updateCurrentTabUrl(url)
                },
                onPageFinished = { url ->
                    viewModel.updateCurrentTabTitle(url)
                    viewModel.updateCurrentTabUrl(url)
                },
                shouldBlockRequests = { url ->
                    state.adBlockEnabled && AdBlocker.shouldBlock(url)
                }
            )

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.databaseEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.setSupportMultipleWindows(true)
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            val cookieManager = android.webkit.CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            if (currentTab.isPrivate) {
                cookieManager.removeAllCookies(null)
            }
        }
    }
}
