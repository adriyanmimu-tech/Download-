package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SocialPlatform
import com.example.data.VideoScrubReport
import com.example.ui.components.FloatingSocialOrbitHero
import com.example.ui.components.GlassCard
import com.example.ui.components.OriginalSocialAppIcon
import com.example.ui.components.ScrubReportVerificationCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.TextPrimarySnow
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.VividViolet

@Composable
fun DownloaderScreen(
    urlInput: String,
    detectedPlatform: SocialPlatform,
    isAnalyzing: Boolean,
    isDownloadingAndScrubbing: Boolean,
    downloadProgress: Float,
    progressStepMessage: String,
    latestScrubReport: VideoScrubReport?,
    onUrlChange: (String) -> Unit,
    onPasteFromClipboard: () -> Unit,
    onSelectSamplePlatform: (SocialPlatform) -> Unit,
    onClickDownloadButton: () -> Unit,
    onPlayLatestVideo: () -> Unit,
    onNavigateToLocalScrubber: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            // Editorial Glass Morphism Header (Matching the clean typography in the user's screenshot)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "PulseSave",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimarySnow
                    )
                    Text(
                        text = "All Social Video Saver & TikTok FYP Safe Engine",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondarySlate
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.78f))
                        .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "FYP Safe Active",
                            tint = EmeraldSafe,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "FYP SAFE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldSafe
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main First-Page Frosted White Glass Card: Video Link Paste + Download Button
            GlassCard(
                accentGlowColor = ElectricCyan,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_downloader_glass_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ভিডিও লিংক পেস্ট করুন",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimarySnow
                        )
                    }

                    // Detected Original App Icon Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        OriginalSocialAppIcon(
                            platform = detectedPlatform,
                            size = 20.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = detectedPlatform.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimarySnow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    placeholder = {
                        Text(
                            text = "TikTok, Instagram, Facebook, YouTube বা যেকোনো ভিডিও লিংক পেস্ট করুন…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                    },
                    singleLine = true,
                    trailingIcon = {
                        if (urlInput.isNotBlank()) {
                            IconButton(
                                onClick = { onUrlChange("") },
                                modifier = Modifier.testTag("clear_link_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Link",
                                    tint = TextSecondarySlate
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.88f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.72f),
                        focusedTextColor = TextPrimarySnow,
                        unfocusedTextColor = TextPrimarySnow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_url_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Paste from Clipboard + Platform Quick Chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricCyan.copy(alpha = 0.14f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .clickable(onClick = onPasteFromClipboard)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("paste_clipboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste Clipboard",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ক্লিপবোর্ড পেস্ট",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                    }

                    SocialPlatform.entries.forEach { platform ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.78f))
                                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                .clickable { onSelectSamplePlatform(platform) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("sample_chip_${platform.name.lowercase()}")
                        ) {
                            OriginalSocialAppIcon(platform = platform, size = 18.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = platform.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimarySnow
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Download Button -> Opens Quality Selection & Caption Popup Menu
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(ElectricCyan, VividViolet)
                            )
                        )
                        .border(1.dp, GlassBorderBright, RoundedCornerShape(18.dp))
                        .clickable(enabled = !isAnalyzing && !isDownloadingAndScrubbing) {
                            onClickDownloadButton()
                        }
                        .testTag("primary_download_button")
                ) {
                    if (isAnalyzing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ভিডিও কোয়ালিটি ও ক্যাপশন লোড হচ্ছে…",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Video",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ডাউনলোড করুন (কোয়ালিটি ও ক্যাপশন মেনু)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Floating Original Social Media App Icons Showcase (Moving smoothly, no circles!)
            FloatingSocialOrbitHero(
                selectedPlatform = detectedPlatform,
                onPlatformIconClick = onSelectSamplePlatform
            )

            // Active Download & Metadata Scrubbing Progress Card
            AnimatedVisibility(visible = isDownloadingAndScrubbing) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    GlassCard(accentGlowColor = VividViolet) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = ElectricCyan,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = progressStepMessage,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimarySnow
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}% সম্পন্ন • মেটাডাটা রিমুভ ও হ্যাশ পরিবর্তন চলছে",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ElectricCyan
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            color = ElectricCyan,
                            trackColor = ObsidianDeep,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            // Latest Scrubbed & Saved Video Report Card
            if (latestScrubReport != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ScrubReportVerificationCard(
                    report = latestScrubReport,
                    onPlayVideoClick = onPlayLatestVideo
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Anti-Copyright & Local Video Scrubber Glass Card
            GlassCard(
                accentGlowColor = VividViolet,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToLocalScrubber)
                    .testTag("goto_local_scrubber_banner")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(listOf(ElectricCyan, VividViolet))
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "FYP Safe Shield",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TikTok 'Not Eligible For You' ও কপিরাইট ক্লিনার",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimarySnow
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "গ্যালারির যেকোনো ডাউনলোড করা ভিডিওর মেটাডাটা, সোর্স ট্যাগ ও MD5 হ্যাশ ক্লিন করে কপিরাইটমুক্ত রি-আপলোড করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "গ্যালারি থেকে ভিডিও ক্লিন করুন →",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
