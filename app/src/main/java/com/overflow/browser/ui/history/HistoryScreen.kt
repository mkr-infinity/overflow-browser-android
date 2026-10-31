package com.overflow.browser.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.overflow.browser.data.HistoryEntry
import com.overflow.browser.ui.components.EmptyState
import com.overflow.browser.ui.components.LetterAvatar
import com.overflow.browser.ui.components.OverflowIconButton
import com.overflow.browser.util.DateUtils
import com.overflow.browser.util.UrlUtils

@Composable
fun HistoryScreen(
    history: List<HistoryEntry>,
    onOpen: (HistoryEntry) -> Unit,
    onDelete: (HistoryEntry) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }
    val filtered = remember(history, query) {
        if (query.isBlank()) {
            history
        } else {
            history.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.url.contains(query, ignoreCase = true)
            }
        }
    }
    val grouped = remember(filtered) {
        filtered.groupBy { DateUtils.dayLabel(it.visitedAt) }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OverflowIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = "Close history",
                    onClick = onClose
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${history.size} pages",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { confirmClear = true },
                    enabled = history.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Clear history"
                    )
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search history") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                shape = CircleShape
            )

            Spacer(Modifier.size(12.dp))

            when {
                history.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.History,
                        title = "No history yet",
                        message = "Pages you visit will appear here. Private tabs are never saved.",
                        modifier = Modifier.weight(1f)
                    )
                }
                filtered.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = "No matches",
                        message = "Try a different title or URL.",
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        grouped.forEach { (label, entries) ->
                            item(key = "header-$label") {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                )
                            }
                            items(entries, key = { it.id }) { entry ->
                                HistoryRow(
                                    entry = entry,
                                    onClick = { onOpen(entry) },
                                    onDelete = { onDelete(entry) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all history?") },
            text = { Text("Every saved page will be removed from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        onClearAll()
                    }
                ) {
                    Text("Clear all")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HistoryRow(
    entry: HistoryEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LetterAvatar(text = entry.title, size = 34.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${UrlUtils.host(entry.url)} · ${DateUtils.formatTime(entry.visitedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete history entry",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
