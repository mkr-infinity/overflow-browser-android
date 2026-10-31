# Keep the JavaScript bridge used by the WebView.
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.overflow.browser.browser.JsBridge { *; }

# Keep annotations used by AndroidX.
-keepattributes *Annotation*
