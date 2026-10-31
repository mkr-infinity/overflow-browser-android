package com.overflow.browser.ui.browser

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.overflow.browser.browser.BrowserTab
import com.overflow.browser.ui.components.OverflowIconButton

@Composable
fun BrowserToolbar(
    tab: BrowserTab?,
    tabCount: Int,
    isLoading: Boolean,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OverflowIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                enabled = canGoBack
            )
            OverflowIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Forward",
                onClick = onForward,
                enabled = canGoForward
            )

            Surface(
                onClick = onOpenSearch,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier.padding(start = 14.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            tab?.isSecure == true -> Icons.Filled.Lock
                            tab?.isHome != false -> Icons.Filled.Search
                            else -> Icons.Filled.Public
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (tab == null || tab.isHome) {
                            "Search or enter address"
                        } else {
                            tab.displayUrl
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    IconButton(
                        onClick = if (isLoading) onStop else onReload,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isLoading) Icons.Filled.Close else Icons.Filled.Refresh,
                            contentDescription = if (isLoading) "Stop loading" else "Reload",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onOpenTabs,
                modifier = Modifier.size(40.dp)
            ) {
                BadgedBox(
                    badge = {
                        Badge {
                            Text(tabCount.toString())
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tab,
                        contentDescription = "Tabs",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            OverflowIconButton(
                icon = Icons.Filled.MoreVert,
                contentDescription = "Menu",
                onClick = onOpenMenu
            )
        }
    }
}
