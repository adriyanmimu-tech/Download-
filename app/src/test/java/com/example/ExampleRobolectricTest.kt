package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FypOptimizationConfig
import com.example.data.SocialPlatform
import com.example.engine.FypMediaScrubberEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun readAppNameFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PulseSave", appName)
    }

    @Test
    fun socialPlatformDetectionIdentifiesTikTokInstagramAndFacebook() {
        assertEquals(
            SocialPlatform.TIKTOK,
            SocialPlatform.detectFromUrl("https://www.tiktok.com/@user/video/123456")
        )
        assertEquals(
            SocialPlatform.INSTAGRAM,
            SocialPlatform.detectFromUrl("https://www.instagram.com/reel/AbCdEfG/")
        )
        assertEquals(
            SocialPlatform.FACEBOOK,
            SocialPlatform.detectFromUrl("https://fb.watch/xYz123/")
        )
    }

    @Test
    fun fypMediaScrubberAltersFileMd5HashAndStripsContainerTags() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val tempVideo = File(context.cacheDir, "test_source.mp4")
            FypMediaScrubberEngine.generateValidPlayableSampleMp4(tempVideo)

            val report = FypMediaScrubberEngine.scrubAndSaveToGallery(
                context = context,
                sourceFile = tempVideo,
                title = "Test TikTok Clip",
                platformName = "TikTok",
                qualityLabel = "1080p Full HD",
                config = FypOptimizationConfig(enabled = true),
                onStepUpdate = { _, _ -> }
            )

            assertNotEquals(report.originalMd5, report.cleanedMd5)
            assertNotEquals(report.originalSha256, report.cleanedSha256)
            assertTrue(report.removedTagsCount >= 4)
            tempVideo.delete()
        }
    }
}
