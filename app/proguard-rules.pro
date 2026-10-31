# Keep the JavaScript bridge used by the WebView.
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.overflow.browser.browser.JsBridge { *; }

# Keep annotations used by AndroidX and Kotlin.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep data models persisted to JSON files so field names are not obfuscated during minification.
-keep class com.overflow.browser.data.** { *; }

# Jetpack Compose runtime and animation reflection rules.
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView {
    boolean onTouchEvent(android.view.MotionEvent);
}

# Kotlin Coroutines debug and internal exceptions.
-dontwarn kotlinx.coroutines.**
