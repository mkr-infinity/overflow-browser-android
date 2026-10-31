# Overflow Browser

**Fast. Private. Yours.** A lightweight, feature-rich Android browser built on the Android system WebView and Jetpack Compose.

> Overflow Browser is an independent open-source project. It is not affiliated with, endorsed by, or connected to Via Browser or any other browser vendor.

## Why Overflow?

- **Lightweight by design** — no bundled Chromium, no accounts, no telemetry, no background sync services.
- **Fast** — the system WebView is shared with Android, so updates arrive through the system and the APK stays small.
- **Private** — request-level ad blocking, private tabs, no history for private browsing, and local-only data.
- **Beautiful** — a calm Material 3 interface with a teal/coral palette, edge-to-edge layout, and a focused bottom toolbar.
- **Made for every screen** — responsive phone, tablet, foldable, and landscape layouts.
- **Open source** — Apache-2.0 licensed. Fork it, modify it, ship it, and credit the original author.

## Planned feature set

- Multiple tabs, tab switcher, session restore, and private tabs
- Smart address bar with search suggestions and voice search
- Bookmarks, history, downloads, and quick access
- Ad/tracker blocking with cosmetic filtering and a live counter
- Reader mode, find in page, desktop site, and share
- Dark/light/system/AMOLED themes, dynamic color, and dark-web-page support
- Configurable search engines, homepage, JavaScript, images, cookies, HTTPS-only mode
- Tablet and foldable responsive layouts
- Manual GitHub Actions APK builds (no automatic workflow runs)

## Requirements

- Android 7.0 (API 24) or newer
- Android Studio Ladybug or newer, JDK 17
- Android SDK 35

## Build

```bash
./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/`.

For a release build:

```bash
./gradlew :app:assembleRelease
```

Release builds are minified and resource-shrunk. Sign the resulting APK with your own keystore before distribution.

## Project status

The project is under active development. The initial PR series builds the foundation, browser engine, UI, privacy features, tools, settings, responsive polish, tests, documentation, and a manual-only CI workflow.

## Credits

Created and maintained by **Mohammad Kaif Raja** ([@mkr-infinity](https://github.com/mkr-infinity)).

If you use Overflow Browser or build on it, please keep the copyright notice and credit the original author. See [NOTICE](NOTICE).

## License

Licensed under the [Apache License 2.0](LICENSE).

<!-- badge pipeline test -->
