package com.example.data

import androidx.compose.ui.graphics.Color

enum class SocialPlatform(
    val displayName: String,
    val badgeText: String,
    val brandColor: Color,
    val secondaryColor: Color,
    val sampleUrl: String,
    val sampleTitle: String,
    val sampleCaption: String,
    val sampleHashtags: String
) {
    TIKTOK(
        displayName = "TikTok",
        badgeText = "TT",
        brandColor = Color(0xFF00F2FE),
        secondaryColor = Color(0xFFFF0050),
        sampleUrl = "https://www.tiktok.com/@creator_studio/video/7429184920184",
        sampleTitle = "Cinematic Night Neon Street Walk in 4K 60FPS",
        sampleCaption = "রাতের শহরের নিওন লাইট এবং সিনেম্যাটিক ট্রানজিশন! কোন শটটা আপনার সবচেয়ে ভালো লেগেছে?",
        sampleHashtags = "#tiktokbangladesh #cinematic #fyp #viral #neonaesthetic #capcut #trending"
    ),
    INSTAGRAM(
        displayName = "Instagram Reels",
        badgeText = "IG",
        brandColor = Color(0xFFE1306C),
        secondaryColor = Color(0xFFF77737),
        sampleUrl = "https://www.instagram.com/reel/C9xK2mLpQvR/",
        sampleTitle = "Aesthetic Coffee & Morning Productivity Routine",
        sampleCaption = "সকালের এক কাপ কফি আর শান্ত পরিবেশে কাজের প্রস্তুতি। Save this reel for motivation!",
        sampleHashtags = "#reelsinstagram #aestheticvideos #morningroutine #viralreels #creator"
    ),
    FACEBOOK(
        displayName = "Facebook Watch",
        badgeText = "FB",
        brandColor = Color(0xFF1877F2),
        secondaryColor = Color(0xFF00C6FF),
        sampleUrl = "https://www.facebook.com/watch/?v=1092837465192",
        sampleTitle = "Amazing Drone View of Sajek Valley Clouds",
        sampleCaption = "মেঘের রাজ্য সাজেক ভ্যালির অসাধারণ ড্রোন ভিউ! বন্ধুদের ট্যাগ করুন যাদের সাথে যেতে চান।",
        sampleHashtags = "#facebookreels #sajekvalley #bangladeshtravel #dronefootage #nature"
    ),
    X_TWITTER(
        displayName = "X / Twitter",
        badgeText = "X",
        brandColor = Color(0xFFE2E8F0),
        secondaryColor = Color(0xFF38BDF8),
        sampleUrl = "https://x.com/techvisuals/status/18492019283746",
        sampleTitle = "Next-Gen AI Robotics & Motion Demo Clip",
        sampleCaption = "ভবিষ্যতের প্রযুক্তি কীভাবে আমাদের দৈনন্দিন জীবন বদলে দিচ্ছে দেখুন এই শর্ট ক্লিপে।",
        sampleHashtags = "#technews #innovation #futuretech #viralclip #xvideo"
    ),
    YOUTUBE(
        displayName = "YT Shorts",
        badgeText = "YT",
        brandColor = Color(0xFFFF0033),
        secondaryColor = Color(0xFFFF6B6B),
        sampleUrl = "https://youtube.com/shorts/dQw4w9WgXcQ",
        sampleTitle = "Quick 30-Second Mobile Photography Secret Trick",
        sampleCaption = "ফোনের ক্যামেরা দিয়ে প্রফেশনাল ছবি ও ভিডিও তোলার গোপন ট্রিক!",
        sampleHashtags = "#shorts #mobilephotography #videotips #contentcreator #viralshorts"
    ),
    PINTEREST(
        displayName = "Pinterest Pin",
        badgeText = "PIN",
        brandColor = Color(0xFFE60023),
        secondaryColor = Color(0xFFFF758C),
        sampleUrl = "https://www.pinterest.com/pin/58293475619283/",
        sampleTitle = "Minimalist Workspace Setup & RGB Lighting",
        sampleCaption = "ড্রিম ডেস্ক সেটআপ আইডিয়া যা আপনার কাজের আগ্রহ দ্বিগুণ করে দেবে।",
        sampleHashtags = "#desksetup #pinterestinspired #minimalist #rgbsetup #aesthetic"
    ),
    THREADS(
        displayName = "Threads",
        badgeText = "TH",
        brandColor = Color(0xFFA855F7),
        secondaryColor = Color(0xFF6366F1),
        sampleUrl = "https://www.threads.net/@designcraft/post/C8mPqR2xL",
        sampleTitle = "Glassmorphism UI Motion Design Breakdown",
        sampleCaption = "মডার্ন অ্যাপ ডিজাইনে গ্লাসমরফিজম ও স্মুথ অ্যানিমেশনের ব্যবহার।",
        sampleHashtags = "#threads #uidesign #glassmorphism #motiongraphics #creative"
    ),
    LIKEE(
        displayName = "Likee / Snack",
        badgeText = "LK",
        brandColor = Color(0xFFFF4B2B),
        secondaryColor = Color(0xFFFF416C),
        sampleUrl = "https://likee.video/@trending_bd/video/9283746512",
        sampleTitle = "Fast Transition Beat Sync Edit Tutorial",
        sampleCaption = "বিট সিঙ্ক ট্রানজিশন ভিডিও এডিটিং টিউটোরিয়াল!",
        sampleHashtags = "#beatsync #transition #shortvideotutorial #trendingbd"
    );

    companion object {
        fun detectFromUrl(url: String): SocialPlatform {
            val lower = url.lowercase().trim()
            return when {
                "tiktok.com" in lower || "vm.tiktok" in lower || "vt.tiktok" in lower -> TIKTOK
                "instagram.com" in lower || "instagr.am" in lower -> INSTAGRAM
                "facebook.com" in lower || "fb.watch" in lower || "fb.com" in lower -> FACEBOOK
                "twitter.com" in lower || "x.com" in lower || "t.co" in lower -> X_TWITTER
                "youtube.com" in lower || "youtu.be" in lower -> YOUTUBE
                "pinterest." in lower || "pin.it" in lower -> PINTEREST
                "threads.net" in lower -> THREADS
                "likee." in lower || "snackvideo" in lower -> LIKEE
                else -> TIKTOK
            }
        }
    }
}

data class VideoQualityOption(
    val id: String,
    val label: String,
    val badge: String,
    val resolution: String,
    val bitrateKbps: Int,
    val estimatedSizeMb: Double,
    val streamUrl: String,
    val fallbackStreamUrls: List<String> = emptyList(),
    val isNoWatermark: Boolean = true,
    val isAudioOnly: Boolean = false
)

data class VideoAnalysisResult(
    val sourceUrl: String,
    val platform: SocialPlatform,
    val title: String,
    val caption: String,
    val hashtags: String,
    val authorHandle: String,
    val durationSec: Int,
    val thumbnailUrl: String,
    val qualities: List<VideoQualityOption>,
    val detectedSourceTags: List<String>
)

data class FypOptimizationConfig(
    val enabled: Boolean = true,
    val stripAllMetadata: Boolean = true,
    val randomizeMd5Hash: Boolean = true,
    val microTrimFrames: Boolean = true,
    val trimMillis: Long = 180L,
    val modifyAudioTrackHeader: Boolean = true,
    val mirrorHorizontalMetadata: Boolean = false,
    val injectFreshCreationTimestamp: Boolean = true,
    val customCameraDeviceTag: String = "iPhone 16 Pro Max (iOS 18.1)"
)

data class LocalVideoInspection(
    val uriString: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val durationMs: Long,
    val resolution: String,
    val bitrateKbps: Int,
    val originalMd5: String,
    val originalSha256: String,
    val detectedMetadata: Map<String, String>,
    val fypRiskLevel: String,
    val fypRiskReasons: List<String>
)

data class VideoScrubReport(
    val title: String,
    val platformName: String,
    val qualityLabel: String,
    val originalMd5: String,
    val cleanedMd5: String,
    val originalSha256: String,
    val cleanedSha256: String,
    val originalSizeBytes: Long,
    val cleanedSizeBytes: Long,
    val removedTagsCount: Int,
    val removedTagNames: List<String>,
    val appliedProtections: List<String>,
    val savedGalleryUri: String,
    val savedFilePath: String,
    val fypSafetyScore: Int
)
