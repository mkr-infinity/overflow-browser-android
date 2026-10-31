package com.overflow.browser.browser

import android.webkit.WebChromeClient

enum class JsDialogType {
    ALERT,
    CONFIRM,
    PROMPT
}

data class JsDialogState(
    val type: JsDialogType,
    val message: String,
    val defaultValue: String = "",
    val result: WebChromeClient.JsResult,
    val promptResult: WebChromeClient.JsPromptResult? = null
)
