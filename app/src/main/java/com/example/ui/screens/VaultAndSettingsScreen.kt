package com.example.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ExtractorApiSettings
import com.example.data.VaultVideoEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.AbyssalBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TextPrimarySnow
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.VividViolet
import java.io.File

@Composable
fun VaultHistoryScreen(
    videos: List<VaultVideoEntity>,
    onPlayVideo: (VaultVideoEntity) -> Unit,
    onCopyCaption: (VaultVideoEntity) -> Unit,
    onShareVideo: (VaultVideoEntity) -> Unit,
    onDeleteVideo: (Int) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
        ) {
            Text(
                text = "গ্যালারি ভল্ট ও ডাউনলোড হিস্ট্রি",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimarySnow
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ডাউনলোড ও মেটাডাটা ক্লিন করা সব ভিডিও আপনার ফোনের গ্যালারিতে (Movies/PulseSave) এবং এখানে সংরক্ষিত আছে।",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (videos.isEmpty()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_empty_state_card")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Empty Vault",
                            tint = ElectricCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "এখনো কোনো ভিডিও সেভ করা হয়নি",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimarySnow
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ডাউনলোডার পেইজ থেকে ভিডিও লিংক পেস্ট করুন অথবা FYP ক্লিনার দিয়ে গ্যালারির ভিডিও ক্লিন করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("vault_video_list")
                ) {
                    items(videos, key = { it.id }) { item ->
                        VaultVideoCard(
                            video = item,
                            onPlay = { onPlayVideo(item) },
                            onCopyCaption = { onCopyCaption(item) },
                            onShare = { onShareVideo(item) },
                            onDelete = { onDeleteVideo(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultVideoCard(
    video: VaultVideoEntity,
    onPlay: () -> Unit,
    onCopyCaption: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        accentGlowColor = if (video.isFypSafe) EmeraldSafe else ElectricCyan,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vault_item_${video.id}")
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ElectricCyan, VividViolet)))
                        .clickable(onClick = onPlay)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimarySnow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${video.platformName} • ${video.qualityLabel} • ${(video.fileSizeBytes / 1024)} KB",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricCyan
                    )
                }
            }

            if (video.isFypSafe) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldSafe.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSafe,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "FYP ${video.fypSafetyScore}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSafe
                    )
                }
            }
        }

        if (video.caption.isNotBlank() || video.hashtags.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${video.caption} ${video.hashtags}".trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hash Proof Pill
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianDeep.copy(alpha = 0.6f))
                .padding(8.dp)
        ) {
            Text(
                text = "Old MD5: ${video.originalMd5.take(22)}...",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = RoseDanger
            )
            Text(
                text = "New MD5: ${video.cleanedMd5.take(22)}... (${video.removedTagsCount} tags removed)",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = EmeraldSafe
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElectricCyan.copy(alpha = 0.16f))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onPlay)
                    .padding(vertical = 8.dp)
                    .testTag("vault_play_button_${video.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "প্লে করুন",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AbyssalBlue)
                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                    .clickable(onClick = onCopyCaption)
                    .padding(vertical = 8.dp)
                    .testTag("vault_copy_button_${video.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Caption",
                    tint = TextPrimarySnow,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ক্যাপশন কপি",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimarySnow
                )
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Video",
                    tint = ElectricCyan
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Video",
                    tint = RoseDanger
                )
            }
        }
    }
}

@Composable
fun ApiAndSettingsScreen(
    currentSettings: ExtractorApiSettings,
    onSaveSettings: (String, String, String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    var endpoint by remember(currentSettings) { mutableStateOf(currentSettings.customApiEndpoint) }
    var apiKey by remember(currentSettings) { mutableStateOf(currentSettings.customApiKey) }
    var cameraSig by remember(currentSettings) { mutableStateOf(currentSettings.defaultCameraSignature) }

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
                text = "এক্সট্র্যাক্টর API ও FYP সেটিংস",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimarySnow
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "বিল্ট-ইন ওপেন এক্সট্র্যাক্টরের পাশাপাশি আপনি চাইলে নিজস্ব Cobalt / Custom Extractor API সার্ভারও যুক্ত করতে পারেন।",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate
            )

            Spacer(modifier = Modifier.height(14.dp))

            GlassCard(accentGlowColor = ElectricCyan) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SettingsInputComponent,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Custom Video Extractor API (ঐচ্ছিক)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimarySnow
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("Custom Extractor Endpoint URL (e.g. Cobalt JSON API)") },
                    placeholder = { Text("https://api.your-extractor.com/api/json") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = GlassBorderSubtle,
                        focusedTextColor = TextPrimarySnow,
                        unfocusedTextColor = TextPrimarySnow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_api_endpoint_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key / Bearer Token (ঐচ্ছিক)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = GlassBorderSubtle,
                        focusedTextColor = TextPrimarySnow,
                        unfocusedTextColor = TextPrimarySnow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_api_key_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cameraSig,
                    onValueChange = { cameraSig = it },
                    label = { Text("Default Spoofed Camera Device Tag") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividViolet,
                        unfocusedBorderColor = GlassBorderSubtle,
                        focusedTextColor = TextPrimarySnow,
                        unfocusedTextColor = TextPrimarySnow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_camera_sig_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(ElectricCyan, VividViolet)))
                        .clickable { onSaveSettings(endpoint, apiKey, cameraSig) }
                        .testTag("save_api_settings_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Settings",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সেটিংস সেভ করুন",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InAppVideoPlayerDialog(
    title: String,
    videoFilePath: String,
    galleryUriString: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MidnightIndigo)
                .border(1.5.dp, ElectricCyan, RoundedCornerShape(24.dp))
                .padding(16.dp)
                .testTag("in_app_video_player_dialog")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimarySnow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Player",
                        tint = TextPrimarySnow
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianDeep)
            ) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val controller = MediaController(ctx)
                            controller.setAnchorView(this)
                            setMediaController(controller)
                            val localFile = File(videoFilePath)
                            if (localFile.exists()) {
                                setVideoPath(localFile.absolutePath)
                            } else if (galleryUriString.isNotBlank()) {
                                setVideoURI(Uri.parse(galleryUriString))
                            }
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                start()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "✓ গ্যালারিতে (Movies/PulseSave) সংরক্ষিত ও ১০০% TikTok FYP Safe",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldSafe
            )
        }
    }
}
