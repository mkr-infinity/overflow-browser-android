package com.overflow.browser

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.overflow.browser.browser.BrowserViewModel
import com.overflow.browser.browser.BrowserViewModelFactory
import com.overflow.browser.browser.ChromeDelegate
import com.overflow.browser.data.BrowserSettings
import com.overflow.browser.ui.browser.BrowserScreen
import com.overflow.browser.ui.theme.OverflowBrowserTheme

/**
 * Single-activity host for the Compose browser UI.
 *
 * The Activity owns only platform-facing concerns: the file chooser, runtime
 * permissions, fullscreen video, system bars, lifecycle forwarding, and
 * incoming VIEW / WEB_SEARCH / SEND intents. Browser state lives in
 * [BrowserViewModel] so rotation and process recreation keep the session.
 */
class MainActivity : ComponentActivity(), ChromeDelegate {

    private val app: OverflowBrowserApp
        get() = application as OverflowBrowserApp

    private val viewModel: BrowserViewModel by viewModels {
        BrowserViewModelFactory(application)
    }

    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private var pendingPermissionRequest: PermissionRequest? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = fileChooserCallback
        fileChooserCallback = null
        if (callback == null) return@registerForActivityResult
        if (result.resultCode == RESULT_OK) {
            callback.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            )
        } else {
            callback.onReceiveValue(null)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val request = pendingPermissionRequest
        pendingPermissionRequest = null
        if (request == null) return@registerForActivityResult
        val granted = request.resources.all { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                    grants[Manifest.permission.CAMERA] == true
                PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                    grants[Manifest.permission.RECORD_AUDIO] == true
                else -> true
            }
        }
        if (granted) request.grant(request.resources) else request.deny()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.chromeDelegate = this

        setContent {
            val settings by app.container.settingsRepository.settings
                .collectAsState(initial = BrowserSettings())
            OverflowBrowserTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                BrowserScreen(viewModel = viewModel)
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (customView != null) {
                        onHideCustomView()
                    } else if (!viewModel.handleBack()) {
                        finish()
                    }
                }
            }
        )

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        if (customView != null) onHideCustomView()
        viewModel.chromeDelegate = null
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = null
        pendingPermissionRequest?.deny()
        pendingPermissionRequest = null
        if (isFinishing) viewModel.onFinish()
        super.onDestroy()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.toString()
                    ?.takeIf { it.isNotBlank() }
                    ?.let(viewModel::openUrlFromIntent)
            }

            Intent.ACTION_WEB_SEARCH -> {
                intent.getStringExtra(SearchManager.QUERY)
                    ?.takeIf { it.isNotBlank() }
                    ?.let(viewModel::openUrlFromIntent)
            }

            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)
                    ?.takeIf { it.isNotBlank() }
                    ?.let(viewModel::openUrlFromIntent)
            }

            Intent.ACTION_PROCESS_TEXT -> {
                intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
                    ?.toString()
                    ?.takeIf { it.isNotBlank() }
                    ?.let(viewModel::openUrlFromIntent)
            }
        }
    }

    override fun onShowFileChooser(
        webView: WebView,
        callback: ValueCallback<Array<Uri>>,
        params: WebChromeClient.FileChooserParams
    ): Boolean {
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = callback
        return try {
            fileChooserLauncher.launch(params.createIntent())
            true
        } catch (error: Exception) {
            fileChooserCallback = null
            false
        }
    }

    override fun onShowCustomView(
        view: View,
        callback: WebChromeClient.CustomViewCallback
    ) {
        if (customView != null) onHideCustomView()
        val root = findViewById<FrameLayout>(android.R.id.content) ?: return
        customView = view
        customViewCallback = callback
        root.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        viewModel.setFullscreen(true)
    }

    override fun onHideCustomView() {
        val view = customView ?: return
        val callback = customViewCallback
        customView = null
        customViewCallback = null
        (view.parent as? FrameLayout)?.removeView(view)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.getInsetsController(window, window.decorView)
            .show(WindowInsetsCompat.Type.systemBars())
        viewModel.setFullscreen(false)
        callback?.onCustomViewHidden()
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        val permissions = request.resources.mapNotNull { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
                else -> null
            }
        }.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissions.isEmpty()) {
            request.grant(request.resources)
            return
        }

        pendingPermissionRequest?.deny()
        pendingPermissionRequest = request
        permissionLauncher.launch(permissions.toTypedArray())
    }
}
