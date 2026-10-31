package com.overflow.browser.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.overflow.browser.data.Bookmark
import com.overflow.browser.data.BrowserSettings
import com.overflow.browser.data.HistoryEntry
import com.overflow.browser.ui.components.SectionHeader
import com.overflow.browser.ui.components.StatChip
import com.overflow.browser.util.DateUtils

@Composable
fun HomeScreen(
    settings: BrowserSettings,
    bookmarks: List<Bookmark>,
    history: List<HistoryEntry>,
    adsBlocked: Long,
    pagesLoaded: Long,
    onOpenSearch: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenBookmark: (Bookmark) -> Unit,
    onOpenHistory: (HistoryEntry) -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistoryScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultLinks = listOf(
        QuickLink("DuckDuckGo", "https://duckduckgo.com"),
        QuickLink("Wikipedia", "https://www.wikipedia.org"),
        QuickLink("GitHub", "https://github.com"),
        QuickLink("YouTube", "https://www.youtube.com")
    )
    val quickLinks = (bookmarks.map { QuickLink(it.title, it.url) } + defaultLinks)
        .distinctBy { it.url }
        .take(4)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-90).dp, y = (-110).dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(190.dp)
                .offset(x = 250.dp, y = 90.dp)
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
                    CircleShape
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 28.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Overflow",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "PRIVATE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = DateUtils.greeting(),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "A quieter way to browse.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Surface(
                    onClick = onOpenSearch,
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Search the web",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${settings.searchEngine.displayName} · suggestions optional",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatChip(
                        value = adsBlocked.toString(),
                        label = "Ads blocked",
                        icon = Icons.Filled.PrivacyTip,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        value = pagesLoaded.toString(),
                        label = "Pages",
                        icon = Icons.Filled.Public,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        value = bookmarks.size.toString(),
                        label = "Bookmarks",
                        icon = Icons.Filled.Bookmark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Column {
                    SectionHeader(title = "Quick links")
                    Spacer(Modifier.size(10.dp))
                    QuickLinksGrid(links = quickLinks, onOpenUrl = onOpenUrl)
                }
            }

            if (history.isNotEmpty()) {
                item {
                    RecentHistorySection(
                        history = history,
                        onOpen = onOpenHistory,
                        onSeeAll = onOpenHistoryScreen
                    )
                }
            }

            if (bookmarks.isNotEmpty()) {
                item {
                    BookmarkPreviewSection(
                        bookmarks = bookmarks,
                        onOpen = onOpenBookmark,
                        onSeeAll = onOpenBookmarks
                    )
                }
            }

            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PrivacyTip,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Private by default",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "No accounts, no analytics, no background sync. History, bookmarks, and downloads stay on this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
