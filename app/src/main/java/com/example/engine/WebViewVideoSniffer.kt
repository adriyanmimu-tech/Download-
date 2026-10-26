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

                    wv.webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val reqUrl = request?.url?.toString() ?: return null
                            if (isLikelyDirectVideoStream(reqUrl)) {
                                finishWith(
                                    SniffedVideoResult(
                                        videoUrl = reqUrl,
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
                                    var v = document.querySelector('video');
                                    if (v && v.src && v.src.indexOf('http') === 0) return v.src;
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
                                    finishWith(
                                        SniffedVideoResult(
                                            videoUrl = cleaned,
                                            title = view.title ?: "Social Video"
                                        )
                                    )
                                }
                            }
                        }
                    }

                    wv.loadUrl(targetUrl)

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
            lower.endsWith(".jpg") || lower.endsWith(".webp") || lower.endsWith(".svg")
        ) {
            return false
        }
        return (".mp4" in lower || "video/mp4" in lower || "mime=video_mp4" in lower) &&
            ("fbcdn.net" in lower || "cdninstagram.com" in lower || "tiktokcdn" in lower ||
                "tikwm.com/video/media" in lower || "twimg.com" in lower ||
                "googlevideo.com" in lower || "pinimg.com/videos" in lower)
    }
}
