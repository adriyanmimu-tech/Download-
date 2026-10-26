package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.VaultDatabase
import com.example.data.VaultRepository
import com.example.ui.PulseSaveTab
import com.example.ui.PulseSaveViewModel
import com.example.ui.components.AmbientAuroraBackground
import com.example.ui.components.QualitySelectionPopupSheet
import com.example.ui.screens.ApiAndSettingsScreen
import com.example.ui.screens.DownloaderScreen
import com.example.ui.screens.FypScrubberScreen
import com.example.ui.screens.InAppVideoPlayerDialog
import com.example.ui.screens.VaultHistoryScreen
import com.example.ui.theme.AbyssalBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.TextPrimarySnow
import com.example.ui.theme.TextSecondarySlate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedText = if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }

        val database = VaultDatabase.getInstance(applicationContext)
        val repository = VaultRepository(database.vaultDao(), applicationContext)

        setContent {
            MyApplicationTheme {
                val pulseViewModel: PulseSaveViewModel = viewModel(
                    factory = PulseSaveViewModel.Factory(repository)
                )

                LaunchedEffect(sharedText) {
                    if (!sharedText.isNullOrBlank()) {
                        pulseViewModel.updateUrlInput(sharedText)
                        pulseViewModel.analyzeAndOpenQualityPopup(this@MainActivity)
                    }
                }

                PulseSaveMainApp(viewModel = pulseViewModel)
            }
        }
    }
}

@Composable
fun PulseSaveMainApp(
    viewModel: PulseSaveViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val vaultVideos by viewModel.vaultVideos.collectAsStateWithLifecycle()
    val apiSettings by viewModel.apiSettings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusSnackbarMessage) {
        val msg = uiState.statusSnackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    AmbientAuroraBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                GlassBottomNavigationBar(
                    currentTab = uiState.currentTab,
                    vaultCount = vaultVideos.size,
                    onSelectTab = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentTab) {
                    PulseSaveTab.DOWNLOADER -> {
                        DownloaderScreen(
                            urlInput = uiState.urlInput,
                            detectedPlatform = uiState.detectedPlatform,
                            isAnalyzing = uiState.isAnalyzingLink,
                            isDownloadingAndScrubbing = uiState.isDownloadingAndScrubbing,
                            downloadProgress = uiState.downloadProgress,
                            progressStepMessage = uiState.progressMessage,
                            latestScrubReport = uiState.latestDownloaderReport,
                            onUrlChange = { viewModel.updateUrlInput(it) },
                            onPasteFromClipboard = {
                                val pasted = readFromClipboard(context)
                                if (!pasted.isNullOrBlank()) {
                                    viewModel.updateUrlInput(pasted)
                                    viewModel.showMessage("ক্লিপবোর্ড থেকে লিংক পেস্ট করা হয়েছে!")
                                } else {
                                    viewModel.showMessage("ক্লিপবোর্ডে কোনো লিংক পাওয়া যায়নি।")
                                }
                            },
                            onSelectSamplePlatform = { platform ->
                                viewModel.selectSamplePlatform(platform)
                            },
                            onClickDownloadButton = {
                                viewModel.analyzeAndOpenQualityPopup(context)
                            },
                            onPlayLatestVideo = {
                                uiState.latestDownloaderReport?.let { rep ->
                                    viewModel.openVideoPlayer(
                                        title = rep.title,
                                        filePath = rep.savedFilePath,
                                        galleryUri = rep.savedGalleryUri
                                    )
                                }
                            },
                            onNavigateToLocalScrubber = {
                                viewModel.selectTab(PulseSaveTab.FYP_SCRUBBER)
                            }
                        )
                    }

                    PulseSaveTab.FYP_SCRUBBER -> {
                        FypScrubberScreen(
                            inspection = uiState.localInspection,
                            fypConfig = uiState.fypConfig,
                            isScrubbing = uiState.isScrubbingLocal,
                            scrubProgressMessage = uiState.localScrubMessage,
                            latestReport = uiState.latestLocalReport,
                            onVideoPicked = { uri ->
                                viewModel.inspectPickedLocalVideo(context, uri)
                            },
                            onLoadDemoVideoForInspection = {
                                viewModel.loadDemoVideoForLocalScrubber(context)
                            },
                            onUpdateFypConfig = { viewModel.updateFypConfig(it) },
                            onCleanSelectedVideo = {
                                viewModel.cleanSelectedLocalVideo(context)
                            },
                            onPlayCleanedVideo = {
                                uiState.latestLocalReport?.let { rep ->
                                    viewModel.openVideoPlayer(
                                        title = rep.title,
                                        filePath = rep.savedFilePath,
                                        galleryUri = rep.savedGalleryUri
                                    )
                                }
                            },
                            onBackToHome = {
                                viewModel.selectTab(PulseSaveTab.DOWNLOADER)
                            }
                        )
                    }

                    PulseSaveTab.VAULT_HISTORY -> {
                        VaultHistoryScreen(
                            videos = vaultVideos,
                            onPlayVideo = { item ->
                                viewModel.openVideoPlayer(
                                    title = item.title,
                                    filePath = item.localFilePath,
                                    galleryUri = item.savedGalleryUri
                                )
                            },
                            onCopyCaption = { item ->
                                val fullText = "${item.title}\n\n${item.caption}\n\n${item.hashtags}".trim()
                                copyToClipboard(context, "Video Caption", fullText)
                                viewModel.showMessage("টাইটেল, ক্যাপশন ও হ্যাশট্যাগ কপি হয়েছে!")
                            },
                            onShareVideo = { item ->
                                shareVideoUri(context, item.savedGalleryUri, item.title, item.caption)
                            },
                            onDeleteVideo = { id ->
                                viewModel.deleteVaultVideo(id)
                            },
                            onBackToHome = {
                                viewModel.selectTab(PulseSaveTab.DOWNLOADER)
                            }
                        )
                    }

                    PulseSaveTab.API_SETTINGS -> {
                        ApiAndSettingsScreen(
                            currentSettings = apiSettings,
                            onSaveSettings = { endpoint, key, camSig ->
                                viewModel.saveApiSettings(endpoint, key, camSig)
                            },
                            onBackToHome = {
                                viewModel.selectTab(PulseSaveTab.DOWNLOADER)
                            }
                        )
                    }
                }
            }

            // Quality Selection & Caption Popup Menu (ModalBottomSheet)
            val analysis = uiState.currentAnalysis
            val selectedQuality = uiState.selectedQuality
            if (uiState.showQualityPopup && analysis != null && selectedQuality != null) {
                QualitySelectionPopupSheet(
                    analysis = analysis,
                    selectedQuality = selectedQuality,
                    editableTitle = uiState.editableTitle,
                    editableCaption = uiState.editableCaption,
                    editableHashtags = uiState.editableHashtags,
                    fypConfig = uiState.fypConfig,
                    onTitleChange = { viewModel.updateEditableTitle(it) },
                    onCaptionChange = { viewModel.updateEditableCaption(it) },
                    onHashtagsChange = { viewModel.updateEditableHashtags(it) },
                    onSelectQuality = { viewModel.selectQualityOption(it) },
                    onUpdateFypConfig = { viewModel.updateFypConfig(it) },
                    onCopyTitleAndCaption = {
                        val combined = "${uiState.editableTitle}\n\n${uiState.editableCaption}\n\n${uiState.editableHashtags}".trim()
                        copyToClipboard(context, "PulseSave Caption", combined)
                        viewModel.showMessage("টাইটেল, ক্যাপশন ও হ্যাশট্যাগ কপি করা হয়েছে!")
                    },
                    onConfirmDownloadAndSave = {
                        viewModel.confirmDownloadAndScrubToGallery(context)
                    },
                    onDismiss = {
                        viewModel.dismissQualityPopup()
                    }
                )
            }

            // In-App Video Player Modal
            val playerState = uiState.activePlayerVideo
            if (playerState != null) {
                InAppVideoPlayerDialog(
                    title = playerState.title,
                    videoFilePath = playerState.localFilePath,
                    galleryUriString = playerState.galleryUri,
                    onDismiss = { viewModel.closeVideoPlayer() }
                )
            }
        }
    }
}

@Composable
private fun GlassBottomNavigationBar(
    currentTab: PulseSaveTab,
    vaultCount: Int,
    onSelectTab: (PulseSaveTab) -> Unit
) {
    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.84f),
        contentColor = TextPrimarySnow,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(
                width = 1.5.dp,
                color = androidx.compose.ui.graphics.Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
    ) {
        val navColors = NavigationBarItemDefaults.colors(
            selectedIconColor = androidx.compose.ui.graphics.Color.White,
            selectedTextColor = ElectricCyan,
            indicatorColor = ElectricCyan,
            unselectedIconColor = TextSecondarySlate,
            unselectedTextColor = TextSecondarySlate
        )

        NavigationBarItem(
            selected = currentTab == PulseSaveTab.DOWNLOADER,
            onClick = { onSelectTab(PulseSaveTab.DOWNLOADER) },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Downloader"
                )
            },
            label = { Text("ডাউনলোডার") },
            colors = navColors,
            modifier = Modifier.testTag("nav_tab_downloader")
        )

        NavigationBarItem(
            selected = currentTab == PulseSaveTab.FYP_SCRUBBER,
            onClick = { onSelectTab(PulseSaveTab.FYP_SCRUBBER) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "FYP Scrubber"
                )
            },
            label = { Text("FYP ক্লিনার") },
            colors = navColors,
            modifier = Modifier.testTag("nav_tab_scrubber")
        )

        NavigationBarItem(
            selected = currentTab == PulseSaveTab.VAULT_HISTORY,
            onClick = { onSelectTab(PulseSaveTab.VAULT_HISTORY) },
            icon = {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "Gallery Vault"
                )
            },
            label = {
                Text(if (vaultCount > 0) "ভল্ট ($vaultCount)" else "গ্যালারি ভল্ট")
            },
            colors = navColors,
            modifier = Modifier.testTag("nav_tab_vault")
        )

        NavigationBarItem(
            selected = currentTab == PulseSaveTab.API_SETTINGS,
            onClick = { onSelectTab(PulseSaveTab.API_SETTINGS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "API & Settings"
                )
            },
            label = { Text("সেটিংস") },
            colors = navColors,
            modifier = Modifier.testTag("nav_tab_settings")
        )
    }
}

private fun readFromClipboard(context: Context): String? {
    return try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val item = clipboard?.primaryClip?.getItemAt(0)
        item?.coerceToText(context)?.toString()?.trim()
    } catch (_: Exception) {
        null
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    } catch (_: Exception) {
    }
}

private fun shareVideoUri(
    context: Context,
    uriString: String,
    title: String,
    caption: String
) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, Uri.parse(uriString))
            putExtra(Intent.EXTRA_TEXT, "$title\n$caption")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share FYP-Safe Video"))
    } catch (_: Exception) {
    }
}
