package com.overflow.browser.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import com.overflow.browser.browser.SearchEngine
import com.overflow.browser.data.BrowserSettings
import com.overflow.browser.data.ToolbarPosition
import com.overflow.browser.ui.components.OverflowIconButton
import com.overflow.browser.ui.theme.ThemeMode

data class SettingsActions(
    val onThemeModeChange: (ThemeMode) -> Unit,
    val onDynamicColorChange: (Boolean) -> Unit,
    val onToolbarPositionChange: (ToolbarPosition) -> Unit,
    val onHideToolbarOnScrollChange: (Boolean) -> Unit,
    val onSearchEngineChange: (String) -> Unit,
    val onCustomSearchUrlChange: (String) -> Unit,
    val onHomepageChange: (String) -> Unit,
    val onAdBlockChange: (Boolean) -> Unit,
    val onJavascriptChange: (Boolean) -> Unit,
    val onImagesChange: (Boolean) -> Unit,
    val onCookiesChange: (Boolean) -> Unit,
    val onHttpsOnlyChange: (Boolean) -> Unit,
    val onSafeBrowsingChange: (Boolean) -> Unit,
    val onSearchSuggestionsChange: (Boolean) -> Unit,
    val onRestoreTabsChange: (Boolean) -> Unit,
    val onDesktopModeDefaultChange: (Boolean) -> Unit,
    val onDarkWebPagesChange: (Boolean) -> Unit,
    val onReaderFontScaleChange: (Float) -> Unit,
    val onOpenLinksInNewTabChange: (Boolean) -> Unit,
    val onClearHistory: () -> Unit,
    val onClearCookies: () -> Unit,
    val onOpenAbout: () -> Unit,
    val onOpenGitHub: () -> Unit,
    val onOpenLicense: () -> Unit
)

private enum class ConfirmAction {
    CLEAR_HISTORY,
    CLEAR_COOKIES
}

@Composable
fun SettingsScreen(
    settings: BrowserSettings,
    actions: SettingsActions,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showToolbarDialog by remember { mutableStateOf(false) }
    var showEngineDialog by remember { mutableStateOf(false) }
    var showHomepageDialog by remember { mutableStateOf(false) }
    var showCustomSearchDialog by remember { mutableStateOf(false) }
    var confirmAction by remember { mutableStateOf<ConfirmAction?>(null) }
    var homepageInput by remember(settings.homepage) { mutableStateOf(settings.homepage) }
    var customSearchInput by remember(settings.customSearchUrl) {
        mutableStateOf(settings.customSearchUrl)
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
                    contentDescription = "Close settings",
                    onClick = onClose
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Overflow Browser",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    SettingsSection(title = "Appearance") {
                        SettingsChoiceRow(
                            icon = Icons.Filled.Palette,
                            title = "Theme",
                            value = settings.themeMode.label(),
                            onClick = { showThemeDialog = true }
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.ColorLens,
                            title = "Dynamic color",
                            subtitle = "Match your wallpaper on Android 12+",
                            checked = settings.dynamicColor,
                            onCheckedChange = actions.onDynamicColorChange
                        )
                        SettingsDivider()
                        SettingsChoiceRow(
                            icon = Icons.Filled.SwapVert,
                            title = "Toolbar position",
                            value = if (settings.toolbarPosition == ToolbarPosition.TOP) {
                                "Top"
                            } else {
                                "Bottom"
                            },
                            onClick = { showToolbarDialog = true }
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.VisibilityOff,
                            title = "Hide toolbar on scroll",
                            subtitle = "More room for the page while reading",
                            checked = settings.hideToolbarOnScroll,
                            onCheckedChange = actions.onHideToolbarOnScrollChange
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.DarkMode,
                            title = "Dark web pages",
                            subtitle = "Ask sites for their dark theme when available",
                            checked = settings.darkWebPages,
                            onCheckedChange = actions.onDarkWebPagesChange
                        )
                    }
                }

                item {
                    SettingsSection(title = "Search") {
                        SettingsChoiceRow(
                            icon = Icons.Filled.Search,
                            title = "Search engine",
                            value = settings.searchEngine.displayName,
                            onClick = { showEngineDialog = true }
                        )
                        if (settings.searchEngine == SearchEngine.CUSTOM) {
                            SettingsDivider()
                            SettingsChoiceRow(
                                icon = Icons.Filled.Code,
                                title = "Custom search URL",
                                value = if (settings.customSearchUrl.isBlank()) {
                                    "Not set"
                                } else {
                                    "Configured"
                                },
                                onClick = { showCustomSearchDialog = true }
                            )
                        }
                        SettingsDivider()
                        SettingsChoiceRow(
                            icon = Icons.Filled.Home,
                            title = "Homepage",
                            value = settings.homepage,
                            onClick = { showHomepageDialog = true }
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.Lightbulb,
                            title = "Search suggestions",
                            subtitle = "Optional suggestions from your engine",
                            checked = settings.searchSuggestions,
                            onCheckedChange = actions.onSearchSuggestionsChange
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.OpenInNew,
                            title = "Open links in a new tab",
                            subtitle = "Keep the current page open",
                            checked = settings.openLinksInNewTab,
                            onCheckedChange = actions.onOpenLinksInNewTabChange
                        )
                    }
                }

                item {
                    SettingsSection(title = "Privacy & security") {
                        SettingsSwitchRow(
                            icon = Icons.Filled.PrivacyTip,
                            title = "Block ads and trackers",
                            subtitle = "Host-based blocking with cosmetic filters",
                            checked = settings.adBlockEnabled,
                            onCheckedChange = actions.onAdBlockChange
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.Lock,
                            title = "HTTPS-only mode",
                            subtitle = "Upgrade typed addresses to HTTPS",
                            checked = settings.httpsOnly,
                            onCheckedChange = actions.onHttpsOnlyChange
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.Security,
                            title = "Safe Browsing",
                            subtitle = "Warn about known dangerous sites",
                            checked = settings.safeBrowsing,
                            onCheckedChange = actions.onSafeBrowsingChange
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.Language,
                            title = "Cookies",
                            subtitle = "Allow sites to keep sign-in and preferences",
                            checked = settings.cookiesEnabled,
                            onCheckedChange = actions.onCookiesChange
                        )
                        SettingsDivider()
                        SettingsActionRow(
                            icon = Icons.Filled.DeleteSweep,
                            title = "Clear history",
                            subtitle = "Remove every saved page",
                            onClick = { confirmAction = ConfirmAction.CLEAR_HISTORY },
                            tint = MaterialTheme.colorScheme.error
                        )
                        SettingsDivider()
                        SettingsActionRow(
                            icon = Icons.Filled.Delete,
                            title = "Clear cookies",
                            subtitle = "Sign out of sites on this device",
                            onClick = { confirmAction = ConfirmAction.CLEAR_COOKIES },
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                item {
                    SettingsSection(title = "Browsing") {
                        SettingsSwitchRow(
                            icon = Icons.Filled.Code,
                            title = "JavaScript",
                            subtitle = "Required by many modern sites",
                            checked = settings.javascriptEnabled,
                            onCheckedChange = actions.onJavascriptChange
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.Image,
                            title = "Load images",
                            subtitle = "Turn off to save data",
                            checked = settings.imagesEnabled,
                            onCheckedChange = actions.onImagesChange
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.DesktopWindows,
                            title = "Desktop site by default",
                            subtitle = "Use a desktop user agent for new tabs",
                            checked = settings.desktopModeDefault,
                            onCheckedChange = actions.onDesktopModeDefaultChange
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Filled.Restore,
                            title = "Restore tabs",
                            subtitle = "Reopen normal tabs after a restart",
                            checked = settings.restoreTabs,
                            onCheckedChange = actions.onRestoreTabsChange
                        )
                    }
                }

                item {
                    SettingsSection(title = "Reading") {
                        SettingsSliderRow(
                            title = "Reader text size",
                            subtitle = "Adjust the reader view",
                            value = settings.readerFontScale,
                            valueRange = 0.8f..1.8f,
                            valueLabel = "${(settings.readerFontScale * 100).toInt()}%",
                            onValueChange = actions.onReaderFontScaleChange
                        )
                    }
                }

                item {
                    SettingsSection(title = "About") {
                        SettingsActionRow(
                            icon = Icons.Filled.Info,
                            title = "About Overflow",
                            subtitle = "Credits, license, and version",
                            onClick = actions.onOpenAbout
                        )
                        SettingsDivider()
                        SettingsActionRow(
                            icon = Icons.Filled.Code,
                            title = "Source code",
                            subtitle = "View the project on GitHub",
                            onClick = actions.onOpenGitHub
                        )
                        SettingsDivider()
                        SettingsActionRow(
                            icon = Icons.Filled.Description,
                            title = "Open-source license",
                            subtitle = "Apache License 2.0",
                            onClick = actions.onOpenLicense
                        )
                    }
                }

                item {
                    Text(
                        text = "Overflow Browser is free and open source. No accounts, no analytics.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        ChoiceDialog(
            title = "Theme",
            options = ThemeMode.entries.map { it.label() to (settings.themeMode == it) },
            onSelect = { index ->
                actions.onThemeModeChange(ThemeMode.entries[index])
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showToolbarDialog) {
        ChoiceDialog(
            title = "Toolbar position",
            options = ToolbarPosition.entries.map {
                val label = if (it == ToolbarPosition.TOP) "Top" else "Bottom"
                label to (settings.toolbarPosition == it)
            },
            onSelect = { index ->
                actions.onToolbarPositionChange(ToolbarPosition.entries[index])
                showToolbarDialog = false
            },
            onDismiss = { showToolbarDialog = false }
        )
    }

    if (showEngineDialog) {
        ChoiceDialog(
            title = "Search engine",
            options = SearchEngine.entries.map {
                it.displayName to (settings.searchEngine == it)
            },
            onSelect = { index ->
                val engine = SearchEngine.entries[index]
                actions.onSearchEngineChange(engine.id)
                showEngineDialog = false
                if (engine == SearchEngine.CUSTOM) {
                    showCustomSearchDialog = true
                }
            },
            onDismiss = { showEngineDialog = false }
        )
    }

    if (showHomepageDialog) {
        AlertDialog(
            onDismissRequest = { showHomepageDialog = false },
            title = { Text("Homepage") },
            text = {
                Column {
                    OutlinedTextField(
                        value = homepageInput,
                        onValueChange = { homepageInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Address") },
                        placeholder = { Text("overflow://home") },
                        singleLine = true
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Use overflow://home for the start page.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        actions.onHomepageChange(normalizeHomepage(homepageInput))
                        showHomepageDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHomepageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCustomSearchDialog) {
        AlertDialog(
            onDismissRequest = { showCustomSearchDialog = false },
            title = { Text("Custom search URL") },
            text = {
                Column {
                    OutlinedTextField(
                        value = customSearchInput,
                        onValueChange = { customSearchInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Template") },
                        placeholder = { Text("https://example.com/search?q=%s") },
                        singleLine = true
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Use %s where the search query should go.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        actions.onCustomSearchUrlChange(customSearchInput.trim())
                        showCustomSearchDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomSearchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val action = confirmAction
    if (action != null) {
        AlertDialog(
            onDismissRequest = { confirmAction = null },
            title = {
                Text(
                    if (action == ConfirmAction.CLEAR_HISTORY) {
                        "Clear all history?"
                    } else {
                        "Clear all cookies?"
                    }
                )
            },
            text = {
                Text(
                    if (action == ConfirmAction.CLEAR_HISTORY) {
                        "Every saved page will be removed from this device."
                    } else {
                        "You will be signed out of sites that use cookies."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (action) {
                            ConfirmAction.CLEAR_HISTORY -> actions.onClearHistory()
                            ConfirmAction.CLEAR_COOKIES -> actions.onClearCookies()
                        }
                        confirmAction = null
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ChoiceDialog(
    title: String,
    options: List<Pair<String, Boolean>>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEachIndexed { index, (label, selected) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { onSelect(index) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.BLACK -> "Black (OLED)"
}

private fun normalizeHomepage(value: String): String {
    val trimmed = value.trim()
    return when {
        trimmed.isEmpty() -> "overflow://home"
        trimmed.startsWith("overflow://", ignoreCase = true) -> trimmed
        trimmed.startsWith("http://", ignoreCase = true) -> trimmed
        trimmed.startsWith("https://", ignoreCase = true) -> trimmed
        trimmed.contains("://") -> trimmed
        else -> "https://$trimmed"
    }
}
