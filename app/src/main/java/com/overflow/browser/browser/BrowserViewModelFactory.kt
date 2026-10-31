package com.overflow.browser.browser

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.overflow.browser.OverflowBrowserApp

class BrowserViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = application as OverflowBrowserApp
        return BrowserViewModel(application, app.container) as T
    }
}
