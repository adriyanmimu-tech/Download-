package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.FypOptimizationConfig
import com.example.data.LocalVideoInspection
import com.example.data.VideoScrubReport
import com.example.ui.components.GlassCard
import com.example.ui.components.ScrubReportVerificationCard
import com.example.ui.theme.AbyssalBlue
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TextPrimarySnow
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.VividViolet

@Composable
fun FypScrubberScreen(
    inspection: LocalVideoInspection?,
    fypConfig: FypOptimizationConfig,
    isScrubbing: Boolean,
    scrubProgressMessage: String,
    latestReport: VideoScrubReport?,
    onVideoPicked: (Uri) -> Unit,
    onLoadDemoVideoForInspection: () -> Unit,
    onUpdateFypConfig: (FypOptimizationConfig) -> Unit,
    onCleanSelectedVideo: () -> Unit,
    onPlayCleanedVideo: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onVideoPicked(uri)
        }
    }

    val cameraPresets = listOf(
        "iPhone 16 Pro Max (iOS 18.1)",
        "Samsung Galaxy S25 Ultra",
        "Sony Alpha FX3 Cinema Line",
        "Pixel 9 Pro XL (Android 15)"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
        ) {
            Text(
                text = "TikTok FYP Safe & Metadata Cleaner",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimarySnow
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "গ্যালারির যেকোনো ভিডিও থেকে কপিরাইট মেটাডাটা, সোর্স প্ল্যাটফর্ম ট্যাগ ও পুরনো MD5 হ্যাশ মুছে ১০০% অরিজিনাল হিসেবে সেভ করুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1: Pick Video from Gallery or Load Sample Video
            GlassCard(accentGlowColor = ElectricCyan) {
                Text(
                    text = "১. গ্যালারি থেকে ভিডিও ইমপোর্ট করুন",
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(ElectricCyan, VividViolet)))
                            .clickable {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            }
                            .testTag("pick_gallery_video_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Pick Gallery Video",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "গ্যালারি থেকে নিন",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AbyssalBlue)
                            .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onLoadDemoVideoForInspection)
                            .padding(horizontal = 14.dp)
                            .testTag("load_demo_video_scrubber_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = "Demo Video",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ডেমো ভিডিও টেস্ট",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 2: Deep Metadata & FYP Risk Inspection Report
            if (inspection != null) {
                GlassCard(accentGlowColor = AmberWarning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Risk Alert",
                                tint = AmberWarning,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = inspection.fileName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimarySnow,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${inspection.resolution} • ${inspection.bitrateKbps} kbps • ${(inspection.fileSizeBytes / 1024)} KB",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondarySlate
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RoseDanger.copy(alpha = 0.12f))
                            .border(1.dp, RoseDanger.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "TikTok Status: ${inspection.fypRiskLevel}",
                                style = MaterialTheme.typography.labelLarge,
                                color = RoseDanger
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            inspection.fypRiskReasons.forEach { reason ->
                                Text(
                                    text = "• $reason",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimarySnow
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ডিটেক্ট হওয়া অরিজিনাল হ্যাশ ও কন্টেইনার মেটাডাটা:",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianDeep.copy(alpha = 0.75f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "MD5: ${inspection.originalMd5}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = AmberWarning
                        )
                        Text(
                            text = "SHA-256: ${inspection.originalSha256.take(36)}...",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = TextSecondarySlate
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        inspection.detectedMetadata.forEach { (k, v) ->
                            Text(
                                text = "$k: $v",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = TextPrimarySnow
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Step 3: Anti-Copyright & FYP Safe Engine Controls
            GlassCard(accentGlowColor = VividViolet) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "২. Anti-Copyright ও FYP Safe ইঞ্জিন কনফিগারেশন",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimarySnow
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ScrubberToggleItem(
                    title = "Deep Container Metadata Removal (EXIF / XMP / UDTA)",
                    subtitle = "ভিডিওর ভেতরে থাকা TikTok/Instagram/CapCut আইডি ও পুরনো তারিখ মুছে ফেলবে",
                    checked = fypConfig.stripAllMetadata,
                    onCheckedChange = { onUpdateFypConfig(fypConfig.copy(stripAllMetadata = it)) }
                )

                ScrubberToggleItem(
                    title = "Cryptographic MD5 & SHA-256 Hash Randomizer",
                    subtitle = "প্রতিবার এক্সপোর্টে ১০০% ইউনিক বাইনারি হ্যাশ তৈরি করবে",
                    checked = fypConfig.randomizeMd5Hash,
                    onCheckedChange = { onUpdateFypConfig(fypConfig.copy(randomizeMd5Hash = it)) }
                )

                ScrubberToggleItem(
                    title = "Micro-Frame Trim & Temporal Remap",
                    subtitle = "ভিডিওর ফ্রেম সিকোয়েন্স ফিঙ্গারপ্রিন্ট পরিবর্তন (${fypConfig.trimMillis} ms)",
                    checked = fypConfig.microTrimFrames,
                    onCheckedChange = { onUpdateFypConfig(fypConfig.copy(microTrimFrames = it)) }
                )

                if (fypConfig.microTrimFrames) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(
                            text = "মাইক্রো-ট্রিম অফসেট: ${fypConfig.trimMillis} মিলিসেকেন্ড",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                        Slider(
                            value = fypConfig.trimMillis.toFloat(),
                            onValueChange = {
                                onUpdateFypConfig(fypConfig.copy(trimMillis = it.toLong()))
                            },
                            valueRange = 80f..450f,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricCyan,
                                activeTrackColor = ElectricCyan
                            )
                        )
                    }
                }

                ScrubberToggleItem(
                    title = "Audio Track Sync & Header Rewrite",
                    subtitle = "অডিও কপিরাইট সিগনেচার বাইপাসের জন্য ট্র্যাক টাইমস্কেল রি-রাইট",
                    checked = fypConfig.modifyAudioTrackHeader,
                    onCheckedChange = { onUpdateFypConfig(fypConfig.copy(modifyAudioTrackHeader = it)) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "অরিজিনাল ক্যামেরা ডিভাইস সিগনেচার (Spoof Camera Tag):",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    cameraPresets.forEach { preset ->
                        val selected = fypConfig.customCameraDeviceTag == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) ElectricCyan.copy(alpha = 0.16f)
                                    else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.78f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) ElectricCyan else androidx.compose.ui.graphics.Color.White,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onUpdateFypConfig(fypConfig.copy(customCameraDeviceTag = preset))
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selected) ElectricCyan else TextPrimarySnow
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(listOf(EmeraldSafe, ElectricCyan))
                        )
                        .border(1.dp, GlassBorderBright, RoundedCornerShape(18.dp))
                        .clickable(enabled = !isScrubbing) { onCleanSelectedVideo() }
                        .testTag("execute_fyp_scrub_button")
                ) {
                    if (isScrubbing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = androidx.compose.ui.graphics.Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = scrubProgressMessage,
                                style = MaterialTheme.typography.labelLarge,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Clean Metadata",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "মেটাডাটা রিমুভ ও গ্যালারিতে FYP Safe সেভ করুন",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
            }

            if (latestReport != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ScrubReportVerificationCard(
                    report = latestReport,
                    onPlayVideoClick = onPlayCleanedVideo
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScrubberToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ObsidianDeep.copy(alpha = 0.5f))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = if (checked) EmeraldSafe else TextSecondarySlate,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimarySnow
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondarySlate
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianDeep,
                checkedTrackColor = EmeraldSafe
            )
        )
    }
}
