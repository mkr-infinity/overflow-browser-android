package com.overflow.browser.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.overflow.browser.BuildConfig
import com.overflow.browser.ui.components.LetterAvatar
import com.overflow.browser.ui.components.OverflowIconButton

@Composable
fun AboutScreen(
    onClose: () -> Unit,
    onOpenGitHub: () -> Unit,
    onOpenLicense: () -> Unit,
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
                    contentDescription = "Close about",
                    onClick = onClose
                )
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LetterAvatar(
                    text = "Overflow",
                    size = 76.dp,
                    background = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Overflow Browser",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "A lightweight, private, open-source browser built with Kotlin and Jetpack Compose.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                AboutCard(
                    icon = Icons.Filled.Favorite,
                    title = "Built by",
                    body = "Mohammad Kaif Raja (@mkr-infinity)"
                )
                Spacer(Modifier.height(12.dp))
                AboutCard(
                    icon = Icons.Filled.Public,
                    title = "Free and open source",
                    body = "Overflow is free to use, study, modify, and share under the Apache License 2.0."
                )
                Spacer(Modifier.height(12.dp))
                AboutCard(
                    icon = Icons.Filled.Code,
                    title = "Source code",
                    body = "The full source, issue tracker, and contribution guide live on GitHub.",
                    actionLabel = "Open GitHub",
                    onAction = onOpenGitHub
                )
                Spacer(Modifier.height(12.dp))
                AboutCard(
                    icon = Icons.Filled.Description,
                    title = "License",
                    body = "Apache License 2.0. See the LICENSE file in the repository.",
                    actionLabel = "View license",
                    onAction = onOpenLicense
                )

                Spacer(Modifier.height(24.dp))
                Text(
                    text = "No accounts. No analytics. Your data stays on your device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AboutCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onAction != null) {
                TextButton(
                    onClick = onAction,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
