# Overflow Browser

A lightweight, privacy-focused Android browser built with Jetpack Compose and the Android system WebView.

## Features
- Fast homepage with search and shortcuts
- Multiple tabs with persistent normal tabs and private tabs
- Ad blocker and tracker protection using request interception
- Bookmarks, browsing history, and quick access
- Download manager integration using Android DownloadManager
- Light, dark, and system theme support
- Responsive layout for phones and tablets

## Android compatibility
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34

## Project structure
- app/src/main/java/com/overflow/browser: Application code
- app/src/main/res: UI resources and manifests

## Build notes
This project uses Gradle Kotlin DSL and AndroidX/Jetpack Compose.

## Privacy and limitations
The built-in ad blocker works through WebView request interception for standard network requests. Some ads or trackers delivered through script execution or browser features cannot always be blocked completely. That is a technical limitation of the Android WebView platform.

## License
Apache License 2.0
