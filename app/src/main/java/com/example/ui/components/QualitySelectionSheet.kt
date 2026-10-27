package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FypOptimizationConfig
import com.example.data.VideoAnalysisResult
import com.example.data.VideoQualityOption
import com.example.ui.theme.AbyssalBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.TextPrimarySnow
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.VividViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualitySelectionPopupSheet(
    analysis: VideoAnalysisResult,
    selectedQuality: VideoQualityOption,
    editableTitle: String,
    editableCaption: String,
    editableHashtags: String,
    fypConfig: FypOptimizationConfig,
    onTitleChange: (String) -> Unit,
    onCaptionChange: (String) -> Unit,
    onHashtagsChange: (String) -> Unit,
    onSelectQuality: (VideoQualityOption) -> Unit,
    onUpdateFypConfig: (FypOptimizationConfig) -> Unit,
    onCopyTitleAndCaption: () -> Unit,
    onConfirmDownloadAndSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightIndigo,
        contentColor = TextPrimarySnow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("quality_selection_popup_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Popup Header with Original Social App Icon & Close button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OriginalSocialAppIcon(
                        platform = analysis.platform,
                        size = 42.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${analysis.platform.displayName} ভিডিও রেডি!",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimarySnow
                        )
                        Text(
                            text = "${analysis.authorHandle} • ডিউরেশন: ${analysis.durationSec}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElectricCyan
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_quality_popup_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Popup",
                        tint = TextSecondarySlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 1: Video Title, Caption & Hashtags (Editable + 1-Tap Copy)
            GlassCard(
                cornerRadius = 20.dp,
                accentGlowColor = ElectricCyan
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "১. টাইটেল, ক্যাপশন ও হ্যাশট্যাগ",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan.copy(alpha = 0.14f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onCopyTitleAndCaption)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("copy_caption_popup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Caption",
                            tint = ElectricCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "কপি করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = editableTitle,
                    onValueChange = onTitleChange,
                    label = { Text("ভিডিও টাইটেল (Title)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = GlassBorderSubtle,
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.70f),
                        focusedTextColor = TextPrimarySnow,
                        unfocusedTextColor = TextPrimarySnow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("popup_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editableCaption,
                    onValueChange = onCaptionChange,
                    label = { Text("ক্যাপশন (Caption)") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = GlassBorderSubtle,
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.70f),
                        focusedTextColor = TextPrimarySnow,
                        unfocusedTextColor = TextPrimarySnow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("popup_caption_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ভাইরাল হ্যাশট্যাগ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondarySlate
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(VividViolet.copy(alpha = 0.12f))
                            .border(1.dp, VividViolet.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .clickable {
                                val viralTags = "#fyp #foryou #viral #trending #reels #shorts"
                                val newTags = if (editableHashtags.isBlank()) viralTags else "$editableHashtags $viralTags"
                                onHashtagsChange(newTags)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("append_viral_tags_button")
                    ) {
                        Text(
                            text = "🔥 +ভাইরাল ট্যাগ যোগ করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = VividViolet
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = editableHashtags,
                    onValueChange = onHashtagsChange,
                    label = { Text("হ্যাশট্যাগসমূহ (Hashtags)") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividViolet,
                        unfocusedBorderColor = GlassBorderSubtle,
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.70f),
                        focusedTextColor = ElectricCyan,
                        unfocusedTextColor = ElectricCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("popup_hashtags_input")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 2: Video Quality Selection
            Text(
                text = "২. ভিডিও কোয়ালিটি সিলেক্ট করুন",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimarySnow
            )
            Spacer(modifier = Modifier.height(8.dp))

            analysis.qualities.forEach { option ->
                val isSelected = option.id == selectedQuality.id
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) ElectricCyan.copy(alpha = 0.14f)
                            else Color.White.copy(alpha = 0.78f)
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) ElectricCyan else Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectQuality(option) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("quality_option_${option.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = option.label,
                            tint = if (isSelected) ElectricCyan else TextSecondarySlate,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimarySnow
                            )
                            Text(
                                text = "${option.resolution} • ${option.bitrateKbps} kbps • ~${option.estimatedSizeMb} MB",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondarySlate
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (option.isAudioOnly) VividViolet.copy(alpha = 0.16f)
                                else EmeraldSafe.copy(alpha = 0.16f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (option.isAudioOnly) Icons.Default.Audiotrack else Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = if (option.isAudioOnly) VividViolet else EmeraldSafe,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = option.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (option.isAudioOnly) VividViolet else EmeraldSafe
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 3: Anti-Copyright & TikTok "Not Eligible for For You" Bypass Engine
            val currentScore = fypConfig.calculateScore()
            GlassCard(
                cornerRadius = 20.dp,
                accentGlowColor = if (currentScore >= 95) EmeraldSafe else VividViolet
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Anti-Copyright Shield",
                            tint = EmeraldSafe,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "৩. TikTok FYP Safe মোড",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimarySnow
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldSafe.copy(alpha = 0.15f))
                                        .border(1.dp, EmeraldSafe.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$currentScore% FYP READY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldSafe,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "রি-আপলোডে কপিরাইট ও 'Not Eligible For You' আসবে না",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldSafe
                            )
                        }
                    }
                    Switch(
                        checked = fypConfig.enabled,
                        onCheckedChange = { onUpdateFypConfig(fypConfig.copy(enabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldSafe
                        ),
                        modifier = Modifier.testTag("popup_fyp_master_switch")
                    )
                }

                if (fypConfig.enabled) {
                    Spacer(modifier = Modifier.height(10.dp))

                    FypOptionToggleRow(
                        title = "Deep MP4 Metadata & Origin Tag Strip (udta/meta)",
                        subtitle = "TikTok/Reels/CapCut-এর লুকানো সোর্স আইডি ও এনকোডার ট্যাগ মুছে ফেলবে",
                        checked = fypConfig.stripAllMetadata,
                        onCheckedChange = { onUpdateFypConfig(fypConfig.copy(stripAllMetadata = it)) }
                    )
                    FypOptionToggleRow(
                        title = "100% নতুন MD5 ও SHA-256 ক্রিপ্টোগ্রাফিক হ্যাশ",
                        subtitle = "ডুপ্লিকেট ফাইল ডিটেকশন এড়াতে নতুন বাইনারি কন্টেইনার সিগনেচার",
                        checked = fypConfig.randomizeMd5Hash,
                        onCheckedChange = { onUpdateFypConfig(fypConfig.copy(randomizeMd5Hash = it)) }
                    )
                    FypOptionToggleRow(
                        title = "1.02x মাইক্রো-স্পিড (অডিও Content-ID ও সাউন্ড বাইপাস)",
                        subtitle = "ভিডিও ও অডিওকে ২% ফাস্ট করে স্বয়ংক্রিয় অডিও মিউট ও কপিরাইট ব্লক ঠেকাবে",
                        checked = fypConfig.enableMicroSpeed,
                        onCheckedChange = { onUpdateFypConfig(fypConfig.copy(enableMicroSpeed = it)) }
                    )
                    FypOptionToggleRow(
                        title = "1.03x মাইক্রো-জুম ও ফ্রেম শিফট (ভিজ্যুয়াল pHash বাইপাস)",
                        subtitle = "পিক্সেল কোঅর্ডিনেট পরিবর্তন করে AI ভিডিও ফ্রেম ম্যাচিং বাইপাস করবে",
                        checked = fypConfig.enableMicroZoom,
                        onCheckedChange = { onUpdateFypConfig(fypConfig.copy(enableMicroZoom = it)) }
                    )
                    FypOptionToggleRow(
                        title = "০.৪ সেকেন্ড ইন্ট্রো হুক ট্রিম (ভিডিও শুরুর রি-আপলোড স্ক্যানার বাইপাস)",
                        subtitle = "অ্যালগরিদম প্রথম ফ্রেম স্ক্যান করে—শুরু ট্রিম করলে ফ্রেশ ভিডিও মনে করবে",
                        checked = fypConfig.enableIntroTrim,
                        onCheckedChange = { onUpdateFypConfig(fypConfig.copy(enableIntroTrim = it)) }
                    )
                    FypOptionToggleRow(
                        title = "হরিজন্টাল মিরর ফ্লিপ (ডান-বাম উল্টানো)",
                        subtitle = "ভিডিও ডানে-বামে ফ্লিপ করে মেটা ও টিকটক রি-আপলোড ফিল্টারকে বিভ্রান্ত করবে",
                        checked = fypConfig.mirrorHorizontal,
                        onCheckedChange = { onUpdateFypConfig(fypConfig.copy(mirrorHorizontal = it)) }
                    )
                    FypOptionToggleRow(
                        title = "অরিজিনাল অডিও মিউট করুন (Mute)",
                        subtitle = "টিকটকের ট্রেন্ডিং মিউজিক অ্যাড করার জন্য অডিও রিমুভ করে রাখবে",
                        checked = fypConfig.muteAudioTrack,
                        onCheckedChange = { onUpdateFypConfig(fypConfig.copy(muteAudioTrack = it)) }
                    )
                    FypOptionToggleRow(
                        title = "iPhone 16 Pro Max ক্যামেরা মেটাডাটা সিগনেচার ইনজেক্ট",
                        subtitle = "অ্যালগরিদম ভিডিওটিকে সরাসরি ক্যামেরায় ধারণ করা নতুন ভিডিও ভাববে",
                        checked = fypConfig.injectFreshCreationTimestamp,
                        onCheckedChange = { onUpdateFypConfig(fypConfig.copy(injectFreshCreationTimestamp = it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Confirm Download & Save to Gallery Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(ElectricCyan, VividViolet)
                        )
                    )
                    .border(1.dp, GlassBorderBright, RoundedCornerShape(18.dp))
                    .clickable(onClick = onConfirmDownloadAndSave)
                    .testTag("confirm_download_save_gallery_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Save to Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (fypConfig.enabled) {
                            "মেটাডাটা ক্লিন করে গ্যালারিতে সেভ করুন (${selectedQuality.resolution})"
                        } else {
                            "গ্যালারিতে ডাউনলোড ও সেভ করুন (${selectedQuality.resolution})"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun FypOptionToggleRow(
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
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, Color.White, RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = if (checked) ElectricCyan else TextSecondarySlate,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
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
                checkedThumbColor = Color.White,
                checkedTrackColor = ElectricCyan
            )
        )
    }
}
