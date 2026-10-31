package com.overflow.browser.browser

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView

class OverflowChromeClient(
    private val tabId: String,
    private val onProgress: (String, Int) -> Unit,
    private val onTitle: (String, String) -> Unit,
    private val onFavicon: (String, Bitmap?) -> Unit,
    private val onJsDialog: (JsDialogState) -> Unit,
    private val onFileChooser: (
        WebView,
        ValueCallback<Array<Uri>>,
        WebChromeClient.FileChooserParams
    ) -> Boolean,
    private val onPermissionRequest: (PermissionRequest) -> Unit,
    private val onShowCustomView: (View, WebChromeClient.CustomViewCallback) -> Unit,
    private val onHideCustomView: () -> Unit
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        onProgress(tabId, newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        if (!title.isNullOrBlank()) onTitle(tabId, title)
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        onFavicon(tabId, icon)
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        if (webView == null || filePathCallback == null || fileChooserParams == null) return false
        return onFileChooser(webView, filePathCallback, fileChooserParams)
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        if (message == null || result == null) return false
        onJsDialog(JsDialogState(JsDialogType.ALERT, message, "", result))
        return true
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        if (message == null || result == null) return false
        onJsDialog(JsDialogState(JsDialogType.CONFIRM, message, "", result))
        return true
    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        if (message == null || result == null) return false
        onJsDialog(
            JsDialogState(
                type = JsDialogType.PROMPT,
                message = message,
                defaultValue = defaultValue ?: "",
                result = result,
                promptResult = result
            )
        )
        return true
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        if (request != null) onPermissionRequest(request)
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        callback?.invoke(origin ?: "", false, false)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (view != null && callback != null) onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        onHideCustomView()
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        if (consoleMessage != null) {
            Log.d(
                "OverflowWebView",
                "${consoleMessage.message()} (${consoleMessage.lineNumber()})"
            )
        }
        return true
    }
}
