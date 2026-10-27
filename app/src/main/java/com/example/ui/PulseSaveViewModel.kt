package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ExtractorApiSettings
import com.example.data.FypOptimizationConfig
import com.example.data.LocalVideoInspection
import com.example.data.SocialPlatform
import com.example.data.VaultRepository
import com.example.data.VaultVideoEntity
import com.example.data.VideoAnalysisResult
import com.example.data.VideoQualityOption
import com.example.data.VideoScrubReport
import com.example.engine.FypMediaScrubberEngine
import com.example.engine.SocialVideoExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class PulseSaveTab {
    DOWNLOADER,
    FYP_SCRUBBER,
    VIRAL_STUDIO,
    VAULT_HISTORY,
    API_SETTINGS
}

data class ActiveVideoPlayerState(
    val title: String,
    val localFilePath: String,
    val galleryUri: String
)

data class PulseSaveUiState(
    val currentTab: PulseSaveTab = PulseSaveTab.DOWNLOADER,
    val urlInput: String = "",
    val detectedPlatform: SocialPlatform = SocialPlatform.TIKTOK,
    val isAnalyzingLink: Boolean = false,
    val showQualityPopup: Boolean = false,
    val currentAnalysis: VideoAnalysisResult? = null,
    val selectedQuality: VideoQualityOption? = null,
    val editableTitle: String = "",
    val editableCaption: String = "",
    val editableHashtags: String = "",
    val fypConfig: FypOptimizationConfig = FypOptimizationConfig(),
    val isDownloadingAndScrubbing: Boolean = false,
    val downloadProgress: Float = 0f,
    val progressMessage: String = "",
    val latestDownloaderReport: VideoScrubReport? = null,
    val localInspection: LocalVideoInspection? = null,
    val selectedLocalUri: Uri? = null,
    val isScrubbingLocal: Boolean = false,
    val localScrubMessage: String = "",
    val latestLocalReport: VideoScrubReport? = null,
    val activePlayerVideo: ActiveVideoPlayerState? = null,
    val statusSnackbarMessage: String? = null
)

class PulseSaveViewModel(
    private val repository: VaultRepository,
    private val extractor: SocialVideoExtractor = SocialVideoExtractor()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PulseSaveUiState())
    val uiState: StateFlow<PulseSaveUiState> = _uiState.asStateFlow()

    val vaultVideos: StateFlow<List<VaultVideoEntity>> = repository.allVideos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val apiSettings: StateFlow<ExtractorApiSettings> = repository.apiSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExtractorApiSettings()
        )

    fun selectTab(tab: PulseSaveTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun updateUrlInput(newUrl: String) {
        val platform = if (newUrl.isBlank()) _uiState.value.detectedPlatform else SocialPlatform.detectFromUrl(newUrl)
        _uiState.update {
            it.copy(
                urlInput = newUrl,
                detectedPlatform = platform
            )
        }
    }

    fun selectSamplePlatform(platform: SocialPlatform) {
        _uiState.update {
            it.copy(
                detectedPlatform = platform,
                statusSnackbarMessage = "${platform.displayName} সিলেক্ট হয়েছে — এখন আপনার ${platform.displayName} ভিডিওর আসল লিংক পেস্ট করুন!"
            )
        }
    }

    fun analyzeAndOpenQualityPopup(context: Context) {
        val currentUrl = _uiState.value.urlInput.trim()
        if (currentUrl.isEmpty()) {
            _uiState.update {
                it.copy(statusSnackbarMessage = "অনুগ্রহ করে প্রথমে একটি আসল ভিডিও লিংক পেস্ট করুন!")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingLink = true) }
            val result = extractor.analyzeVideoLink(context, currentUrl, apiSettings.value)
            if (result == null) {
                _uiState.update {
                    it.copy(
                        isAnalyzingLink = false,
                        statusSnackbarMessage = "এই লিংক থেকে সরাসরি ভিডিও স্ট্রিম পাওয়া যায়নি। ভিডিওটি পাবলিক কি না চেক করুন!"
                    )
                }
                return@launch
            }
            val firstQuality = result.qualities.firstOrNull()
            _uiState.update {
                it.copy(
                    isAnalyzingLink = false,
                    currentAnalysis = result,
                    selectedQuality = firstQuality,
                    editableTitle = result.title,
                    editableCaption = result.caption,
                    editableHashtags = result.hashtags,
                    showQualityPopup = true
                )
            }
        }
    }

    fun dismissQualityPopup() {
        _uiState.update { it.copy(showQualityPopup = false) }
    }

    fun updateEditableTitle(title: String) {
        _uiState.update { it.copy(editableTitle = title) }
    }

    fun updateEditableCaption(caption: String) {
        _uiState.update { it.copy(editableCaption = caption) }
    }

    fun updateEditableHashtags(hashtags: String) {
        _uiState.update { it.copy(editableHashtags = hashtags) }
    }

    fun selectQualityOption(option: VideoQualityOption) {
        _uiState.update { it.copy(selectedQuality = option) }
    }

    fun updateFypConfig(config: FypOptimizationConfig) {
        _uiState.update { it.copy(fypConfig = config) }
    }

    fun confirmDownloadAndScrubToGallery(context: Context) {
        val state = _uiState.value
        val analysis = state.currentAnalysis ?: return
        val quality = state.selectedQuality ?: analysis.qualities.firstOrNull() ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showQualityPopup = false,
                    isDownloadingAndScrubbing = true,
                    downloadProgress = 0.05f,
                    progressMessage = "${analysis.platform.displayName} থেকে আসল ভিডিও ডাউনলোড হচ্ছে…"
                )
            }

            val rawTempFile = extractor.downloadVideoStreamToCache(
                context = context,
                cacheDir = context.cacheDir,
                sourcePageUrl = analysis.sourceUrl,
                qualityOption = quality,
                onProgress = { pct ->
                    _uiState.update {
                        it.copy(
                            downloadProgress = (pct * 0.55f).coerceIn(0.05f, 0.55f),
                            progressMessage = "আসল ভিডিও স্ট্রিম ডাউনলোড হচ্ছে (${(pct * 100).toInt()}%)…"
                        )
                    }
                }
            )

            if (rawTempFile == null || !rawTempFile.exists()) {
                _uiState.update {
                    it.copy(
                        isDownloadingAndScrubbing = false,
                        statusSnackbarMessage = "ভিডিও স্ট্রিম ডাউনলোড করা যায়নি। অনুগ্রহ করে লিংকটি আবার চেক করুন।"
                    )
                }
                return@launch
            }

            val report = FypMediaScrubberEngine.scrubAndSaveToGallery(
                context = context,
                sourceFile = rawTempFile,
                title = state.editableTitle.ifBlank { analysis.title },
                platformName = analysis.platform.displayName,
                qualityLabel = quality.label,
                config = state.fypConfig,
                onStepUpdate = { msg, stepPct ->
                    _uiState.update {
                        it.copy(
                            downloadProgress = (0.55f + stepPct * 0.45f).coerceIn(0.55f, 1.0f),
                            progressMessage = msg
                        )
                    }
                }
            )

            rawTempFile.delete()

            val entity = VaultVideoEntity(
                title = state.editableTitle.ifBlank { analysis.title },
                caption = state.editableCaption,
                hashtags = state.editableHashtags,
                platformName = analysis.platform.displayName,
                sourceUrl = analysis.sourceUrl,
                qualityLabel = quality.label,
                resolution = quality.resolution,
                savedGalleryUri = report.savedGalleryUri,
                localFilePath = report.savedFilePath,
                originalMd5 = report.originalMd5,
                cleanedMd5 = report.cleanedMd5,
                removedTagsCount = report.removedTagsCount,
                appliedProtectionsSummary = report.appliedProtections.joinToString(" • "),
                fileSizeBytes = report.cleanedSizeBytes,
                isFypSafe = state.fypConfig.enabled,
                fypSafetyScore = report.fypSafetyScore
            )
            repository.insertVideo(entity)

            _uiState.update {
                it.copy(
                    isDownloadingAndScrubbing = false,
                    downloadProgress = 1f,
                    latestDownloaderReport = report,
                    statusSnackbarMessage = "গ্যালারিতে আসল ভিডিও সেভ সম্পন্ন! মেটাডাটা রিমুভ ও নতুন MD5 হ্যাশ তৈরি হয়েছে।"
                )
            }
        }
    }

    fun inspectPickedLocalVideo(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedLocalUri = uri,
                    isScrubbingLocal = true,
                    localScrubMessage = "ভিডিওর মেটাডাটা ও কন্টেইনার হ্যাশ বিশ্লেষণ করা হচ্ছে…"
                )
            }
            val inspection = FypMediaScrubberEngine.inspectLocalVideo(context, uri)
            _uiState.update {
                it.copy(
                    isScrubbingLocal = false,
                    localInspection = inspection,
                    statusSnackbarMessage = "ভিডিও স্ক্যান সম্পন্ন! ${inspection.detectedMetadata.size}টি মেটাডাটা ট্যাগ পাওয়া গেছে।"
                )
            }
        }
    }

    fun loadDemoVideoForLocalScrubber(context: Context) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScrubbingLocal = true,
                    localScrubMessage = "ডেমো ভিডিও তৈরি ও মেটাডাটা স্ক্যান করা হচ্ছে…"
                )
            }
            val demoFile = File(context.cacheDir, "TikTok_Raw_Download_7419283.mp4")
            FypMediaScrubberEngine.generateValidPlayableSampleMp4(demoFile)
            val uri = Uri.fromFile(demoFile)
            val inspection = FypMediaScrubberEngine.inspectLocalVideo(context, uri)
            _uiState.update {
                it.copy(
                    selectedLocalUri = uri,
                    isScrubbingLocal = false,
                    localInspection = inspection,
                    statusSnackbarMessage = "ডেমো ভিডিও লোড হয়েছে! এখন 'মেটাডাটা রিমুভ ও FYP Safe' বাটনে ট্যাপ করুন।"
                )
            }
        }
    }

    fun cleanSelectedLocalVideo(context: Context) {
        val state = _uiState.value
        val localUri = state.selectedLocalUri
        if (localUri == null) {
            _uiState.update {
                it.copy(statusSnackbarMessage = "অনুগ্রহ করে প্রথমে গ্যালারি থেকে একটি ভিডিও সিলেক্ট করুন!")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScrubbingLocal = true,
                    localScrubMessage = "গ্যালারি ভিডিও রিমাক্স ও মেটাডাটা ক্লিন করা হচ্ছে…"
                )
            }

            val inputFile = File(context.cacheDir, "local_source_${System.currentTimeMillis()}.mp4")
            try {
                context.contentResolver.openInputStream(localUri)?.use { input ->
                    FileOutputStream(inputFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (_: Exception) {
            }

            if (!inputFile.exists() || inputFile.length() == 0L) {
                _uiState.update {
                    it.copy(
                        isScrubbingLocal = false,
                        statusSnackbarMessage = "গ্যালারি থেকে ভিডিও ফাইলটি পড়া যায়নি। আবার সিলেক্ট করুন।"
                    )
                }
                return@launch
            }

            val title = state.localInspection?.fileName?.removeSuffix(".mp4") ?: "Gallery_Cleaned_Video"
            val report = FypMediaScrubberEngine.scrubAndSaveToGallery(
                context = context,
                sourceFile = inputFile,
                title = title,
                platformName = "Gallery Scrubber",
                qualityLabel = state.localInspection?.resolution ?: "1080p FYP Clean",
                config = state.fypConfig.copy(enabled = true),
                onStepUpdate = { msg, _ ->
                    _uiState.update { it.copy(localScrubMessage = msg) }
                }
            )
            inputFile.delete()

            val entity = VaultVideoEntity(
                title = title,
                caption = "Cleaned via PulseSave FYP Scrubber — Ready for Copyright-Free Upload",
                hashtags = "#fyp #viral #originalcontent #pulsesave",
                platformName = "Gallery Scrubber",
                sourceUrl = "local://gallery",
                qualityLabel = report.qualityLabel,
                resolution = state.localInspection?.resolution ?: "1080x1920",
                savedGalleryUri = report.savedGalleryUri,
                localFilePath = report.savedFilePath,
                originalMd5 = report.originalMd5,
                cleanedMd5 = report.cleanedMd5,
                removedTagsCount = report.removedTagsCount,
                appliedProtectionsSummary = report.appliedProtections.joinToString(" • "),
                fileSizeBytes = report.cleanedSizeBytes,
                isFypSafe = true,
                fypSafetyScore = 99
            )
            repository.insertVideo(entity)

            _uiState.update {
                it.copy(
                    isScrubbingLocal = false,
                    latestLocalReport = report,
                    statusSnackbarMessage = "ভিডিওটির মেটাডাটা ও হ্যাশ ক্লিন করে গ্যালারিতে সেভ করা হয়েছে!"
                )
            }
        }
    }

    fun openVideoPlayer(title: String, filePath: String, galleryUri: String) {
        _uiState.update {
            it.copy(
                activePlayerVideo = ActiveVideoPlayerState(
                    title = title,
                    localFilePath = filePath,
                    galleryUri = galleryUri
                )
            )
        }
    }

    fun closeVideoPlayer() {
        _uiState.update { it.copy(activePlayerVideo = null) }
    }

    fun deleteVaultVideo(id: Int) {
        viewModelScope.launch {
            repository.deleteVideoById(id)
            _uiState.update {
                it.copy(statusSnackbarMessage = "ভিডিওটি ভল্ট হিস্ট্রি থেকে মুছে ফেলা হয়েছে।")
            }
        }
    }

    fun saveApiSettings(endpoint: String, apiKey: String, cameraSig: String) {
        viewModelScope.launch {
            repository.saveApiSettings(endpoint, apiKey, cameraSig)
            _uiState.update {
                it.copy(
                    fypConfig = it.fypConfig.copy(customCameraDeviceTag = cameraSig),
                    statusSnackbarMessage = "এক্সট্র্যাক্টর API এবং ক্যামেরা সিগনেচার সেটিংস সেভ হয়েছে!"
                )
            }
        }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(statusSnackbarMessage = msg) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(statusSnackbarMessage = null) }
    }

    class Factory(private val repository: VaultRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PulseSaveViewModel(repository) as T
        }
    }
}
