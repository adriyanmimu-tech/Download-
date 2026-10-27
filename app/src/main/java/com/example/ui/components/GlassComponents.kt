package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SocialPlatform
import com.example.data.VideoScrubReport
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.FrostIceBlueMid
import com.example.ui.theme.FrostSkyBlueEnd
import com.example.ui.theme.FrostWhiteStart
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TextPrimarySnow
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.VividViolet
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    borderColor: Color = Color.White,
    accentGlowColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.96f),
            (accentGlowColor ?: borderColor).copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.75f)
        )
    )

    val surfaceBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.78f),
            Color(0xFFF3F8FF).copy(alpha = 0.62f),
            Color.White.copy(alpha = 0.72f)
        )
    )

    Column(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = Color(0xFF6366F1).copy(alpha = 0.14f),
                spotColor = Color(0xFF3B82F6).copy(alpha = 0.16f)
            )
            .clip(shape)
            .background(brush = surfaceBrush)
            .border(width = 1.5.dp, brush = borderBrush, shape = shape)
            .padding(18.dp),
        content = content
    )
}

private data class FloatingBgIconSpec(
    val platform: SocialPlatform,
    val anchorX: Float,
    val anchorY: Float,
    val radiusX: Float,
    val radiusY: Float,
    val speedMultiplier: Float,
    val phaseOffset: Float,
    val sizeDp: Dp,
    val alpha: Float,
    val blurredBehindPane: Boolean
)

private val backgroundFloatingIcons = listOf(
    FloatingBgIconSpec(SocialPlatform.TIKTOK, 0.16f, 0.14f, 26f, 34f, 1.0f, 0.0f, 58.dp, 0.78f, true),
    FloatingBgIconSpec(SocialPlatform.INSTAGRAM, 0.82f, 0.12f, 30f, 28f, 0.85f, 1.2f, 54.dp, 0.82f, false),
    FloatingBgIconSpec(SocialPlatform.FACEBOOK, 0.18f, 0.42f, 34f, 30f, 0.9f, 2.4f, 62.dp, 0.72f, true),
    FloatingBgIconSpec(SocialPlatform.YOUTUBE, 0.84f, 0.38f, 28f, 36f, 1.1f, 3.1f, 56.dp, 0.80f, false),
    FloatingBgIconSpec(SocialPlatform.X_TWITTER, 0.14f, 0.72f, 30f, 26f, 0.95f, 4.2f, 52.dp, 0.74f, true),
    FloatingBgIconSpec(SocialPlatform.PINTEREST, 0.80f, 0.68f, 32f, 30f, 0.8f, 5.0f, 54.dp, 0.78f, false),
    FloatingBgIconSpec(SocialPlatform.THREADS, 0.46f, 0.24f, 38f, 24f, 0.75f, 1.8f, 50.dp, 0.65f, true),
    FloatingBgIconSpec(SocialPlatform.LIKEE, 0.54f, 0.82f, 34f, 24f, 1.05f, 2.9f, 48.dp, 0.70f, false)
)

/**
 * Full-Screen White & Ice-Blue Frosted Glassmorphism Background (matching the user's reference image)
 * with NO circles/spheres — instead, Original Social Media App Icons float and move across the background
 * behind the frosted glass refraction panes!
 */
@Composable
fun AmbientAuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_floating_app_icons")
    val timeRad by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_rad"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        FrostWhiteStart,
                        FrostIceBlueMid,
                        FrostSkyBlueEnd
                    )
                )
            )
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // Layer 1: Original Social Media App Icons floating across the entire screen background
        backgroundFloatingIcons.forEach { spec ->
            val angle = timeRad * spec.speedMultiplier + spec.phaseOffset
            val dx = (cos(angle.toDouble()).toFloat() * spec.radiusX).dp
            val dy = (sin(angle.toDouble() * 1.3).toFloat() * spec.radiusY).dp
            val baseX = screenW * spec.anchorX + dx - (spec.sizeDp / 2)
            val baseY = screenH * spec.anchorY + dy - (spec.sizeDp / 2)

            OriginalSocialAppIcon(
                platform = spec.platform,
                size = spec.sizeDp,
                modifier = Modifier
                    .offset(x = baseX, y = baseY)
                    .alpha(spec.alpha)
                    .then(
                        if (spec.blurredBehindPane) Modifier.blur(8.dp) else Modifier.blur(2.dp)
                    )
            )
        }

        // Layer 2: Split Frosted Glass Refraction Overlay (Just like the user's reference image, without spheres!)
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Frosted Milky-White Glass Pane (42% width, matching reference image split)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.42f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.64f),
                                Color(0xFFF2F7FF).copy(alpha = 0.52f),
                                Color.White.copy(alpha = 0.60f)
                            )
                        )
                    )
            )
            // Crisp Specular White Glass Divider Line (from reference image)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.5.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color.White.copy(alpha = 0.70f),
                                Color.White.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
            // Right Soft Ice-Blue Glass Pane (58% width)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.58f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE3EEFF).copy(alpha = 0.38f),
                                Color(0xFFD0E3FF).copy(alpha = 0.44f),
                                Color(0xFFE8F1FF).copy(alpha = 0.40f)
                            )
                        )
                    )
            )
        }

        // Layer 3: Foreground App Content
        content()
    }
}

/**
 * Interactive Floating Original App Icons Showcase on the First Page (NO circles/rings!)
 */
@Composable
fun FloatingSocialOrbitHero(
    selectedPlatform: SocialPlatform,
    onPlatformIconClick: (SocialPlatform) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_floating_icons")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    val driftX by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_x"
    )

    val platforms = SocialPlatform.entries

    GlassCard(
        cornerRadius = 28.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("floating_social_orbit_hero")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Supported Social Platforms",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimarySnow
                )
                Text(
                    text = "যেকোনো ভাসমান অরিজিনাল অ্যাপ আইকনে ট্যাপ করুন বা নিচে লিংক পেস্ট করুন",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondarySlate
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2 Rows of continuously floating Original Social Media App Icons (No circles!)
        val firstRow = platforms.take(4)
        val secondRow = platforms.drop(4)

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            firstRow.forEachIndexed { idx, platform ->
                val floatY = (sin((wavePhase + idx * 1.1f).toDouble()).toFloat() * 7f).dp
                val floatX = (cos((wavePhase + idx * 0.9f).toDouble()).toFloat() * (driftX * 0.6f)).dp
                FloatingOriginalAppIconItem(
                    platform = platform,
                    isSelected = platform == selectedPlatform,
                    onClick = { onPlatformIconClick(platform) },
                    modifier = Modifier
                        .offset {
                            IntOffset(x = floatX.roundToPx(), y = floatY.roundToPx())
                        }
                        .testTag("floating_icon_${platform.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            secondRow.forEachIndexed { idx, platform ->
                val floatY = (cos((wavePhase + (idx + 2) * 1.2f).toDouble()).toFloat() * 7f).dp
                val floatX = (sin((wavePhase + idx * 1.0f).toDouble()).toFloat() * (driftX * 0.6f)).dp
                FloatingOriginalAppIconItem(
                    platform = platform,
                    isSelected = platform == selectedPlatform,
                    onClick = { onPlatformIconClick(platform) },
                    modifier = Modifier
                        .offset {
                            IntOffset(x = floatX.roundToPx(), y = floatY.roundToPx())
                        }
                        .testTag("floating_icon_${platform.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
private fun FloatingOriginalAppIconItem(
    platform: SocialPlatform,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .then(
                    if (isSelected) {
                        Modifier
                            .border(
                                width = 2.dp,
                                color = ElectricCyan,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(3.dp)
                    } else {
                        Modifier
                    }
                )
        ) {
            OriginalSocialAppIcon(
                platform = platform,
                size = 50.dp
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = platform.displayName.substringBefore(" "),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (isSelected) ElectricCyan else TextPrimarySnow
        )
    }
}

/**
 * Draws the authentic, original-style Social Media App Icon (Rounded Squircle + Brand Vector Logo)
 */
@Composable
fun OriginalSocialAppIcon(
    platform: SocialPlatform,
    size: Dp = 50.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(size * 0.26f)
    Canvas(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = platform.brandColor.copy(alpha = 0.35f),
                spotColor = platform.brandColor.copy(alpha = 0.45f)
            )
            .clip(shape)
    ) {
        val w = this.size.width
        val h = this.size.height
        val corner = CornerRadius(w * 0.26f, h * 0.26f)

        when (platform) {
            SocialPlatform.TIKTOK -> drawTikTokOriginalIcon(w, h, corner)
            SocialPlatform.INSTAGRAM -> drawInstagramOriginalIcon(w, h, corner)
            SocialPlatform.FACEBOOK -> drawFacebookOriginalIcon(w, h, corner)
            SocialPlatform.YOUTUBE -> drawYouTubeOriginalIcon(w, h, corner)
            SocialPlatform.X_TWITTER -> drawXOriginalIcon(w, h, corner)
            SocialPlatform.PINTEREST -> drawPinterestOriginalIcon(w, h, corner)
            SocialPlatform.THREADS -> drawThreadsOriginalIcon(w, h, corner)
            SocialPlatform.LIKEE -> drawLikeeOriginalIcon(w, h, corner)
        }

        // Subtle glossy top-left app icon specular highlight
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.32f),
                    Color.Transparent
                ),
                start = Offset.Zero,
                end = Offset(w * 0.7f, h * 0.7f)
            ),
            cornerRadius = corner
        )
    }
}

private fun DrawScope.drawTikTokOriginalIcon(w: Float, h: Float, corner: CornerRadius) {
    drawRoundRect(color = Color(0xFF0B0B0F), cornerRadius = corner)

    val strokeW = w * 0.115f
    // Cyan shifted note
    drawTikTokNoteGlyph(
        cx = w * 0.46f,
        cy = h * 0.51f,
        scale = w,
        color = Color(0xFF25F4EE),
        strokeW = strokeW
    )
    // Red-pink shifted note
    drawTikTokNoteGlyph(
        cx = w * 0.52f,
        cy = h * 0.55f,
        scale = w,
        color = Color(0xFFFE2C55),
        strokeW = strokeW
    )
    // Center white note
    drawTikTokNoteGlyph(
        cx = w * 0.49f,
        cy = h * 0.53f,
        scale = w,
        color = Color.White,
        strokeW = strokeW
    )
}

private fun DrawScope.drawTikTokNoteGlyph(
    cx: Float,
    cy: Float,
    scale: Float,
    color: Color,
    strokeW: Float
) {
    val path = Path().apply {
        moveTo(cx + scale * 0.06f, cy - scale * 0.26f)
        lineTo(cx + scale * 0.06f, cy + scale * 0.08f)
        cubicTo(
            cx + scale * 0.06f, cy + scale * 0.22f,
            cx - scale * 0.16f, cy + scale * 0.22f,
            cx - scale * 0.16f, cy + scale * 0.09f
        )
        cubicTo(
            cx - scale * 0.16f, cy - scale * 0.02f,
            cx - scale * 0.02f, cy - scale * 0.04f,
            cx + scale * 0.01f, cy - scale * 0.02f
        )
        moveTo(cx + scale * 0.06f, cy - scale * 0.24f)
        cubicTo(
            cx + scale * 0.09f, cy - scale * 0.13f,
            cx + scale * 0.16f, cy - scale * 0.09f,
            cx + scale * 0.23f, cy - scale * 0.08f
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawInstagramOriginalIcon(w: Float, h: Float, corner: CornerRadius) {
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF4F5BD5),
                Color(0xFF962FBF),
                Color(0xFFD62976),
                Color(0xFFFA7E1E),
                Color(0xFFFEDA75)
            ),
            start = Offset(w * 0.85f, 0f),
            end = Offset(w * 0.15f, h)
        ),
        cornerRadius = corner
    )
    val pad = w * 0.22f
    val strokeW = w * 0.075f
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(pad, pad),
        size = Size(w - pad * 2, h - pad * 2),
        cornerRadius = CornerRadius(w * 0.16f, h * 0.16f),
        style = Stroke(width = strokeW)
    )
    drawCircle(
        color = Color.White,
        radius = w * 0.13f,
        center = Offset(w * 0.5f, h * 0.5f),
        style = Stroke(width = strokeW)
    )
    drawCircle(
        color = Color.White,
        radius = w * 0.038f,
        center = Offset(w * 0.65f, h * 0.35f)
    )
}

private fun DrawScope.drawFacebookOriginalIcon(w: Float, h: Float, corner: CornerRadius) {
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF18ACFE), Color(0xFF0163E0))
        ),
        cornerRadius = corner
    )
    val fPath = Path().apply {
        moveTo(w * 0.58f, h * 0.88f)
        lineTo(w * 0.58f, h * 0.56f)
        lineTo(w * 0.70f, h * 0.56f)
        lineTo(w * 0.72f, h * 0.43f)
        lineTo(w * 0.58f, h * 0.43f)
        lineTo(w * 0.58f, h * 0.35f)
        cubicTo(w * 0.58f, h * 0.30f, w * 0.61f, h * 0.28f, w * 0.67f, h * 0.28f)
        lineTo(w * 0.73f, h * 0.28f)
        lineTo(w * 0.73f, h * 0.17f)
        lineTo(w * 0.62f, h * 0.17f)
        cubicTo(w * 0.49f, h * 0.17f, w * 0.44f, h * 0.24f, w * 0.44f, h * 0.35f)
        lineTo(w * 0.44f, h * 0.43f)
        lineTo(w * 0.34f, h * 0.43f)
        lineTo(w * 0.34f, h * 0.56f)
        lineTo(w * 0.44f, h * 0.56f)
        lineTo(w * 0.44f, h * 0.88f)
        close()
    }
    drawPath(path = fPath, color = Color.White)
}

private fun DrawScope.drawYouTubeOriginalIcon(w: Float, h: Float, corner: CornerRadius) {
    drawRoundRect(color = Color.White, cornerRadius = corner)
    val padX = w * 0.15f
    val padY = h * 0.25f
    drawRoundRect(
        color = Color(0xFFFF0000),
        topLeft = Offset(padX, padY),
        size = Size(w - padX * 2, h - padY * 2),
        cornerRadius = CornerRadius(w * 0.14f, h * 0.14f)
    )
    val playPath = Path().apply {
        moveTo(w * 0.43f, h * 0.39f)
        lineTo(w * 0.62f, h * 0.50f)
        lineTo(w * 0.43f, h * 0.61f)
        close()
    }
    drawPath(path = playPath, color = Color.White)
}

private fun DrawScope.drawXOriginalIcon(w: Float, h: Float, corner: CornerRadius) {
    drawRoundRect(color = Color(0xFF0F1419), cornerRadius = corner)
    val strokeW = w * 0.09f
    drawLine(
        color = Color.White,
        start = Offset(w * 0.28f, h * 0.26f),
        end = Offset(w * 0.72f, h * 0.74f),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White,
        start = Offset(w * 0.72f, h * 0.26f),
        end = Offset(w * 0.28f, h * 0.74f),
        strokeWidth = strokeW * 0.75f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawPinterestOriginalIcon(w: Float, h: Float, corner: CornerRadius) {
    drawRoundRect(color = Color(0xFFE60023), cornerRadius = corner)
    val strokeW = w * 0.095f
    drawLine(
        color = Color.White,
        start = Offset(w * 0.44f, h * 0.76f),
        end = Offset(w * 0.48f, h * 0.34f),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = Color.White,
        radius = w * 0.16f,
        center = Offset(w * 0.53f, h * 0.43f),
        style = Stroke(width = strokeW)
    )
}

private fun DrawScope.drawThreadsOriginalIcon(w: Float, h: Float, corner: CornerRadius) {
    drawRoundRect(color = Color(0xFF101014), cornerRadius = corner)
    val strokeW = w * 0.075f
    drawCircle(
        color = Color.White,
        radius = w * 0.24f,
        center = Offset(w * 0.5f, h * 0.5f),
        style = Stroke(width = strokeW)
    )
    drawCircle(
        color = Color.White,
        radius = w * 0.10f,
        center = Offset(w * 0.5f, h * 0.5f),
        style = Stroke(width = strokeW)
    )
}

private fun DrawScope.drawLikeeOriginalIcon(w: Float, h: Float, corner: CornerRadius) {
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B), Color(0xFFFFB75E))
        ),
        cornerRadius = corner
    )
    val playPath = Path().apply {
        moveTo(w * 0.38f, h * 0.32f)
        lineTo(w * 0.68f, h * 0.50f)
        lineTo(w * 0.38f, h * 0.68f)
        close()
    }
    drawPath(path = playPath, color = Color.White)
}

fun platformIconVector(platform: SocialPlatform): ImageVector {
    return when (platform) {
        SocialPlatform.TIKTOK -> Icons.Default.MusicNote
        SocialPlatform.INSTAGRAM -> Icons.Default.CameraAlt
        SocialPlatform.FACEBOOK -> Icons.Default.Public
        SocialPlatform.X_TWITTER -> Icons.Default.AlternateEmail
        SocialPlatform.YOUTUBE -> Icons.Default.SmartDisplay
        SocialPlatform.PINTEREST -> Icons.Default.PushPin
        SocialPlatform.THREADS -> Icons.Default.AutoAwesome
        SocialPlatform.LIKEE -> Icons.Default.PlayCircleFilled
    }
}

@Composable
fun ScrubReportVerificationCard(
    report: VideoScrubReport,
    onPlayVideoClick: () -> Unit,
    onPostToTikTokClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        accentGlowColor = EmeraldSafe
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "FYP Safe Verified",
                    tint = EmeraldSafe,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "TikTok FYP Safe & গ্যালারিতে সেভ হয়েছে!",
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldSafe
                    )
                    Text(
                        text = "${report.platformName} • ${report.qualityLabel} • Safety Score: ${report.fypSafetyScore}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondarySlate
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianDeep.copy(alpha = 0.75f))
                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "OLD HASH (Flagged / Duplicate Risk):",
                style = MaterialTheme.typography.labelSmall,
                color = RoseDanger
            )
            Text(
                text = report.originalMd5,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = TextSecondarySlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "NEW FYP-SAFE HASH (100% Unique Container):",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldSafe
            )
            Text(
                text = report.cleanedMd5,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = TextPrimarySnow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "রিমুভ করা মেটাডাটা ও কপিরাইট প্রোটেকশন (${report.removedTagsCount}টি ট্যাগ ক্লিন):",
            style = MaterialTheme.typography.labelLarge,
            color = ElectricCyan
        )
        Spacer(modifier = Modifier.height(4.dp))
        report.appliedProtections.forEach { item ->
            Text(
                text = "✓ $item",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimarySnow
            )
        }

        if (onPostToTikTokClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00F2FE),
                                Color(0xFF161823),
                                Color(0xFFFF0050)
                            )
                        )
                    )
                    .clickable(onClick = onPostToTikTokClick)
                    .padding(vertical = 12.dp)
                    .testTag("direct_post_tiktok_button")
            ) {
                OriginalSocialAppIcon(
                    platform = SocialPlatform.TIKTOK,
                    size = 20.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🚀 TikTok-এ ১-ক্লিকে পোস্ট করুন (ক্যাপশন রেডি)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(listOf(ElectricCyan, VividViolet))
                )
                .clickable(onClick = onPlayVideoClick)
                .padding(vertical = 12.dp)
                .testTag("play_latest_saved_video_button")
        ) {
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = "Play Saved Video",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "সেভ হওয়া ভিডিওটি প্লে করুন (In-App Preview)",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}
