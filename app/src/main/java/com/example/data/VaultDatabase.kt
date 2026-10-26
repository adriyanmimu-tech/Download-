package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pulsesave_settings")

@Entity(tableName = "vault_videos")
data class VaultVideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val caption: String,
    val hashtags: String,
    val platformName: String,
    val sourceUrl: String,
    val qualityLabel: String,
    val resolution: String,
    val savedGalleryUri: String,
    val localFilePath: String,
    val originalMd5: String,
    val cleanedMd5: String,
    val removedTagsCount: Int,
    val appliedProtectionsSummary: String,
    val fileSizeBytes: Long,
    val isFypSafe: Boolean,
    val fypSafetyScore: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_videos ORDER BY createdAt DESC")
    fun getAllVideos(): Flow<List<VaultVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VaultVideoEntity): Long

    @Query("DELETE FROM vault_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Int)

    @Query("DELETE FROM vault_videos")
    suspend fun clearAll()
}

@Database(entities = [VaultVideoEntity::class], version = 1, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null

        fun getInstance(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "pulsesave_vault.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

data class ExtractorApiSettings(
    val customApiEndpoint: String = "",
    val customApiKey: String = "",
    val defaultCameraSignature: String = "iPhone 16 Pro Max (iOS 18.1)"
)

class VaultRepository(
    private val vaultDao: VaultDao,
    private val context: Context
) {
    private val keyApiEndpoint = stringPreferencesKey("custom_api_endpoint")
    private val keyApiKey = stringPreferencesKey("custom_api_key")
    private val keyCameraSig = stringPreferencesKey("camera_signature")

    val allVideos: Flow<List<VaultVideoEntity>> = vaultDao.getAllVideos()

    val apiSettingsFlow: Flow<ExtractorApiSettings> = context.dataStore.data.map { prefs ->
        ExtractorApiSettings(
            customApiEndpoint = prefs[keyApiEndpoint] ?: "",
            customApiKey = prefs[keyApiKey] ?: "",
            defaultCameraSignature = prefs[keyCameraSig] ?: "iPhone 16 Pro Max (iOS 18.1)"
        )
    }

    suspend fun insertVideo(video: VaultVideoEntity): Long = vaultDao.insertVideo(video)

    suspend fun deleteVideoById(id: Int) = vaultDao.deleteVideoById(id)

    suspend fun saveApiSettings(endpoint: String, apiKey: String, cameraSignature: String) {
        context.dataStore.edit { prefs ->
            prefs[keyApiEndpoint] = endpoint.trim()
            prefs[keyApiKey] = apiKey.trim()
            prefs[keyCameraSig] = cameraSignature.trim().ifEmpty { "iPhone 16 Pro Max (iOS 18.1)" }
        }
    }
}
