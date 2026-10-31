# Overflow Browser — Architecture & Design

This document details the architectural layout, core subsystems, and data flow of Overflow Browser.

```
┌────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI                      │
│   (HomeScreen, BrowserToolbar, TabSwitcher, SettingsScreen)   │
└───────────────────────────────┬────────────────────────────────┘
                                │ observes state / sends intents
                                ▼
┌────────────────────────────────────────────────────────────────┐
│                       BrowserViewModel                         │
│     - StateFlow<BrowserUiState> (Tabs, URL, Progress, Reader)  │
│     - Tab lifecycle (create, select, close, restore)           │
└───────┬───────────────────────┬────────────────────────┬───────┘
        │                       │                        │
        ▼                       ▼                        ▼
┌───────────────┐       ┌───────────────┐        ┌───────────────┐
│ WebView Engine│       │ Data Repos    │        │ SettingsStore │
│- Clients      │       │- Bookmarks    │        │- Preferences  │
│- ChromeDeleg  │       │- History      │        │  DataStore    │
│- AdBlocker    │       │- Downloads    │        │               │
│- ReaderMode   │       │- Sessions     │        │               │
└───────────────┘       └───────────────┘        └───────────────┘
```

## Architectural Principles

1. **Unidirectional Data Flow (UDF)**:
   - UI components observe immutable `BrowserUiState` through Kotlin `StateFlow`.
   - UI actions (such as navigating to a URL, toggling incognito, adding bookmarks) invoke explicit methods on `BrowserViewModel`.

2. **No Heavy Third-Party Frameworks**:
   - Dependency injection is handled cleanly via `AppContainer` instantiated in `OverflowBrowserApp`.
   - Persistence is implemented using lightweight, thread-safe JSON files (`JsonFileStore`) and AndroidX DataStore for user preferences.

3. **Isolated WebView Client Delegations**:
   - `OverflowWebViewClient`: Manages URL overrides, SSL certificate validation, and page load lifecycle events.
   - `OverflowChromeClient`: Coordinates with `ChromeDelegate` to handle file chooser dialogs, custom full-screen video containers, permission prompts, and JavaScript alerts without leaking Activity contexts into background workers.

4. **Privacy Isolation**:
   - Incognito tabs use separate session memory; their history entries and cached sessions are discarded upon closing the incognito tab group.
