package com.example.engine

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class SniffedVideoResult(
    val videoUrl: String,
    val title: String
)

object WebViewVideoSniffer {

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun sniffVideoStream(
        context: Context,
        targetUrl: String,
        timeoutMs: Long = 11_000L
    ): SniffedVideoResult? = withContext(Dispatchers.Main) {
        var webView: WebView? = null
        try {
            withTimeoutOrNull(timeoutMs) {
                suspendCancellableCoroutine { cont ->
                    var completed = false
                    fun finishWith(res: SniffedVideoResult?) {
                        if (!completed && cont.isActive) {
                            completed = true
                            cont.resume(res)
                        }
                    }

                    val wv = WebView(context.applicationContext)
                    webView = wv

                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true)

                    wv.settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString =
                            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
                    }

                    val effectiveUrl = if (targetUrl.contains("youtube.com") || targetUrl.contains("youtu.be")) {
                        val vid = extractYouTubeVideoId(targetUrl)
                        if (vid != null) "https://m.youtube.com/watch?v=$vid" else targetUrl
                    } else {
                        targetUrl
                    }

                    wv.webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val reqUrl = request?.url?.toString() ?: return null
                            if (isLikelyDirectVideoStream(reqUrl)) {
                                val cleanStreamUrl = if ("googlevideo.com/videoplayback" in reqUrl) {
                                    reqUrl.replace(Regex("&range=[0-9]+-[0-9]+"), "")
                                        .replace(Regex("\\?range=[0-9]+-[0-9]+&"), "?")
                                } else {
                                    reqUrl
                                }
                                finishWith(
                                    SniffedVideoResult(
                                        videoUrl = cleanStreamUrl,
                                        title = view?.title ?: "Social Video"
                                    )
                                )
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            val js = """
                                (function() {
                                    try {
                                        var v = document.querySelector('video');
                                        if (v) {
                                            v.muted = true;
                                            v.play();
                                            if (v.src && v.src.indexOf('http') === 0) return v.src;
                                        }
                                        var p = window.ytInitialPlayerResponse;
                                        if (p && p.streamingData) {
                                            var fmts = p.streamingData.formats || [];
                                            for (var i = 0; i < fmts.length; i++) {
                                                if (fmts[i].url) return fmts[i].url;
                                            }
                                        }
                                    } catch(e) {}
                                    var s = document.querySelector('video source');
                                    if (s && s.src && s.src.indexOf('http') === 0) return s.src;
                                    var og = document.querySelector('meta[property="og:video"], meta[property="og:video:secure_url"]');
                                    if (og && og.content && og.content.indexOf('http') === 0) return og.content;
                                    var html = document.documentElement.innerHTML;
                                    var m1 = html.match(/"(?:browser_native_hd_url|playable_url_quality_hd|playable_url|video_url|playAddr)"\s*:\s*"(https?:[^"]+)"/);
                                    if (m1 && m1[1]) return m1[1];
                                    return "";
                                })();
                            """.trimIndent()

                            view?.evaluateJavascript(js) { rawResult ->
                                val cleaned = rawResult
                                    ?.trim()
                                    ?.removeSurrounding("\"")
                                    ?.replace("\\/", "/")
                                    ?.replace("\\u0025", "%")
                                    ?.replace("\\u0026", "&")
                                    ?.replace("&amp;", "&")
                                    ?: ""
                                if (cleaned.startsWith("http") && !cleaned.endsWith(".html")) {
                                    val cleanStreamUrl = if ("googlevideo.com/videoplayback" in cleaned) {
                                        cleaned.replace(Regex("&range=[0-9]+-[0-9]+"), "")
                                            .replace(Regex("\\?range=[0-9]+-[0-9]+&"), "?")
                                    } else {
                                        cleaned
                                    }
                                    finishWith(
                                        SniffedVideoResult(
                                            videoUrl = cleanStreamUrl,
                                            title = view.title ?: "Social Video"
                                        )
                                    )
                                }
                            }
                        }
                    }

                    wv.loadUrl(effectiveUrl)

                    cont.invokeOnCancellation {
                        try {
                            wv.stopLoading()
                            wv.destroy()
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        } catch (_: Exception) {
            null
        } finally {
            try {
                webView?.stopLoading()
                webView?.destroy()
            } catch (_: Exception) {
            }
        }
    }

    private fun isLikelyDirectVideoStream(url: String): Boolean {
        val lower = url.lowercase()
        if (lower.endsWith(".js") || lower.endsWith(".css") || lower.endsWith(".png") ||
            lower.endsWith(".jpg") || lower.endsWith(".webp") || lower.endsWith(".svg") ||
            lower.endsWith(".m4s") || lower.contains("/generate_204")
        ) {
            return false
        }
        if ("googlevideo.com/videoplayback" in lower) {
            return true
        }
        return (".mp4" in lower || "video/mp4" in lower || "video%2fmp4" in lower || "mime=video_mp4" in lower) &&
            ("fbcdn.net" in lower || "cdninstagram.com" in lower || "tiktokcdn" in lower ||
                "tikwm.com/video/media" in lower || "twimg.com" in lower ||
                "googlevideo.com" in lower || "pinimg.com/videos" in lower)
    }

    private fun extractYouTubeVideoId(url: String): String? {
        val patterns = listOf(
            "youtube\\.com/shorts/([A-Za-z0-9_-]{11})",
            "youtu\\.be/([A-Za-z0-9_-]{11})",
            "[?&]v=([A-Za-z0-9_-]{11})",
            "youtube\\.com/embed/([A-Za-z0-9_-]{11})"
        )
        for (p in patterns) {
            val m = Regex(p).find(url)
            if (m != null) return m.groupValues[1]
        }
        return null
    }
}
