package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ViralSeoBank
import com.example.ui.components.GlassCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.TextPrimarySnow
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.VividViolet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ViralSeoScreen(
    onCopyText: (String, String) -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val currentPack = ViralSeoBank.hashtagPacks[selectedCategoryIndex]
    val bestTimeAdvice = remember { ViralSeoBank.getBestPostingTimeAdvice() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 90.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Screen Title Banner
        GlassCard(accentGlowColor = ElectricCyan) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricCyan.copy(alpha = 0.16f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Viral Studio",
                        tint = ElectricCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ভাইরাল FYP স্টুডিও (Viral Studio)",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimarySnow
                    )
                    Text(
                        text = "ট্রেন্ডিং হ্যাশট্যাগ, হুক টাইটেল ও ভাইরাল আপলোড গাইড",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondarySlate
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 1: Real-time Best Posting Time Advice
        GlassCard(accentGlowColor = EmeraldSafe) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Best Posting Time",
                    tint = EmeraldSafe,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "পোস্ট করার সেরা সময় (Best Posting Windows)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimarySnow
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmeraldSafe.copy(alpha = 0.12f))
                    .border(1.dp, EmeraldSafe.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = bestTimeAdvice,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimarySnow
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "💡 টিপস: ভিডিও পোস্ট করার সাথে সাথে প্রথম ৩০ মিনিট কাউকে দিয়ে ফুল ওয়াচ ও শেয়ার করালে TikTok অ্যালগরিদম ভিডিওটিকে দ্রুত For You-তে বুস্ট করে।",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Trending Hashtag Bank
        GlassCard(accentGlowColor = VividViolet) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = "Hashtags",
                        tint = VividViolet,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ট্রেন্ডিং ভাইরাল হ্যাশট্যাগ ব্যাংক",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimarySnow
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(VividViolet.copy(alpha = 0.15f))
                        .border(1.dp, VividViolet.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .clickable {
                            val allTags = currentPack.hashtags.joinToString(" ")
                            onCopyText(allTags, "${currentPack.categoryName}-এর সব হ্যাশট্যাগ কপি করা হয়েছে!")
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("copy_all_hashtags_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy All",
                        tint = VividViolet,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "সবগুলো কপি",
                        style = MaterialTheme.typography.labelSmall,
                        color = VividViolet
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category selector chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                ViralSeoBank.hashtagPacks.forEachIndexed { idx, pack ->
                    val isSelected = idx == selectedCategoryIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) VividViolet.copy(alpha = 0.22f)
                                else Color.White.copy(alpha = 0.70f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) VividViolet else Color.White,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedCategoryIndex = idx }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${pack.iconEmoji} ${pack.categoryName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) VividViolet else TextPrimarySnow
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hashtag Pills (Tap individual hashtag to copy)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                currentPack.hashtags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            .clickable {
                                onCopyText(tag, "$tag কপি করা হয়েছে!")
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ElectricCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 3: High-CTR Psychological Hooks Bank
        GlassCard(accentGlowColor = ElectricCyan) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Hooks",
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "হাই-সিটিআর ভাইরাল হুক টাইটেল",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimarySnow
                )
            }
            Text(
                text = "ভিডিওর প্রথম ৩ সেকেন্ডে দর্শককে ধরে রাখার জন্য নিচের টাইটেলগুলো ব্যবহার করুন:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate
            )

            Spacer(modifier = Modifier.height(10.dp))

            ViralSeoBank.viralHooks.forEach { hook ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.75f))
                        .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                        .clickable {
                            onCopyText(hook.title, "\"${hook.title}\" কপি করা হয়েছে!")
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = hook.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimarySnow
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${hook.category} • ${hook.psychologicalTrigger}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Hook",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 4: 100% FYP Viral Checklist
        GlassCard(accentGlowColor = EmeraldSafe) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Checklist",
                    tint = EmeraldSafe,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "১০০% FYP ভাইরাল আপলোড চেকলিস্ট",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimarySnow
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val checklistItems = listOf(
                "MD5 ও SHA-256 ক্রিপ্টোগ্রাফিক হ্যাশ ১০০% রেন্ডমাইজড হয়েছে (PulseSave দ্বারা স্বয়ংক্রিয়)",
                "লুকানো UDTA, Lavf, ও CapCut মেটাডাটা বক্স ক্লিন করা হয়েছে",
                "1.02x মাইক্রো-স্পিড দিয়ে অডিও Content-ID ও সাউন্ড কপিরাইট বাইপাস একটিভ",
                "ভিডিও শুরুর ০.৪ সেকেন্ড ট্রিম করা হয়েছে যাতে ওপেনিং ফ্রেম ম্যাচিং না ধরে",
                "ভিডিওর কোয়ালিটি 1080p HD বা 720p 60fps-এ সেভ করা হয়েছে",
                "ক্যাপশনে কৌতূহল জাগানো প্রশ্ন ও ৩-৫টি ক্যাটাগরি হ্যাশট্যাগ যুক্ত করা হয়েছে",
                "পিক আওয়ারে (সন্ধ্যা ৮:০০ PM - ১০:৩০ PM) পোস্ট করা হচ্ছে"
            )

            checklistItems.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSafe,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimarySnow
                    )
                }
            }
        }
    }
}
