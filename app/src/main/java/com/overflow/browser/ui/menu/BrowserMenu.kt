package com.overflow.browser.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.overflow.browser.browser.BrowserTab
import com.overflow.browser.ui.components.LetterAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMenu(
    tab: BrowserTab?,
    onDismiss: () -> Unit,
    onNewTab: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onBookmarks: () -> Unit,
    onHistory: () -> Unit,
    onDownloads: () -> Unit,
    onFindInPage: () -> Unit,
    onToggleDesktop: () -> Unit,
    onReaderMode: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenExternal: () -> Unit,
    onSettings: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hasPage = tab != null && !tab.isHome

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LetterAvatar(text = tab?.displayTitle ?: "Overflow", size = 38.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Overflow",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = tab?.displayTitle ?: "Start page",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close menu"
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    MenuAction(
                        icon = Icons.Filled.Add,
                        label = "New tab",
                        onClick = onNewTab
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.VisibilityOff,
                        label = "Private tab",
                        onClick = onNewPrivateTab
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.Bookmark,
                        label = "Bookmarks",
                        onClick = onBookmarks
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.History,
                        label = "History",
                        onClick = onHistory
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.Download,
                        label = "Downloads",
                        onClick = onDownloads
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.Search,
                        label = "Find",
                        onClick = onFindInPage,
                        enabled = hasPage
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.DesktopWindows,
                        label = if (tab?.isDesktopMode == true) "Mobile" else "Desktop",
                        onClick = onToggleDesktop,
                        enabled = hasPage
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.Article,
                        label = "Reader",
                        onClick = onReaderMode,
                        enabled = hasPage
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.Share,
                        label = "Share",
                        onClick = onShare,
                        enabled = hasPage
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.ContentCopy,
                        label = "Copy link",
                        onClick = onCopyLink,
                        enabled = hasPage
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.OpenInNew,
                        label = "Open in app",
                        onClick = onOpenExternal,
                        enabled = hasPage
                    )
                }
                item {
                    MenuAction(
                        icon = Icons.Filled.Settings,
                        label = "Settings",
                        onClick = onSettings
                    )
                }
            }

            Text(
                text = "Private by default · No analytics",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun MenuAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (enabled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
