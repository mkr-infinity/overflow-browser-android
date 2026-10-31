# Changelog

All notable changes to Overflow Browser will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-10-31

### Added
- **Browser Engine**:
  - Multi-tab management with persistent and incognito modes.
  - Native WebView client integration with SSL error handling and security warnings.
  - Custom Chrome client with file chooser, full-screen video, and WebRTC permission delegation.
  - Distraction-free Reader Mode with automated readability extraction.
  - Built-in host-based ad and tracker blocker with cosmetic CSS filtering.
  - Multiple search engine providers (DuckDuckGo, Google, Bing, Brave, Ecosia, Startpage, Custom).
  - Search suggestion query engine with debounced network fetches.
- **Data Persistence**:
  - Fast, thread-safe JSON file stores for bookmarks, browsing history, downloads, and tab sessions.
  - Jetpack DataStore preferences for persistent user settings.
  - Privacy statistics repository tracking blocked ads and saved data.
- **User Interface**:
  - Modern Jetpack Compose UI adhering to Material Design 3 guidelines.
  - Responsive home start page with customizable quick links and recent items.
  - Adaptive top/bottom toolbar with omnibox, loading progress indicator, and action sheet menu.
  - Full-screen tab switcher grid with thumbnail previews and close gestures.
  - Dedicated screens for Bookmarks, History, Downloads, Settings, and About information.
- **Platform & System**:
  - Edge-to-edge system bar integration with dynamic theming (Light/Dark/System).
  - Intent filters for standard browser protocols (`http`, `https`), `WEB_SEARCH`, and text processing.
  - Manual GitHub Actions workflow (`workflow_dispatch`) for artifact builds.
