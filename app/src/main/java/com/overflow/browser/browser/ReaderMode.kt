package com.overflow.browser.browser

import org.json.JSONObject

data class ReaderContent(
    val title: String,
    val byline: String,
    val text: String,
    val url: String
)

object ReaderMode {

    fun parse(raw: String?): ReaderContent? {
        if (raw.isNullOrBlank() || raw == "null") return null
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return null
        val text = json.optString("text").trim()
        if (text.length < 80) return null
        return ReaderContent(
            title = json.optString("title").ifBlank { "Reader" },
            byline = json.optString("byline"),
            text = text,
            url = json.optString("url")
        )
    }

    /**
     * ES5-only extraction so it also works on older Android WebView builds.
     * Returns a plain object; evaluateJavascript serializes it as JSON.
     */
    val extractionScript: String = """
        (function() {
          function textOf(el) {
            return ((el && (el.innerText || el.textContent)) || '').trim();
          }

          var candidates = [
            document.querySelector('article'),
            document.querySelector('main'),
            document.querySelector('[role=main]')
          ];
          var best = null;
          var bestLen = 0;
          for (var i = 0; i < candidates.length; i++) {
            var candidate = candidates[i];
            if (!candidate) continue;
            var length = textOf(candidate).length;
            if (length > bestLen) {
              best = candidate;
              bestLen = length;
            }
          }

          if (!best || bestLen < 400) {
            var blocks = document.querySelectorAll('div,section');
            for (var j = 0; j < blocks.length; j++) {
              var blockLength = textOf(blocks[j]).length;
              if (blockLength > bestLen) {
                best = blocks[j];
                bestLen = blockLength;
              }
            }
          }

          if (!best) return null;

          var clone = best.cloneNode(true);
          var junk = clone.querySelectorAll(
            'script,style,noscript,nav,footer,header,aside,form,button,iframe,svg,' +
            '[role=navigation],[aria-hidden=true]'
          );
          for (var k = junk.length - 1; k >= 0; k--) {
            if (junk[k].parentNode) junk[k].parentNode.removeChild(junk[k]);
          }

          var text = textOf(clone).replace(/\n{3,}/g, '\n\n').trim();
          var ogTitle = document.querySelector('meta[property="og:title"]');
          var heading = document.querySelector('h1');
          var author = document.querySelector('meta[name="author"]');
          var title = ogTitle ? ogTitle.getAttribute('content') : (heading ? textOf(heading) : document.title);
          var byline = author ? author.getAttribute('content') : '';

          return {
            title: (title || '').trim(),
            byline: (byline || '').trim(),
            text: text,
            url: location.href
          };
        })()
    """.trimIndent()
}
