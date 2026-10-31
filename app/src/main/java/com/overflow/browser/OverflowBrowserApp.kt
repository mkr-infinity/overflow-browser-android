package com.overflow.browser

import android.app.Application
import android.webkit.WebView

/**
 * Application entry point for Overflow Browser.
 *
 * The app deliberately has no analytics, accounts, or background services. The
 * only long-lived state is the WebView engine and the local browser data files.
 */
class OverflowBrowserApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }
}
