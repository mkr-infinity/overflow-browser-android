package com.overflow.browser.ui.downloads

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.overflow.browser.browser.DownloadInfo
import com.overflow.browser.data.DownloadRecord
import com.overflow.browser.ui.components.EmptyState
import com.overflow.browser.ui.components.LetterAvatar
import com.overflow.browser.ui.components.OverflowIconButton
import com.overflow.browser.util.DateUtils
import kotlinx.coroutines.delay

@Composable
fun DownloadsScreen(
    downloads: List<DownloadRecord>,
    downloadInfo: (Long) -> DownloadInfo?,
    onOpen: (DownloadRecord) -> Unit,
    onShare: (DownloadRecord) -> Unit,
    onDelete: (DownloadRecord) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    contentDescription = "Close downloads",
                    onClick = onClose
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Downloads",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${downloads.size} files",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (downloads.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Download,
                    title = "No downloads yet",
                    message = "Files you download with Overflow will appear here.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(downloads, key = { it.id }) { record ->
                        DownloadRow(
                            record = record,
                            downloadInfo = downloadInfo,
                            onOpen = { onOpen(record) },
                            onShare = { onShare(record) },
                            onDelete = { onDelete(record) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    record: DownloadRecord,
    downloadInfo: (Long) -> DownloadInfo?,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var info by remember(record.id) { mutableStateOf(downloadInfo(record.id)) }

    LaunchedEffect(record.id) {
        while (true) {
            info = downloadInfo(record.id)
            if (info?.isRunning != true) break
            delay(1000)
        }
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LetterAvatar(text = record.fileName, size = 36.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = statusText(info),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (info?.isRunning == true) {
                Spacer(Modifier.size(8.dp))
                LinearProgressIndicator(
                    progress = { info?.progress ?: 0f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onOpen,
                    enabled = info?.isComplete == true
                ) {
                    Text("Open")
                }
                TextButton(
                    onClick = onShare,
                    enabled = info?.isComplete == true
                ) {
                    Text("Share")
                }
                TextButton(onClick = onDelete) {
                    Text("Remove")
                }
            }
        }
    }
}

private fun statusText(info: DownloadInfo?): String {
    if (info == null) return "Preparing download…"
    return when {
        info.isComplete -> "Completed · ${DateUtils.formatBytes(info.totalBytes)}"
        info.isRunning -> "Downloading ${(info.progress * 100).toInt()}%"
        else -> "Download failed"
    }
}
