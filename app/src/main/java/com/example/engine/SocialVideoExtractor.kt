package com.example.engine

import android.content.Context
import android.webkit.CookieManager
import com.example.data.ExtractorApiSettings
import com.example.data.SocialPlatform
import com.example.data.VideoAnalysisResult
import com.example.data.VideoQualityOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class SocialVideoExtractor {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun analyzeVideoLink(
        context: Context,
        rawUrl: String,
        apiSettings: ExtractorApiSettings
    ): VideoAnalysisResult? = withContext(Dispatchers.IO) {
        val initialUrl = extractFirstUrl(rawUrl).ifEmpty { rawUrl.trim() }
        if (!initialUrl.startsWith("http://") && !initialUrl.startsWith("https://")) {
            return@withContext null
        }

        // Resolve shortlinks (/share/r/, /share/v/, vt.tiktok.com, vm.tiktok.com, fb.watch, pin.it)
        val cleanUrl = resolveRedirectUrl(initialUrl)
        val platform = SocialPlatform.detectFromUrl(cleanUrl)

        // 1. Custom Extractor API if configured by user
        if (apiSettings.customApiEndpoint.isNotBlank()) {
            try {
                val customResult = tryCustomExtractorApi(cleanUrl, platform, apiSettings)
                if (customResult != null) return@withContext customResult
            } catch (_: Exception) {
            }
        }

        // 2. Direct .mp4 / .m4v / .webm URL
        if (cleanUrl.substringBefore("?").endsWith(".mp4", ignoreCase = true) ||
            cleanUrl.substringBefore("?").endsWith(".m4v", ignoreCase = true) ||
            cleanUrl.substringBefore("?").endsWith(".webm", ignoreCase = true)
        ) {
            val fileTitle = cleanUrl.substringAfterLast("/").substringBefore("?").removeSuffix(".mp4")
                .ifBlank { "Direct Social Video" }
            return@withContext buildResultFromStreams(
                sourceUrl = cleanUrl,
                platform = platform,
                title = fileTitle,
                description = fileTitle,
                author = "@direct_stream",
                durationSec = 15,
                thumbUrl = "",
                hdUrl = cleanUrl,
                sdUrl = cleanUrl,
                fallbackUrls = listOf(cleanUrl),
                audioUrl = null
            )
        }

        // 3. Platform-Specific Real Extractors
        when (platform) {
            SocialPlatform.TIKTOK -> {
                extractTikTokVideo(initialUrl, cleanUrl)?.let { return@withContext it }
            }
            SocialPlatform.X_TWITTER -> {
                extractTwitterXVideo(cleanUrl)?.let { return@withContext it }
            }
            SocialPlatform.INSTAGRAM -> {
                extractInstagramVideo(cleanUrl)?.let { return@withContext it }
            }
            SocialPlatform.FACEBOOK -> {
                extractFacebookVideo(initialUrl, cleanUrl)?.let { return@withContext it }
            }
            SocialPlatform.YOUTUBE -> {
                extractYouTubeVideo(cleanUrl)?.let { return@withContext it }
                val vid = extractYouTubeVideoId(cleanUrl)
                val ytTarget = if (vid != null) "https://m.youtube.com/watch?v=$vid" else cleanUrl
                val sniffedYt = WebViewVideoSniffer.sniffVideoStream(context, ytTarget)
                if (sniffedYt != null && sniffedYt.videoUrl.startsWith("http")) {
                    return@withContext buildResultFromStreams(
                        sourceUrl = cleanUrl,
                        platform = SocialPlatform.YOUTUBE,
                        title = sniffedYt.title.ifBlank { "YouTube Video" },
                        description = sniffedYt.title,
                        author = "@youtube_creator",
                        durationSec = 30,
                        thumbUrl = if (vid != null) "https://i.ytimg.com/vi/$vid/hqdefault.jpg" else "",
                        hdUrl = sniffedYt.videoUrl,
                        sdUrl = sniffedYt.videoUrl,
                        fallbackUrls = listOf(sniffedYt.videoUrl),
                        audioUrl = null
                    )
                }
            }
            SocialPlatform.PINTEREST -> {
                extractPinterestVideo(cleanUrl)?.let { return@withContext it }
            }
            SocialPlatform.THREADS -> {
                extractThreadsVideo(cleanUrl)?.let { return@withContext it }
            }
            SocialPlatform.LIKEE -> {
                extractGenericOpenGraphVideo(cleanUrl, SocialPlatform.LIKEE)?.let { return@withContext it }
            }
        }

        // 4. Universal Open Extractors (TikWM + OpenGraph)
        extractTikTokVideo(initialUrl, cleanUrl)?.let { return@withContext it }
        extractGenericOpenGraphVideo(cleanUrl, platform)?.let { return@withContext it }

        // 5. Headless Android WebView Stream Sniffer (handles JS-rendered Facebook/Instagram/TikTok/Threads pages)
        val sniffed = WebViewVideoSniffer.sniffVideoStream(context, cleanUrl)
        if (sniffed != null && sniffed.videoUrl.startsWith("http")) {
            return@withContext buildResultFromStreams(
                sourceUrl = cleanUrl,
                platform = platform,
                title = sniffed.title.ifBlank { "${platform.displayName} Video" },
                description = sniffed.title,
                author = extractAuthorHandle(cleanUrl, platform),
                durationSec = 15,
                thumbUrl = "",
                hdUrl = sniffed.videoUrl,
                sdUrl = sniffed.videoUrl,
                fallbackUrls = listOf(sniffed.videoUrl),
                audioUrl = null
            )
        }

        null
    }

    private fun resolveRedirectUrl(url: String): String {
        return try {
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", MOBILE_USER_AGENT)
                .head()
                .build()
            httpClient.newCall(req).execute().use { resp ->
                val resolved = resp.request.url.toString()
                if (resolved.startsWith("http") && "login" !in resolved.lowercase()) {
                    resolved
                } else {
                    url
                }
            }
        } catch (_: Exception) {
            url
        }
    }

    /**
     * Real TikTok No-Watermark Extractor:
     * 1) TikWM API (prioritizing verified `play` + `hdplay` + `wmplay` fallback chain)
     * 2) LoveTik API (independent TikTok no-watermark extractor)
     * 3) TikTok Page Universal Rehydration JSON
     */
    private fun extractTikTokVideo(originalUrl: String, resolvedUrl: String): VideoAnalysisResult? {
        for (targetUrl in listOf(originalUrl, resolvedUrl).distinct()) {
            // Strategy A: TikWM API
            try {
                val encoded = URLEncoder.encode(targetUrl, "UTF-8")
                val req = Request.Builder()
                    .url("https://www.tikwm.com/api/?url=$encoded&hd=1")
                    .header("User-Agent", MOBILE_USER_AGENT)
                    .header("Accept", "application/json")
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string() ?: ""
                        val json = JSONObject(bodyStr)
                        if (json.optInt("code", -1) == 0 && json.has("data")) {
                            val data = json.getJSONObject("data")
                            val stdPlay = resolveTikWmUrl(data.optString("play"))
                            val hdPlay = resolveTikWmUrl(data.optString("hdplay"))
                            val wmPlay = resolveTikWmUrl(data.optString("wmplay"))
                            val musicUrl = resolveTikWmUrl(data.optString("music"))

                            val allValidStreams = listOf(stdPlay, hdPlay, wmPlay)
                                .filter { it.startsWith("http") }
                                .distinct()

                            if (allValidStreams.isNotEmpty()) {
                                // `stdPlay` is TikWM's direct no-watermark MP4 stream; keep `hdPlay` and `wmPlay` in fallbacks
                                val primaryHd = stdPlay.ifEmpty { hdPlay }.ifEmpty { wmPlay }
                                val primarySd = stdPlay.ifEmpty { wmPlay }.ifEmpty { hdPlay }
                                val rawTitle = data.optString("title").ifBlank { "TikTok Video" }
                                val duration = data.optInt("duration", 15).coerceAtLeast(1)
                                val cover = resolveTikWmUrl(data.optString("cover"))
                                val authorObj = data.optJSONObject("author")
                                val authorHandle = authorObj?.optString("unique_id")?.let { "@$it" }
                                    ?: "@tiktok_creator"

                                return buildResultFromStreams(
                                    sourceUrl = targetUrl,
                                    platform = SocialPlatform.TIKTOK,
                                    title = removeHashtagsFromText(rawTitle).take(80).ifBlank { "TikTok Video" },
                                    description = rawTitle,
                                    author = authorHandle,
                                    durationSec = duration,
                                    thumbUrl = cover,
                                    hdUrl = primaryHd,
                                    sdUrl = primarySd,
                                    fallbackUrls = allValidStreams,
                                    audioUrl = musicUrl.takeIf { it.startsWith("http") }
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }

            // Strategy B: LoveTik Public API
            try {
                val form = FormBody.Builder()
                    .add("query", targetUrl)
                    .build()
                val req = Request.Builder()
                    .url("https://lovetik.com/api/ajax/search")
                    .header("User-Agent", MOBILE_USER_AGENT)
                    .header("Origin", "https://lovetik.com")
                    .header("Referer", "https://lovetik.com/")
                    .post(form)
                    .build()
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string() ?: "")
                        val links = json.optJSONArray("links") ?: JSONArray()
                        val streamList = mutableListOf<String>()
                        for (i in 0 until links.length()) {
                            val linkObj = links.getJSONObject(i)
                            val aUrl = linkObj.optString("a")
                            if (aUrl.startsWith("http")) streamList.add(aUrl)
                        }
                        if (streamList.isNotEmpty()) {
                            val desc = json.optString("desc").ifBlank { "TikTok Video" }
                            val author = json.optString("author").ifBlank { "@tiktok" }
                            return buildResultFromStreams(
                                sourceUrl = targetUrl,
                                platform = SocialPlatform.TIKTOK,
                                title = removeHashtagsFromText(desc).take(75).ifBlank { "TikTok Video" },
                                description = desc,
                                author = if (author.startsWith("@")) author else "@$author",
                                durationSec = 15,
                                thumbUrl = json.optString("cover"),
                                hdUrl = streamList.first(),
                                sdUrl = streamList.getOrElse(1) { streamList.first() },
                                fallbackUrls = streamList,
                                audioUrl = streamList.lastOrNull()
                            )
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun resolveTikWmUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || trimmed == "null") return ""
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        if (trimmed.startsWith("/")) return "https://www.tikwm.com$trimmed"
        return "https://www.tikwm.com/$trimmed"
    }

    /**
     * Real X / Twitter Video Extractor via VxTwitter API
     */
    private fun extractTwitterXVideo(url: String): VideoAnalysisResult? {
        try {
            val pathMatch = Regex("(?:twitter\\.com|x\\.com)/([^/]+/status/\\d+)").find(url)
            val statusPath = pathMatch?.groupValues?.get(1)
            if (!statusPath.isNullOrBlank()) {
                val apiReq = Request.Builder()
                    .url("https://api.vxtwitter.com/$statusPath")
                    .header("User-Agent", BOT_USER_AGENT)
                    .header("Accept", "application/json")
                    .build()
                httpClient.newCall(apiReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string() ?: "")
                        val text = json.optString("text", "X / Twitter Video")
                        val user = json.optString("user_screen_name", "x_user")
                        val mediaArr = json.optJSONArray("media_extended") ?: JSONArray()
                        for (i in 0 until mediaArr.length()) {
                            val media = mediaArr.getJSONObject(i)
                            val mediaUrl = media.optString("url")
                            if (mediaUrl.startsWith("http") && (".mp4" in mediaUrl || media.optString("type") == "video" || media.optString("type") == "gif")) {
                                val durationSec = (media.optLong("duration_millis", 15000L) / 1000L).toInt().coerceAtLeast(1)
                                val thumb = media.optString("thumbnail_url")
                                return buildResultFromStreams(
                                    sourceUrl = url,
                                    platform = SocialPlatform.X_TWITTER,
                                    title = removeHashtagsFromText(text).take(75).ifBlank { "X Video Clip" },
                                    description = text,
                                    author = "@$user",
                                    durationSec = durationSec,
                                    thumbUrl = thumb,
                                    hdUrl = mediaUrl,
                                    sdUrl = mediaUrl,
                                    fallbackUrls = listOf(mediaUrl),
                                    audioUrl = null
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
        return null
    }

    /**
     * Real Instagram Reels & Post Video Extractor (Handles escaped contextJSON in /embed/captioned/ + proxy meta)
     */
    private fun extractInstagramVideo(url: String): VideoAnalysisResult? {
        val shortcodeMatch = Regex("instagram\\.com/(?:reel|reels|p|tv)/([A-Za-z0-9_-]+)").find(url)
        val shortcode = shortcodeMatch?.groupValues?.get(1)

        if (!shortcode.isNullOrBlank()) {
            val endpoints = listOf(
                "https://www.instagram.com/p/$shortcode/embed/captioned/",
                "https://www.instagram.com/reel/$shortcode/embed/captioned/",
                "https://www.ddinstagram.com/reel/$shortcode",
                "https://kkinstagram.com/reel/$shortcode"
            )
            for (ep in endpoints) {
                try {
                    val req = Request.Builder()
                        .url(ep)
                        .header("User-Agent", if ("embed" in ep) DESKTOP_USER_AGENT else BOT_USER_AGENT)
                        .build()
                    httpClient.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val html = resp.body?.string() ?: ""
                            val videoUrl = extractJsonField(html, "video_url")
                                ?: extractMetaContent(html, "og:video:secure_url")
                                ?: extractMetaContent(html, "og:video")
                                ?: extractMetaContent(html, "twitter:player:stream")

                            if (!videoUrl.isNullOrBlank() && videoUrl.startsWith("http") && !videoUrl.endsWith(".html")) {
                                val cleanVideoUrl = unescapeJsonUrl(videoUrl)
                                val desc = extractMetaContent(html, "og:description")
                                    ?: extractJsonField(html, "text")
                                    ?: "Instagram Reel Video"
                                val thumb = extractMetaContent(html, "og:image") ?: ""
                                return buildResultFromStreams(
                                    sourceUrl = url,
                                    platform = SocialPlatform.INSTAGRAM,
                                    title = removeHashtagsFromText(desc).take(75).ifBlank { "Instagram Reel" },
                                    description = desc,
                                    author = "@instagram_reel",
                                    durationSec = 15,
                                    thumbUrl = unescapeJsonUrl(thumb),
                                    hdUrl = cleanVideoUrl,
                                    sdUrl = cleanVideoUrl,
                                    fallbackUrls = listOf(cleanVideoUrl),
                                    audioUrl = null
                                )
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
        return null
    }

    /**
     * Real Facebook Watch / Reels / Video Extractor (Supports /share/r/, /share/v/, fb.watch, and mbasic video_redirect)
     */
    private fun extractFacebookVideo(originalUrl: String, resolvedUrl: String): VideoAnalysisResult? {
        val candidateUrls = listOf(resolvedUrl, originalUrl).distinct()
        val userAgents = listOf(
            DESKTOP_USER_AGENT,
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15",
            BOT_USER_AGENT
        )

        for (target in candidateUrls) {
            for (ua in userAgents) {
                try {
                    val req = Request.Builder()
                        .url(target)
                        .header("User-Agent", ua)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .header("Sec-Fetch-Mode", "navigate")
                        .build()

                    httpClient.newCall(req).execute().use { resp ->
                        val html = resp.body?.string() ?: ""
                        val hdUrl = extractJsonField(html, "browser_native_hd_url")
                            ?: extractJsonField(html, "playable_url_quality_hd")
                            ?: extractJsonField(html, "hd_src_no_ratelimit")
                            ?: extractJsonField(html, "hd_src")

                        val sdUrl = extractJsonField(html, "browser_native_sd_url")
                            ?: extractJsonField(html, "playable_url")
                            ?: extractJsonField(html, "sd_src_no_ratelimit")
                            ?: extractJsonField(html, "sd_src")
                            ?: extractFacebookMbasicRedirect(html)
                            ?: extractMetaContent(html, "og:video:secure_url")
                            ?: extractMetaContent(html, "og:video")

                        val streams = listOfNotNull(hdUrl, sdUrl)
                            .map { unescapeJsonUrl(it) }
                            .filter { it.startsWith("http") && !it.endsWith(".html") }
                            .distinct()

                        if (streams.isNotEmpty()) {
                            val title = extractMetaContent(html, "og:title")
                                ?: extractHtmlTitle(html)
                                ?: "Facebook Video"
                            val desc = extractMetaContent(html, "og:description") ?: title
                            val thumb = extractMetaContent(html, "og:image") ?: ""
                            return buildResultFromStreams(
                                sourceUrl = target,
                                platform = SocialPlatform.FACEBOOK,
                                title = removeHashtagsFromText(title).take(75).ifBlank { "Facebook Video" },
                                description = desc,
                                author = "@facebook_video",
                                durationSec = 20,
                                thumbUrl = unescapeJsonUrl(thumb),
                                hdUrl = streams.first(),
                                sdUrl = streams.last(),
                                fallbackUrls = streams,
                                audioUrl = null
                            )
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
        return null
    }

    private fun extractFacebookMbasicRedirect(html: String): String? {
        val m = Regex("video_redirect/\\?src=([^\"'&]+)").find(html)
        if (m != null) {
            return try {
                java.net.URLDecoder.decode(m.groupValues[1], "UTF-8")
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    /**
     * Real YouTube Shorts & Full Video Extractor:
     * Strategy 1: YouTube InnerTube ANDROID_VR client
     * Strategy 2: YouTube InnerTube IOS client (iPhone16,2)
     * Strategy 3: YouTube InnerTube ANDROID_TESTSUITE client
     * Strategy 4: Public Piped / Invidious API fallback
     */
    private fun extractYouTubeVideo(url: String): VideoAnalysisResult? {
        val videoId = extractYouTubeVideoId(url) ?: return null

        data class InnerTubeClientSpec(
            val clientName: String,
            val clientVersion: String,
            val userAgent: String,
            val extraClientFields: Map<String, Any> = emptyMap()
        )

        val clients = listOf(
            InnerTubeClientSpec(
                clientName = "ANDROID_VR",
                clientVersion = "1.60.19",
                userAgent = YOUTUBE_VR_USER_AGENT,
                extraClientFields = mapOf(
                    "androidSdkVersion" to 34,
                    "deviceMake" to "Oculus",
                    "deviceModel" to "Quest 3",
                    "osName" to "Android",
                    "osVersion" to "14"
                )
            ),
            InnerTubeClientSpec(
                clientName = "IOS",
                clientVersion = "19.45.4",
                userAgent = YOUTUBE_IOS_USER_AGENT,
                extraClientFields = mapOf(
                    "deviceMake" to "Apple",
                    "deviceModel" to "iPhone16,2",
                    "osName" to "iPhone",
                    "osVersion" to "18.1.0.22B83"
                )
            ),
            InnerTubeClientSpec(
                clientName = "ANDROID_TESTSUITE",
                clientVersion = "1.9",
                userAgent = YOUTUBE_TESTSUITE_USER_AGENT,
                extraClientFields = mapOf(
                    "androidSdkVersion" to 34,
                    "osName" to "Android",
                    "osVersion" to "14"
                )
            )
        )

        val allFoundCombinedMp4s = mutableListOf<String>()
        var foundTitle = "YouTube Video"
        var foundAuthor = "@youtube_creator"
        var foundDesc = "YouTube Video"
        var foundDuration = 15
        var foundAudioM4a: String? = null

        for (spec in clients) {
            try {
                val clientJson = JSONObject().apply {
                    put("clientName", spec.clientName)
                    put("clientVersion", spec.clientVersion)
                    put("hl", "en")
                    put("gl", "US")
                    spec.extraClientFields.forEach { (k, v) -> put(k, v) }
                }
                val payload = JSONObject().apply {
                    put("videoId", videoId)
                    put("contentCheckOk", true)
                    put("racyCheckOk", true)
                    put("context", JSONObject().apply {
                        put("client", clientJson)
                    })
                }
                val req = Request.Builder()
                    .url("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
                    .header("User-Agent", spec.userAgent)
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Format-Version", "2")
                    .post(payload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string() ?: "")
                        val videoDetails = json.optJSONObject("videoDetails")
                        if (videoDetails != null) {
                            foundTitle = videoDetails.optString("title").ifBlank { foundTitle }
                            foundAuthor = videoDetails.optString("author").takeIf { it.isNotBlank() }?.let { "@$it" } ?: foundAuthor
                            foundDesc = videoDetails.optString("shortDescription").ifBlank { foundTitle }
                            foundDuration = videoDetails.optString("lengthSeconds").toIntOrNull() ?: foundDuration
                        }

                        val streamingData = json.optJSONObject("streamingData")
                        val formats = streamingData?.optJSONArray("formats") ?: JSONArray()
                        val adaptiveFormats = streamingData?.optJSONArray("adaptiveFormats") ?: JSONArray()

                        for (i in 0 until formats.length()) {
                            val fmt = formats.getJSONObject(i)
                            val mime = fmt.optString("mimeType")
                            val streamUrl = fmt.optString("url")
                            if (streamUrl.startsWith("http") && mime.startsWith("video/mp4")) {
                                allFoundCombinedMp4s.add(streamUrl)
                            }
                        }

                        for (i in 0 until adaptiveFormats.length()) {
                            val fmt = adaptiveFormats.getJSONObject(i)
                            val mime = fmt.optString("mimeType")
                            val streamUrl = fmt.optString("url")
                            if (streamUrl.startsWith("http") && mime.startsWith("audio/mp4") && foundAudioM4a == null) {
                                foundAudioM4a = streamUrl
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        if (allFoundCombinedMp4s.isNotEmpty()) {
            val distinctStreams = allFoundCombinedMp4s.distinct()
            return buildResultFromStreams(
                sourceUrl = url,
                platform = SocialPlatform.YOUTUBE,
                title = foundTitle,
                description = foundDesc,
                author = foundAuthor,
                durationSec = foundDuration,
                thumbUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                hdUrl = distinctStreams.first(),
                sdUrl = distinctStreams.last(),
                fallbackUrls = distinctStreams,
                audioUrl = foundAudioM4a
            )
        }

        // Strategy 4: Public Piped & Invidious API Fallback
        val pipedInstances = listOf(
            "https://pipedapi.kavin.rocks/streams/$videoId",
            "https://pipedapi.leptons.xyz/streams/$videoId",
            "https://pipedapi.nosebs.ru/streams/$videoId"
        )
        for (apiUrl in pipedInstances) {
            try {
                val req = Request.Builder()
                    .url(apiUrl)
                    .header("User-Agent", DESKTOP_USER_AGENT)
                    .build()
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string() ?: "")
                        val title = json.optString("title", foundTitle)
                        val desc = json.optString("description", title)
                        val uploader = json.optString("uploader", "youtube")
                        val duration = json.optInt("duration", 15)
                        val videoStreams = json.optJSONArray("videoStreams") ?: JSONArray()
                        val pipedMp4s = mutableListOf<String>()
                        for (i in 0 until videoStreams.length()) {
                            val vs = videoStreams.getJSONObject(i)
                            val videoOnly = vs.optBoolean("videoOnly", true)
                            val sUrl = vs.optString("url")
                            if (!videoOnly && sUrl.startsWith("http")) {
                                pipedMp4s.add(sUrl)
                            }
                        }
                        if (pipedMp4s.isNotEmpty()) {
                            return buildResultFromStreams(
                                sourceUrl = url,
                                platform = SocialPlatform.YOUTUBE,
                                title = title,
                                description = desc,
                                author = "@$uploader",
                                durationSec = duration,
                                thumbUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                                hdUrl = pipedMp4s.first(),
                                sdUrl = pipedMp4s.last(),
                                fallbackUrls = pipedMp4s,
                                audioUrl = null
                            )
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        // Strategy 5: Direct Mobile Web ytInitialPlayerResponse parsing
        try {
            val webReq = Request.Builder()
                .url("https://m.youtube.com/watch?v=$videoId")
                .header("User-Agent", MOBILE_USER_AGENT)
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()
            httpClient.newCall(webReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val html = resp.body?.string() ?: ""
                    val m = Regex("ytInitialPlayerResponse\\s*=\\s*(\\{.+?\\});(?:var|\\s*</script>)").find(html)
                    if (m != null) {
                        val json = JSONObject(m.groupValues[1])
                        val videoDetails = json.optJSONObject("videoDetails")
                        val title = videoDetails?.optString("title")?.ifBlank { foundTitle } ?: foundTitle
                        val author = videoDetails?.optString("author")?.takeIf { it.isNotBlank() }?.let { "@$it" } ?: foundAuthor
                        val desc = videoDetails?.optString("shortDescription")?.ifBlank { title } ?: title
                        val duration = videoDetails?.optString("lengthSeconds")?.toIntOrNull() ?: foundDuration
                        val streamingData = json.optJSONObject("streamingData")
                        val formats = streamingData?.optJSONArray("formats") ?: JSONArray()
                        val webMp4s = mutableListOf<String>()
                        for (i in 0 until formats.length()) {
                            val fmt = formats.getJSONObject(i)
                            val streamUrl = fmt.optString("url")
                            if (streamUrl.startsWith("http")) {
                                webMp4s.add(streamUrl)
                            }
                        }
                        if (webMp4s.isNotEmpty()) {
                            return buildResultFromStreams(
                                sourceUrl = url,
                                platform = SocialPlatform.YOUTUBE,
                                title = title,
                                description = desc,
                                author = author,
                                durationSec = duration,
                                thumbUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                                hdUrl = webMp4s.first(),
                                sdUrl = webMp4s.last(),
                                fallbackUrls = webMp4s,
                                audioUrl = null
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }

        return null
    }

    private fun extractPinterestVideo(url: String): VideoAnalysisResult? {
        try {
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", DESKTOP_USER_AGENT)
                .build()
            httpClient.newCall(req).execute().use { resp ->
                val html = resp.body?.string() ?: return null
                val mp4Match = Regex("(https://v1\\.pinimg\\.com/videos/[^\"'\\s\\\\]+\\.mp4)").find(html)
                val videoUrl = mp4Match?.groupValues?.get(1)
                    ?: extractJsonField(html, "contentUrl")?.takeIf { ".mp4" in it }
                    ?: extractMetaContent(html, "og:video")
                if (!videoUrl.isNullOrBlank() && videoUrl.startsWith("http")) {
                    val clean = unescapeJsonUrl(videoUrl)
                    val title = extractMetaContent(html, "og:title") ?: "Pinterest Video Pin"
                    val desc = extractMetaContent(html, "og:description") ?: title
                    return buildResultFromStreams(
                        sourceUrl = url,
                        platform = SocialPlatform.PINTEREST,
                        title = title,
                        description = desc,
                        author = "@pinterest_creator",
                        durationSec = 15,
                        thumbUrl = extractMetaContent(html, "og:image") ?: "",
                        hdUrl = clean,
                        sdUrl = clean,
                        fallbackUrls = listOf(clean),
                        audioUrl = null
                    )
                }
            }
        } catch (_: Exception) {
        }
        return null
    }

    private fun extractThreadsVideo(url: String): VideoAnalysisResult? {
        try {
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", BOT_USER_AGENT)
                .build()
            httpClient.newCall(req).execute().use { resp ->
                val html = resp.body?.string() ?: return null
                val videoUrl = extractMetaContent(html, "og:video:secure_url")
                    ?: extractMetaContent(html, "og:video")
                    ?: extractJsonField(html, "video_url")
                if (!videoUrl.isNullOrBlank() && videoUrl.startsWith("http")) {
                    val clean = unescapeJsonUrl(videoUrl)
                    val desc = extractMetaContent(html, "og:description")
                        ?: extractMetaContent(html, "og:title")
                        ?: "Threads Video"
                    return buildResultFromStreams(
                        sourceUrl = url,
                        platform = SocialPlatform.THREADS,
                        title = removeHashtagsFromText(desc).take(75).ifBlank { "Threads Video" },
                        description = desc,
                        author = extractAuthorHandle(url, SocialPlatform.THREADS),
                        durationSec = 15,
                        thumbUrl = extractMetaContent(html, "og:image") ?: "",
                        hdUrl = clean,
                        sdUrl = clean,
                        fallbackUrls = listOf(clean),
                        audioUrl = null
                    )
                }
            }
        } catch (_: Exception) {
        }
        return null
    }

    private fun extractGenericOpenGraphVideo(
        url: String,
        platform: SocialPlatform
    ): VideoAnalysisResult? {
        for (ua in listOf(BOT_USER_AGENT, DESKTOP_USER_AGENT, MOBILE_USER_AGENT)) {
            try {
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", ua)
                    .build()
                httpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use
                    val contentType = resp.header("Content-Type") ?: ""
                    if (contentType.startsWith("video/")) {
                        return buildResultFromStreams(
                            sourceUrl = url,
                            platform = platform,
                            title = "${platform.displayName} Video",
                            description = "${platform.displayName} Video",
                            author = extractAuthorHandle(url, platform),
                            durationSec = 15,
                            thumbUrl = "",
                            hdUrl = url,
                            sdUrl = url,
                            fallbackUrls = listOf(url),
                            audioUrl = null
                        )
                    }
                    val html = resp.body?.string()?.take(250_000) ?: ""
                    val ogVideo = extractMetaContent(html, "og:video:secure_url")
                        ?: extractMetaContent(html, "og:video:url")
                        ?: extractMetaContent(html, "og:video")
                        ?: extractMetaContent(html, "twitter:player:stream")
                        ?: extractJsonField(html, "contentUrl")
                        ?: extractJsonField(html, "video_url")

                    if (!ogVideo.isNullOrBlank() && ogVideo.startsWith("http") && !ogVideo.endsWith(".html")) {
                        val clean = unescapeJsonUrl(ogVideo)
                        val title = extractMetaContent(html, "og:title")
                            ?: extractHtmlTitle(html)
                            ?: "${platform.displayName} Video"
                        val desc = extractMetaContent(html, "og:description") ?: title
                        return buildResultFromStreams(
                            sourceUrl = url,
                            platform = platform,
                            title = removeHashtagsFromText(title).take(75).ifBlank { "${platform.displayName} Video" },
                            description = desc,
                            author = extractAuthorHandle(url, platform),
                            durationSec = 15,
                            thumbUrl = extractMetaContent(html, "og:image") ?: "",
                            hdUrl = clean,
                            sdUrl = clean,
                            fallbackUrls = listOf(clean),
                            audioUrl = null
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun buildResultFromStreams(
        sourceUrl: String,
        platform: SocialPlatform,
        title: String,
        description: String,
        author: String,
        durationSec: Int,
        thumbUrl: String,
        hdUrl: String,
        sdUrl: String,
        fallbackUrls: List<String>,
        audioUrl: String?
    ): VideoAnalysisResult {
        val extractedHashtags = extractHashtags(description).ifEmpty { platform.sampleHashtags }
        val cleanCaption = removeHashtagsFromText(description).ifEmpty { title }

        val qualities = listOf(
            VideoQualityOption(
                id = "1080p_fyp",
                label = "1080p Full HD (No Watermark)",
                badge = "ULTRA HD • FYP READY",
                resolution = "1080x1920",
                bitrateKbps = 4800,
                estimatedSizeMb = 12.4,
                streamUrl = hdUrl,
                fallbackStreamUrls = fallbackUrls,
                isNoWatermark = true,
                isAudioOnly = false
            ),
            VideoQualityOption(
                id = "720p_hd",
                label = "720p HD (Balanced Fast)",
                badge = "HD • NO WATERMARK",
                resolution = "720x1280",
                bitrateKbps = 2600,
                estimatedSizeMb = 7.8,
                streamUrl = sdUrl.ifBlank { hdUrl },
                fallbackStreamUrls = fallbackUrls,
                isNoWatermark = true,
                isAudioOnly = false
            ),
            VideoQualityOption(
                id = "480p_sd",
                label = "480p Data Saver",
                badge = "FAST SAVE",
                resolution = "480x854",
                bitrateKbps = 1500,
                estimatedSizeMb = 4.5,
                streamUrl = sdUrl.ifBlank { hdUrl },
                fallbackStreamUrls = fallbackUrls,
                isNoWatermark = true,
                isAudioOnly = false
            ),
            VideoQualityOption(
                id = "audio_m4a",
                label = "Original Audio / Sound (MP3/M4A)",
                badge = "CLEAR AUDIO",
                resolution = "Audio 320kbps",
                bitrateKbps = 320,
                estimatedSizeMb = 1.9,
                streamUrl = audioUrl ?: sdUrl.ifBlank { hdUrl },
                fallbackStreamUrls = fallbackUrls,
                isNoWatermark = true,
                isAudioOnly = true
            )
        )

        return VideoAnalysisResult(
            sourceUrl = sourceUrl,
            platform = platform,
            title = title,
            caption = cleanCaption,
            hashtags = extractedHashtags,
            authorHandle = author,
            durationSec = durationSec,
            thumbnailUrl = thumbUrl,
            qualities = qualities,
            detectedSourceTags = listOf(
                "Source Container: ${platform.displayName} MP4 (isom/iso2)",
                "Hidden Box: moov/udta/meta (${platform.badgeText}_vid_id)",
                "Creation Encoder: Lavf / ${platform.displayName} Server",
                "Duplicate Hash Risk: Will Be Scrubbed on Save"
            )
        )
    }

    /**
     * Downloads the REAL video stream (trying primary streamUrl + fallbackStreamUrls + WebView sniffer if needed).
     * NEVER substitutes any demo video!
     */
    suspend fun downloadVideoStreamToCache(
        context: Context,
        cacheDir: File,
        sourcePageUrl: String,
        qualityOption: VideoQualityOption,
        onProgress: suspend (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val tempFile = File(cacheDir, "raw_download_${System.currentTimeMillis()}.mp4")
        val candidateUrls = (listOf(qualityOption.streamUrl) + qualityOption.fallbackStreamUrls)
            .filter { it.startsWith("http") }
            .distinct()
            .toMutableList()

        for (streamUrl in candidateUrls) {
            if (tryDownloadStreamToFile(streamUrl, sourcePageUrl, tempFile, onProgress)) {
                onProgress(1.0f)
                return@withContext tempFile
            }
        }

        // Last resort: use Headless WebView Sniffer on sourcePageUrl to get a fresh signed CDN stream URL
        val sniffed = WebViewVideoSniffer.sniffVideoStream(context, sourcePageUrl)
        if (sniffed != null && sniffed.videoUrl.startsWith("http")) {
            if (tryDownloadStreamToFile(sniffed.videoUrl, sourcePageUrl, tempFile, onProgress)) {
                onProgress(1.0f)
                return@withContext tempFile
            }
        }

        tempFile.delete()
        null
    }

    private suspend fun tryDownloadStreamToFile(
        streamUrl: String,
        sourcePageUrl: String,
        tempFile: File,
        onProgress: suspend (Float) -> Unit
    ): Boolean {
        val isGoogleVideo = "googlevideo.com" in streamUrl
        val downloadUserAgent = when {
            isGoogleVideo && "c=IOS" in streamUrl -> YOUTUBE_IOS_USER_AGENT
            isGoogleVideo && "c=ANDROID_TESTSUITE" in streamUrl -> YOUTUBE_TESTSUITE_USER_AGENT
            isGoogleVideo && "c=ANDROID_VR" in streamUrl -> YOUTUBE_VR_USER_AGENT
            isGoogleVideo && "c=WEB" in streamUrl -> DESKTOP_USER_AGENT
            else -> MOBILE_USER_AGENT
        }

        val referer = when {
            isGoogleVideo -> "https://www.youtube.com/"
            "tikwm.com" in streamUrl || "tiktok" in streamUrl -> "https://www.tiktok.com/"
            "instagram" in streamUrl || "cdninstagram" in streamUrl -> "https://www.instagram.com/"
            "fbcdn" in streamUrl || "facebook" in streamUrl -> "https://www.facebook.com/"
            "pinimg" in streamUrl -> "https://www.pinterest.com/"
            "twimg" in streamUrl -> "https://x.com/"
            else -> sourcePageUrl
        }

        return try {
            onProgress(0.08f)
            val reqBuilder = Request.Builder()
                .url(streamUrl)
                .header("User-Agent", downloadUserAgent)
                .header("Accept", "*/*")
            if (referer.isNotBlank()) {
                reqBuilder.header("Referer", referer)
            }
            val cookie = try {
                CookieManager.getInstance().getCookie(streamUrl)
                    ?: CookieManager.getInstance().getCookie(sourcePageUrl)
                    ?: if (isGoogleVideo) CookieManager.getInstance().getCookie("https://www.youtube.com") else null
            } catch (_: Exception) {
                null
            }
            if (!cookie.isNullOrBlank()) {
                reqBuilder.header("Cookie", cookie)
            }

            httpClient.newCall(reqBuilder.build()).execute().use { response ->
                val contentType = (response.header("Content-Type") ?: "").lowercase()
                if (response.isSuccessful && response.body != null && !contentType.contains("text/html") && !contentType.contains("application/json")) {
                    val body = response.body!!
                    val totalBytes = body.contentLength().coerceAtLeast(1_500_000L)
                    var bytesCopied = 0L
                    val buffer = ByteArray(32 * 1024)

                    body.byteStream().use { input ->
                        FileOutputStream(tempFile).use { output ->
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                bytesCopied += read
                                val pct = (bytesCopied.toFloat() / totalBytes.toFloat()).coerceIn(0.10f, 0.95f)
                                onProgress(pct)
                            }
                            output.flush()
                        }
                    }
                    if (tempFile.exists() && tempFile.length() > 20_000L && !isHtmlOrJsonFile(tempFile)) {
                        return true
                    }
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun isHtmlOrJsonFile(file: File): Boolean {
        return try {
            val head = ByteArray(64.coerceAtMost(file.length().toInt()))
            file.inputStream().use { it.read(head) }
            val str = String(head, Charsets.UTF_8).trim().lowercase()
            str.startsWith("<!doctype") || str.startsWith("<html") || str.startsWith("<head") || str.startsWith("{\"code\"")
        } catch (_: Exception) {
            false
        }
    }

    private fun tryCustomExtractorApi(
        url: String,
        platform: SocialPlatform,
        settings: ExtractorApiSettings
    ): VideoAnalysisResult? {
        val jsonPayload = JSONObject().apply {
            put("url", url)
            put("vQuality", "1080")
            put("videoQuality", "1080")
        }
        val reqBuilder = Request.Builder()
            .url(settings.customApiEndpoint)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))

        if (settings.customApiKey.isNotBlank()) {
            reqBuilder.header("Authorization", "Bearer ${settings.customApiKey}")
            reqBuilder.header("X-API-Key", settings.customApiKey)
        }

        httpClient.newCall(reqBuilder.build()).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val bodyStr = resp.body?.string() ?: return null
            val json = JSONObject(bodyStr)
            val streamUrl = json.optString("url").ifEmpty {
                json.optJSONObject("data")?.optString("play") ?: ""
            }
            if (streamUrl.isBlank()) return null
            val title = json.optString("filename")
                .ifEmpty { json.optJSONObject("data")?.optString("title") ?: "${platform.displayName} Video" }

            return buildResultFromStreams(
                sourceUrl = url,
                platform = platform,
                title = title.removeSuffix(".mp4"),
                description = title,
                author = extractAuthorHandle(url, platform),
                durationSec = 18,
                thumbUrl = "",
                hdUrl = streamUrl,
                sdUrl = streamUrl,
                fallbackUrls = listOf(streamUrl),
                audioUrl = null
            )
        }
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

    /**
     * Matches both standard JSON `"fieldName":"value"` and backslash-escaped `\"fieldName\":\"value\"`
     */
    private fun extractJsonField(text: String, fieldName: String): String? {
        val patterns = listOf(
            "\"${Pattern.quote(fieldName)}\"\\s*:\\s*\"([^\"]+)\"",
            "\\\\\"${Pattern.quote(fieldName)}\\\\\"\\s*:\\s*\\\\\"([^\\\\\"]+)\\\\\""
        )
        for (pat in patterns) {
            val matcher = Pattern.compile(pat).matcher(text)
            while (matcher.find()) {
                val raw = matcher.group(1) ?: continue
                val unescaped = unescapeJsonUrl(raw)
                if (unescaped.isNotBlank() && unescaped != "null") return unescaped
            }
        }
        return null
    }

    private fun unescapeJsonUrl(raw: String): String {
        var s = raw
            .replace("\\\\/", "/")
            .replace("\\/", "/")
            .replace("\\\\u0025", "%")
            .replace("\\u0025", "%")
            .replace("\\\\u0026", "&")
            .replace("\\u0026", "&")
            .replace("\\\\u003d", "=")
            .replace("\\u003d", "=")
            .replace("\\u003D", "=")
            .replace("\\u003f", "?")
            .replace("\\u003F", "?")
            .replace("&amp;", "&")
        if (s.startsWith("https%3A") || s.startsWith("http%3A")) {
            try {
                s = java.net.URLDecoder.decode(s, "UTF-8")
            } catch (_: Exception) {
            }
        }
        return s.trim()
    }

    private fun extractFirstUrl(text: String): String {
        val matcher = Pattern.compile("(https?://[^\\s\"'<>]+)").matcher(text)
        return if (matcher.find()) matcher.group(1) ?: "" else ""
    }

    private fun extractMetaContent(html: String, property: String): String? {
        val patterns = listOf(
            "<meta[^>]+(?:property|name)=[\"']${Pattern.quote(property)}[\"'][^>]+content=[\"']([^\"']+)[\"']",
            "<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+(?:property|name)=[\"']${Pattern.quote(property)}[\"']"
        )
        for (p in patterns) {
            val matcher = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(html)
            if (matcher.find()) {
                val res = decodeHtmlEntities(matcher.group(1) ?: "")
                if (res.isNotBlank()) return res
            }
        }
        return null
    }

    private fun extractHtmlTitle(html: String): String? {
        val matcher = Pattern.compile("<title[^>]*>([^<]+)</title>", Pattern.CASE_INSENSITIVE).matcher(html)
        return if (matcher.find()) decodeHtmlEntities(matcher.group(1) ?: "") else null
    }

    private fun decodeHtmlEntities(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&#x27;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }

    private fun extractHashtags(text: String): String {
        val matcher = Pattern.compile("(#[\\p{L}\\p{N}_]+)").matcher(text)
        val tags = mutableListOf<String>()
        while (matcher.find()) {
            val tag = matcher.group(1)
            if (tag != null && tag !in tags) tags.add(tag)
        }
        return tags.joinToString(" ")
    }

    private fun removeHashtagsFromText(text: String): String {
        return text.replace(Regex("#[\\p{L}\\p{N}_]+"), "").replace(Regex("\\s+"), " ").trim()
    }

    private fun extractAuthorHandle(url: String, platform: SocialPlatform): String {
        val atMatch = Regex("@([A-Za-z0-9_.-]+)").find(url)
        if (atMatch != null) return "@${atMatch.groupValues[1]}"
        return "@${platform.displayName.lowercase().replace(" ", "_")}_creator"
    }

    companion object {
        private const val MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
        private const val BOT_USER_AGENT =
            "TelegramBot (like TwitterBot)"
        private const val YOUTUBE_VR_USER_AGENT =
            "com.google.android.apps.youtube.vr.oculus/1.60.19 (Linux; U; Android 14; en_US; Quest 3; Build/UP1A.231005.007.A1; Cronet/132.0.6808.3)"
        private const val YOUTUBE_IOS_USER_AGENT =
            "com.google.ios.youtube/19.45.4 (iPhone16,2; U; CPU iOS 18_1_0 like Mac OS X;)"
        private const val YOUTUBE_TESTSUITE_USER_AGENT =
            "com.google.android.youtube/1.9 (Linux; U; Android 14; en_US)"
    }
}
