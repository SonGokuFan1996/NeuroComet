@file:Suppress("unused")

package com.kyilmaz.neurocomet

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ═══════════════════════════════════════════════════════════════════════════════
// DATA MODELS
// ═══════════════════════════════════════════════════════════════════════════════

enum class BackupFrequency { OFF, DAILY, WEEKLY, MONTHLY }

enum class BackupStorageLocation { LOCAL, GOOGLE_DRIVE }

@Serializable
data class BackupScope(
    val includeProfile: Boolean = true,
    val includeMessages: Boolean = true,
    val includePosts: Boolean = true,
    val includeBookmarks: Boolean = true,
    val includeFollows: Boolean = true,
    val includeSettings: Boolean = true,
    val includeNotifications: Boolean = true
)

@Serializable
data class BackupMetadata(
    val backupId: String,
    val createdAt: String,
    val appVersion: String,
    val sizeBytes: Long,
    val isEncrypted: Boolean = false,
    val storageLocation: String = "local",
    val dataManifest: Map<String, Int> = emptyMap(),
    val scope: BackupScope = BackupScope(),
    val label: String? = null
) {
    val formattedSize: String get() {
        return when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${"%.1f".format(sizeBytes / 1024.0)} KB"
            sizeBytes < 1024L * 1024 * 1024 -> "${"%.1f".format(sizeBytes / (1024.0 * 1024))} MB"
            else -> "${"%.1f".format(sizeBytes / (1024.0 * 1024 * 1024))} GB"
        }
    }

    val formattedDate: String get() {
        return try {
            val instant = Instant.parse(createdAt)
            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm")
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (_: Exception) {
            createdAt
        }
    }
}

data class BackupProgress(
    val stage: String = "Idle",
    val progress: Float = 0f,
    val isComplete: Boolean = false,
    val error: String? = null,
    val failedItems: Int = 0
)

data class BackupSettings(
    val autoBackupFrequency: BackupFrequency = BackupFrequency.OFF,
    val wifiOnly: Boolean = true,
    val encryptBackups: Boolean = false,
    val scope: BackupScope = BackupScope(),
    val googleAccountEmail: String? = null,
    val lastBackupAt: String? = null,
    val lastBackupId: String? = null,
    val lastBackupSizeBytes: Long? = null
)

data class BackupUiState(
    val settings: BackupSettings = BackupSettings(),
    val localBackups: List<BackupMetadata> = emptyList(),
    val isLoading: Boolean = true,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val progress: BackupProgress = BackupProgress(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// VIEW MODEL
// ═══════════════════════════════════════════════════════════════════════════════

class BackupViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "BackupViewModel"
        private const val BACKUP_DIR = "neurocomet_backups"
        private const val BACKUP_VERSION = "1.0.0"
        private const val PREFS_NAME = "neurocomet_backup_settings"

        // SharedPreferences keys
        private const val KEY_AUTO_FREQUENCY = "backup_auto_frequency"
        private const val KEY_WIFI_ONLY = "backup_wifi_only"
        private const val KEY_ENCRYPT = "backup_encrypt"
        private const val KEY_LAST_BACKUP_AT = "backup_last_at"
        private const val KEY_LAST_BACKUP_ID = "backup_last_id"
        private const val KEY_LAST_BACKUP_SIZE = "backup_last_size"
        private const val KEY_SCOPE_PROFILE = "backup_scope_profile"
        private const val KEY_SCOPE_MESSAGES = "backup_scope_messages"
        private const val KEY_SCOPE_POSTS = "backup_scope_posts"
        private const val KEY_SCOPE_BOOKMARKS = "backup_scope_bookmarks"
        private const val KEY_SCOPE_FOLLOWS = "backup_scope_follows"
        private const val KEY_SCOPE_SETTINGS = "backup_scope_settings"
        private const val KEY_SCOPE_NOTIFICATIONS = "backup_scope_notifications"
    }

    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true; encodeDefaults = true }

    init {
        loadSettings()
        loadBackupList()
    }

    // ═══════════════════════════════════════════════════════════════
    // SETTINGS PERSISTENCE
    // ═══════════════════════════════════════════════════════════════

    private fun getPrefs() = getApplication<Application>()
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun loadSettings() {
        val prefs = getPrefs()
        val settings = BackupSettings(
            autoBackupFrequency = try {
                BackupFrequency.valueOf(prefs.getString(KEY_AUTO_FREQUENCY, "OFF") ?: "OFF")
            } catch (_: Exception) { BackupFrequency.OFF },
            wifiOnly = prefs.getBoolean(KEY_WIFI_ONLY, true),
            encryptBackups = prefs.getBoolean(KEY_ENCRYPT, false),
            lastBackupAt = prefs.getString(KEY_LAST_BACKUP_AT, null),
            lastBackupId = prefs.getString(KEY_LAST_BACKUP_ID, null),
            lastBackupSizeBytes = if (prefs.contains(KEY_LAST_BACKUP_SIZE))
                try { prefs.getLong(KEY_LAST_BACKUP_SIZE, 0) } catch (_: ClassCastException) {
                    // Restore may have stored Long as Int — read as Int and widen
                    try { prefs.getInt(KEY_LAST_BACKUP_SIZE, 0).toLong() } catch (_: Exception) { null }
                } else null,
            scope = BackupScope(
                includeProfile = prefs.getBoolean(KEY_SCOPE_PROFILE, true),
                includeMessages = prefs.getBoolean(KEY_SCOPE_MESSAGES, true),
                includePosts = prefs.getBoolean(KEY_SCOPE_POSTS, true),
                includeBookmarks = prefs.getBoolean(KEY_SCOPE_BOOKMARKS, true),
                includeFollows = prefs.getBoolean(KEY_SCOPE_FOLLOWS, true),
                includeSettings = prefs.getBoolean(KEY_SCOPE_SETTINGS, true),
                includeNotifications = prefs.getBoolean(KEY_SCOPE_NOTIFICATIONS, true)
            )
        )
        _state.update { it.copy(settings = settings, isLoading = false) }
    }

    private fun saveSettings(settings: BackupSettings) {
        getPrefs().edit().apply {
            putString(KEY_AUTO_FREQUENCY, settings.autoBackupFrequency.name)
            putBoolean(KEY_WIFI_ONLY, settings.wifiOnly)
            putBoolean(KEY_ENCRYPT, settings.encryptBackups)
            settings.lastBackupAt?.let { putString(KEY_LAST_BACKUP_AT, it) }
            settings.lastBackupId?.let { putString(KEY_LAST_BACKUP_ID, it) }
            settings.lastBackupSizeBytes?.let { putLong(KEY_LAST_BACKUP_SIZE, it) }
            putBoolean(KEY_SCOPE_PROFILE, settings.scope.includeProfile)
            putBoolean(KEY_SCOPE_MESSAGES, settings.scope.includeMessages)
            putBoolean(KEY_SCOPE_POSTS, settings.scope.includePosts)
            putBoolean(KEY_SCOPE_BOOKMARKS, settings.scope.includeBookmarks)
            putBoolean(KEY_SCOPE_FOLLOWS, settings.scope.includeFollows)
            putBoolean(KEY_SCOPE_SETTINGS, settings.scope.includeSettings)
            putBoolean(KEY_SCOPE_NOTIFICATIONS, settings.scope.includeNotifications)
            apply()
        }
    }

    fun updateSettings(transform: (BackupSettings) -> BackupSettings) {
        val newSettings = transform(_state.value.settings)
        _state.update { it.copy(settings = newSettings) }
        saveSettings(newSettings)
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }

    // ═══════════════════════════════════════════════════════════════
    // BACKUP DIRECTORY MANAGEMENT
    // ═══════════════════════════════════════════════════════════════

    private fun getBackupDir(): File {
        val dir = File(getApplication<Application>().filesDir, BACKUP_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun loadBackupList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = getBackupDir()
                val metaFiles = dir.listFiles()
                    ?.filter { it.name.startsWith("meta_") && it.name.endsWith(".json") }
                    ?: emptyList()

                val backups = metaFiles.mapNotNull { file ->
                    try {
                        val content = file.readText()
                        json.decodeFromString<BackupMetadata>(content)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to read metadata: ${file.name}", e)
                        null
                    }
                }.sortedByDescending { it.createdAt }

                _state.update { it.copy(localBackups = backups) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to list backups", e)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CREATE BACKUP
    // ═══════════════════════════════════════════════════════════════

    fun createBackup() {
        if (_state.value.isBackingUp) return

        // SECURITY: Only whitelisted dev devices may create backups
        if (!DeviceAuthority.isAuthorizedDevice(getApplication())) {
            _state.update { it.copy(errorMessage = "Backup not available on this device") }
            return
        }

        _state.update { it.copy(isBackingUp = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    _state.update {
                        it.copy(isBackingUp = false, errorMessage = "Not logged in")
                    }
                    return@launch
                }

                val jsonString = buildBackupJson(userId)

                // ---- Write to disk ----
                updateProgress("Saving backup...", 0.95f)

                val sizeBytes = jsonString.toByteArray().size.toLong()
                val backupId = try {
                    json.parseToJsonElement(jsonString).jsonObject["backup_id"]?.jsonPrimitive?.content
                        ?: "${System.currentTimeMillis()}_$userId"
                } catch (_: Exception) {
                    "${System.currentTimeMillis()}_$userId"
                }

                val dir = getBackupDir()
                File(dir, "backup_$backupId.ncb").writeText(jsonString)

                val scope = _state.value.settings.scope
                val metadata = BackupMetadata(
                    backupId = backupId,
                    createdAt = Instant.now().toString(),
                    appVersion = BuildConfig.VERSION_NAME,
                    sizeBytes = sizeBytes,
                    isEncrypted = false,
                    storageLocation = "local",
                    dataManifest = emptyMap(),
                    scope = scope
                )
                File(dir, "meta_$backupId.json").writeText(
                    json.encodeToString(BackupMetadata.serializer(), metadata)
                )

                // Update settings with last backup info
                withContext(Dispatchers.Main) {
                    updateSettings { s ->
                        s.copy(
                            lastBackupAt = metadata.createdAt,
                            lastBackupId = backupId,
                            lastBackupSizeBytes = sizeBytes
                        )
                    }
                }

                loadBackupList()

                _state.update {
                    it.copy(
                        isBackingUp = false,
                        progress = BackupProgress("Complete", 1f, isComplete = true),
                        successMessage = "Backup completed successfully!"
                    )
                }

                Log.i(TAG, "Backup created: $backupId (${"%.1f".format(sizeBytes / 1024.0)} KB)")

            } catch (e: Exception) {
                Log.e(TAG, "Backup failed", e)
                _state.update {
                    it.copy(
                        isBackingUp = false,
                        progress = BackupProgress(error = "Backup failed: ${e.message}"),
                        errorMessage = "Backup failed: ${e.message}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // RESTORE BACKUP
    // ═══════════════════════════════════════════════════════════════

    fun restoreBackup(backupId: String) {
        if (_state.value.isRestoring) return

        // SECURITY: Only whitelisted dev devices may restore backups
        if (!DeviceAuthority.isAuthorizedDevice(getApplication())) {
            _state.update { it.copy(errorMessage = "Restore not available on this device") }
            return
        }

        _state.update { it.copy(isRestoring = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    _state.update {
                        it.copy(isRestoring = false, errorMessage = "Not logged in")
                    }
                    return@launch
                }

                updateProgress("Loading backup...", 0.05f)

                val dir = getBackupDir()
                val file = File(dir, "backup_$backupId.ncb")
                if (!file.exists()) {
                    _state.update {
                        it.copy(isRestoring = false, errorMessage = "Backup file not found")
                    }
                    return@launch
                }

                val backupData = json.parseToJsonElement(file.readText()).jsonObject
                val backupUserId = backupData["user_id"]?.jsonPrimitive?.content

                if (backupUserId != userId) {
                    _state.update {
                        it.copy(
                            isRestoring = false,
                            errorMessage = "This backup belongs to a different account"
                        )
                    }
                    return@launch
                }

                val client = AppSupabaseClient.client
                if (client == null) {
                    Log.i(TAG, "Supabase not configured — skipping cloud data restore, restoring local settings only")
                }

                var failedItems = 0

                // ---- Restore Profile ----
                if (client != null && backupData.containsKey("profile")) {
                    updateProgress("Restoring profile...", 0.15f)
                    try {
                        val profile = backupData["profile"]!!.jsonObject
                        val updates = buildJsonObject {
                            profile["display_name"]?.let { put("display_name", it) }
                            profile["bio"]?.let { put("bio", it) }
                            profile["avatar_url"]?.let { put("avatar_url", it) }
                            profile["banner_url"]?.let { put("banner_url", it) }
                            put("updated_at", Instant.now().toString())
                        }
                        safeUpdate("users", updates, "id=eq.$userId")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore profile", e)
                    }
                }

                // ---- Restore Posts ----
                if (client != null && backupData.containsKey("posts")) {
                    updateProgress("Restoring posts...", 0.3f)
                    try {
                        val posts = backupData["posts"]!!.jsonArray
                        for (post in posts) {
                            try {
                                client.safeInsert("posts", post.jsonObject)
                            } catch (_: Exception) {
                                failedItems++
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore posts", e)
                        failedItems++
                    }
                }

                // ---- Restore Bookmarks ----
                if (client != null && backupData.containsKey("bookmarks")) {
                    updateProgress("Restoring bookmarks...", 0.45f)
                    try {
                        val bookmarks = backupData["bookmarks"]!!.jsonArray
                        for (bookmark in bookmarks) {
                            try {
                                client.safeInsert("bookmarks", bookmark.jsonObject)
                            } catch (_: Exception) { failedItems++ }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore bookmarks", e)
                        failedItems++
                    }
                }

                // ---- Restore Follows ----
                if (client != null && backupData.containsKey("following")) {
                    updateProgress("Restoring follows...", 0.55f)
                    try {
                        val following = backupData["following"]!!.jsonArray
                        for (follow in following) {
                            try {
                                client.safeInsert("follows", follow.jsonObject)
                            } catch (_: Exception) { failedItems++ }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore follows", e)
                        failedItems++
                    }
                }

                // ---- Restore Messages ----
                if (client != null && backupData.containsKey("messages")) {
                    updateProgress("Restoring messages...", 0.6f)
                    try {
                        val messages = backupData["messages"]!!.jsonArray
                        for (msg in messages) {
                            try {
                                client.safeInsert("messages", msg.jsonObject)
                            } catch (_: Exception) { failedItems++ }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore messages", e)
                        failedItems++
                    }
                }

                // ---- Restore Conversations ----
                if (client != null && backupData.containsKey("conversations")) {
                    updateProgress("Restoring conversations...", 0.65f)
                    try {
                        val conversations = backupData["conversations"]!!.jsonArray
                        for (conv in conversations) {
                            try { client.safeInsert("conversations", conv.jsonObject) } catch (_: Exception) { failedItems++ }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore conversations", e)
                        failedItems++
                    }
                }

                // ---- Restore Post Comments ----
                if (client != null && backupData.containsKey("post_comments")) {
                    updateProgress("Restoring comments...", 0.7f)
                    try {
                        val comments = backupData["post_comments"]!!.jsonArray
                        for (comment in comments) {
                            try { client.safeInsert("post_comments", comment.jsonObject) } catch (_: Exception) { failedItems++ }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore post comments", e)
                        failedItems++
                    }
                }

                // ---- Restore Post Likes ----
                if (client != null && backupData.containsKey("post_likes")) {
                    updateProgress("Restoring likes...", 0.73f)
                    try {
                        val likes = backupData["post_likes"]!!.jsonArray
                        for (like in likes) {
                            try { client.safeInsert("post_likes", like.jsonObject) } catch (_: Exception) { failedItems++ }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore post likes", e)
                        failedItems++
                    }
                }

                // ---- Restore Notifications ----
                if (client != null && backupData.containsKey("notifications")) {
                    updateProgress("Restoring notifications...", 0.76f)
                    try {
                        val notifications = backupData["notifications"]!!.jsonArray
                        for (notif in notifications) {
                            try { client.safeInsert("notifications", notif.jsonObject) } catch (_: Exception) { failedItems++ }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore notifications", e)
                        failedItems++
                    }
                }

                // ---- Restore All Local Settings (comprehensive) ----
                if (backupData.containsKey("all_local_settings")) {
                    updateProgress("Restoring all settings...", 0.82f)
                    try {
                        val allStores = backupData["all_local_settings"]!!.jsonObject
                        val app = getApplication<Application>()
                        for ((storeName, storeData) in allStores) {
                            try {
                                val storeObj = storeData.jsonObject
                                val prefs = app.getSharedPreferences(storeName, Context.MODE_PRIVATE)

                                // New format: { "_values": {...}, "_types": {...} }
                                val valuesObj = storeObj["_values"]?.jsonObject
                                val typesObj = storeObj["_types"]?.jsonObject

                                if (valuesObj != null) {
                                    // New typed format
                                    prefs.edit {
                                        for ((key, value) in valuesObj) {
                                            val declaredType = typesObj?.get(key)?.jsonPrimitive?.contentOrNull
                                            restoreTypedPrefValue(this, key, value, declaredType)
                                        }
                                    }
                                } else {
                                    // Legacy flat format (no _values/_types wrapper)
                                    val existingAll = prefs.all
                                    prefs.edit {
                                        for ((key, value) in storeObj) {
                                            restorePrefValue(this, key, value, existingAll[key])
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to restore prefs store: $storeName", e)
                            }
                        }
                        // Reload SettingsManager so UI reflects restored values
                        withContext(Dispatchers.Main) {
                            SettingsManager.reload()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore all_local_settings", e)
                    }
                } else if (backupData.containsKey("local_settings")) {
                    // ---- Legacy fallback: only neurocomet_persistent_settings ----
                    updateProgress("Restoring settings (legacy)...", 0.85f)
                    try {
                        val settingsJson = backupData["local_settings"]!!.jsonObject
                        val settingsPrefs = getApplication<Application>()
                            .getSharedPreferences("neurocomet_persistent_settings", Context.MODE_PRIVATE)
                        val existingAll = settingsPrefs.all
                        settingsPrefs.edit {
                            for ((key, value) in settingsJson) {
                                restorePrefValue(this, key, value, existingAll[key])
                            }
                        }
                        withContext(Dispatchers.Main) {
                            SettingsManager.reload()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to restore legacy settings", e)
                    }
                }

                val successMsg = if (failedItems > 0) {
                    "Data restored successfully! $failedItems item(s) could not be restored. Restart the app for all changes to take effect."
                } else {
                    "Data restored successfully! Restart the app for all changes to take effect."
                }

                _state.update {
                    it.copy(
                        isRestoring = false,
                        progress = BackupProgress("Complete", 1f, isComplete = true, failedItems = failedItems),
                        successMessage = successMsg
                    )
                }

                Log.i(TAG, "Backup restored: $backupId")

            } catch (e: Exception) {
                Log.e(TAG, "Restore failed", e)
                _state.update {
                    it.copy(
                        isRestoring = false,
                        progress = BackupProgress(error = "Restore failed: ${e.message}"),
                        errorMessage = "Restore failed: ${e.message}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE / SHARE BACKUPS
    // ═══════════════════════════════════════════════════════════════

    fun deleteBackup(backupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = getBackupDir()
            File(dir, "backup_$backupId.ncb").delete()
            File(dir, "meta_$backupId.json").delete()
            loadBackupList()
        }
    }

    fun deleteAllBackups() {
        viewModelScope.launch(Dispatchers.IO) {
            getBackupDir().deleteRecursively()
            loadBackupList()
            _state.update { it.copy(successMessage = "All backups deleted") }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DEV: CREATE TEST BACKUPS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Creates multiple backups with different scope configurations to
     * exercise and validate the entire backup pipeline (typed format,
     * scope filtering, restore). For dev testing only.
     */
    fun createTestBackups() {
        if (_state.value.isBackingUp) return
        if (!DeviceAuthority.isAuthorizedDevice(getApplication())) return

        _state.update { it.copy(isBackingUp = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    _state.update { it.copy(isBackingUp = false, errorMessage = "Not logged in") }
                    return@launch
                }

                val testConfigs = listOf(
                    "Profile Only" to BackupScope(
                        includeProfile = true, includeMessages = false, includePosts = false,
                        includeBookmarks = false, includeFollows = false, includeSettings = false,
                        includeNotifications = false
                    ),
                    "Messages & Posts" to BackupScope(
                        includeProfile = false, includeMessages = true, includePosts = true,
                        includeBookmarks = false, includeFollows = false, includeSettings = false,
                        includeNotifications = false
                    ),
                    "Settings Only" to BackupScope(
                        includeProfile = false, includeMessages = false, includePosts = false,
                        includeBookmarks = false, includeFollows = false, includeSettings = true,
                        includeNotifications = false
                    ),
                    "Full Backup" to BackupScope(
                        includeProfile = true, includeMessages = true, includePosts = true,
                        includeBookmarks = true, includeFollows = true, includeSettings = true,
                        includeNotifications = true
                    )
                )

                val results = mutableListOf<String>()

                for ((index, pair) in testConfigs.withIndex()) {
                    val (label, scope) = pair
                    val progress = (index + 1).toFloat() / testConfigs.size
                    updateProgress("Creating test: $label...", progress * 0.9f)

                    try {
                        // Temporarily swap scope for this backup
                        val savedScope = _state.value.settings.scope
                        _state.update { it.copy(settings = it.settings.copy(scope = scope)) }

                        val jsonString = buildBackupJson(userId)

                        // Restore original scope
                        _state.update { it.copy(settings = it.settings.copy(scope = savedScope)) }

                        val sizeBytes = jsonString.toByteArray().size.toLong()
                        val backupId = "${System.currentTimeMillis()}_test_${index}_$userId"

                        val dir = getBackupDir()
                        File(dir, "backup_$backupId.ncb").writeText(jsonString)

                        val metadata = BackupMetadata(
                            backupId = backupId,
                            createdAt = Instant.now().toString(),
                            appVersion = BuildConfig.VERSION_NAME,
                            sizeBytes = sizeBytes,
                            isEncrypted = false,
                            storageLocation = "local",
                            dataManifest = emptyMap(),
                            scope = scope,
                            label = label
                        )
                        File(dir, "meta_$backupId.json").writeText(
                            json.encodeToString(BackupMetadata.serializer(), metadata)
                        )

                        // Validate: re-read and parse the backup
                        val readBack = File(dir, "backup_$backupId.ncb").readText()
                        val parsed = json.parseToJsonElement(readBack).jsonObject
                        val version = parsed["backup_version"]?.jsonPrimitive?.content
                        val hasTypedSettings = parsed["all_local_settings"]
                            ?.jsonObject?.values?.firstOrNull()
                            ?.jsonObject?.containsKey("_types") == true

                        val checks = mutableListOf<String>()
                        if (version == BACKUP_VERSION) checks.add("ver✓")
                        if (parsed["user_id"]?.jsonPrimitive?.content == userId) checks.add("uid✓")
                        if (scope.includeProfile && parsed.containsKey("profile")) checks.add("profile✓")
                        if (scope.includePosts && parsed.containsKey("posts")) checks.add("posts✓")
                        if (scope.includeMessages) checks.add("msgs✓")
                        if (scope.includeBookmarks && parsed.containsKey("bookmarks")) checks.add("bkm✓")
                        if (scope.includeFollows) checks.add("follows✓")
                        if (scope.includeSettings && parsed.containsKey("all_local_settings")) checks.add("settings✓")
                        if (scope.includeSettings && hasTypedSettings) checks.add("typed✓")
                        if (scope.includeNotifications) checks.add("notif✓")

                        results.add("✅ $label (${"%.1f".format(sizeBytes / 1024.0)} KB) [${checks.joinToString(" ")}]")
                        Log.i(TAG, "Test backup created: $label — $backupId (${sizeBytes}B)")

                        // Small delay between backups so timestamps differ
                        kotlinx.coroutines.delay(100)

                    } catch (e: Exception) {
                        results.add("❌ $label: ${e.message}")
                        Log.e(TAG, "Test backup failed: $label", e)
                    }
                }

                loadBackupList()

                val summary = results.joinToString("\n")
                _state.update {
                    it.copy(
                        isBackingUp = false,
                        progress = BackupProgress("Complete", 1f, isComplete = true),
                        successMessage = "Created ${testConfigs.size} test backups:\n$summary"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Test backup suite failed", e)
                _state.update {
                    it.copy(
                        isBackingUp = false,
                        progress = BackupProgress(error = "Test failed: ${e.message}"),
                        errorMessage = "Test backup suite failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Validates that an existing backup can be read, parsed, and its
     * structure is correct. Returns a human-readable report.
     */
    fun validateBackup(backupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.update { it.copy(errorMessage = null, successMessage = null) }

                val dir = getBackupDir()
                val file = File(dir, "backup_$backupId.ncb")
                if (!file.exists()) {
                    _state.update { it.copy(errorMessage = "Backup file not found: $backupId") }
                    return@launch
                }

                val raw = file.readText()
                val parsed = json.parseToJsonElement(raw).jsonObject
                val checks = mutableListOf<String>()
                val issues = mutableListOf<String>()

                // Version check
                val ver = parsed["backup_version"]?.jsonPrimitive?.content
                if (ver != null) checks.add("✅ Version: $ver") else issues.add("❌ Missing backup_version")

                // User ID
                val uid = parsed["user_id"]?.jsonPrimitive?.content
                if (uid != null) checks.add("✅ User ID present") else issues.add("❌ Missing user_id")

                // Created at
                val createdAt = parsed["created_at"]?.jsonPrimitive?.content
                if (createdAt != null) checks.add("✅ Created: $createdAt") else issues.add("❌ Missing created_at")

                // Data sections
                val sections = listOf("profile", "posts", "post_comments", "post_likes",
                    "conversations", "messages", "bookmarks", "following", "followers",
                    "notifications", "local_settings", "all_local_settings", "data_manifest")
                for (section in sections) {
                    if (parsed.containsKey(section)) {
                        val el = parsed[section]!!
                        val count = when (el) {
                            is JsonArray -> "${el.size} items"
                            is JsonObject -> "${el.size} keys"
                            else -> "present"
                        }
                        checks.add("  📦 $section: $count")
                    }
                }

                // Typed settings check
                val allSettings = parsed["all_local_settings"]?.jsonObject
                if (allSettings != null) {
                    var typedCount = 0
                    var untypedCount = 0
                    for ((storeName, storeData) in allSettings) {
                        val obj = storeData.jsonObject
                        if (obj.containsKey("_types") && obj.containsKey("_values")) {
                            typedCount++
                            val types = obj["_types"]!!.jsonObject
                            val values = obj["_values"]!!.jsonObject
                            checks.add("  🔑 $storeName: ${values.size} values, ${types.size} types")
                        } else {
                            untypedCount++
                            checks.add("  ⚠️ $storeName: flat/untyped (${obj.size} keys)")
                        }
                    }
                    if (typedCount > 0) checks.add("✅ Typed format: $typedCount stores")
                    if (untypedCount > 0) issues.add("⚠️ Untyped stores: $untypedCount (legacy)")
                }

                val size = raw.toByteArray().size
                val report = buildString {
                    append("Backup Validation: $backupId\n")
                    append("Size: ${"%.1f".format(size / 1024.0)} KB\n\n")
                    checks.forEach { append("$it\n") }
                    if (issues.isNotEmpty()) {
                        append("\nIssues:\n")
                        issues.forEach { append("$it\n") }
                    } else {
                        append("\n✅ All checks passed!")
                    }
                }

                _state.update { it.copy(successMessage = report) }
                Log.i(TAG, report)

            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Validation failed: ${e.message}") }
            }
        }
    }

    fun shareBackup(backupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = getBackupDir()
                val file = File(dir, "backup_$backupId.ncb")
                if (!file.exists()) {
                    _state.update { it.copy(errorMessage = "Backup file not found") }
                    return@launch
                }

                val context = getApplication<Application>()
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "NeuroComet Backup")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "Share Backup").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                Log.e(TAG, "Failed to share backup", e)
                _state.update { it.copy(errorMessage = "Failed to share backup: ${e.message}") }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EXPORT TO FILE (SAF — supports Google Drive, Downloads, etc.)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Returns the suggested filename for a new backup export.
     * The caller should use this with ACTION_CREATE_DOCUMENT.
     */
    fun getSuggestedExportFilename(): String {
        val ts = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"))
        return "NeuroComet_Backup_$ts.ncb"
    }

    /**
     * Creates a fresh backup in memory and writes it to the given SAF URI.
     * Call this from the Activity/Composable after the user picks a destination
     * via ACTION_CREATE_DOCUMENT.
     */
    fun exportBackupToUri(uri: Uri) {
        if (_state.value.isBackingUp) return
        if (!DeviceAuthority.isAuthorizedDevice(getApplication())) {
            _state.update { it.copy(errorMessage = "Backup not available on this device") }
            return
        }

        _state.update { it.copy(isBackingUp = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    _state.update { it.copy(isBackingUp = false, errorMessage = "Not logged in") }
                    return@launch
                }

                // First, create a local backup (reuse the core logic)
                updateProgress("Preparing backup data...", 0.05f)
                val jsonString = buildBackupJson(userId)

                // Write to the SAF URI
                updateProgress("Writing to external storage...", 0.9f)
                val context = getApplication<Application>()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonString.toByteArray(Charsets.UTF_8))
                    out.flush()
                } ?: throw Exception("Could not open output stream for selected location")

                val sizeBytes = jsonString.toByteArray().size.toLong()

                // Also save a local copy for the backup list
                val backupId = "${System.currentTimeMillis()}_$userId"
                val dir = getBackupDir()
                File(dir, "backup_$backupId.ncb").writeText(jsonString)
                val metadata = BackupMetadata(
                    backupId = backupId,
                    createdAt = Instant.now().toString(),
                    appVersion = BuildConfig.VERSION_NAME,
                    sizeBytes = sizeBytes,
                    isEncrypted = false,
                    storageLocation = "exported",
                    dataManifest = emptyMap(),
                    scope = _state.value.settings.scope
                )
                File(dir, "meta_$backupId.json").writeText(
                    json.encodeToString(BackupMetadata.serializer(), metadata)
                )

                withContext(Dispatchers.Main) {
                    updateSettings { s ->
                        s.copy(lastBackupAt = metadata.createdAt, lastBackupId = backupId, lastBackupSizeBytes = sizeBytes)
                    }
                }
                loadBackupList()

                _state.update {
                    it.copy(
                        isBackingUp = false,
                        progress = BackupProgress("Complete", 1f, isComplete = true),
                        successMessage = "Backup exported successfully! (${"%.1f".format(sizeBytes / 1024.0)} KB)"
                    )
                }
                Log.i(TAG, "Backup exported to URI: $uri (${"%.1f".format(sizeBytes / 1024.0)} KB)")
            } catch (e: Exception) {
                Log.e(TAG, "Export failed", e)
                _state.update {
                    it.copy(
                        isBackingUp = false,
                        progress = BackupProgress(error = "Export failed: ${e.message}"),
                        errorMessage = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Writes an existing local backup to the given SAF URI.
     * Use this when the user wants to export a specific existing backup.
     */
    fun exportExistingBackupToUri(backupId: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getBackupDir(), "backup_$backupId.ncb")
                if (!file.exists()) {
                    _state.update { it.copy(errorMessage = "Backup file not found") }
                    return@launch
                }

                _state.update { it.copy(isBackingUp = true, errorMessage = null, successMessage = null) }
                updateProgress("Exporting backup...", 0.5f)

                val context = getApplication<Application>()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { input -> input.copyTo(out) }
                    out.flush()
                } ?: throw Exception("Could not open output stream")

                _state.update {
                    it.copy(
                        isBackingUp = false,
                        progress = BackupProgress("Complete", 1f, isComplete = true),
                        successMessage = "Backup saved to selected location!"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Export existing backup failed", e)
                _state.update {
                    it.copy(
                        isBackingUp = false,
                        errorMessage = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // IMPORT FROM FILE (SAF — supports Google Drive, Downloads, etc.)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Reads a .ncb backup file from the given SAF URI, saves it locally,
     * and then restores it. Call this after ACTION_OPEN_DOCUMENT.
     */
    fun importAndRestoreFromUri(uri: Uri) {
        if (_state.value.isRestoring) return
        if (!DeviceAuthority.isAuthorizedDevice(getApplication())) {
            _state.update { it.copy(errorMessage = "Restore not available on this device") }
            return
        }

        _state.update { it.copy(isRestoring = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    _state.update { it.copy(isRestoring = false, errorMessage = "Not logged in") }
                    return@launch
                }

                updateProgress("Reading backup file...", 0.1f)

                val context = getApplication<Application>()
                val rawJson = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader(Charsets.UTF_8).readText()
                } ?: run {
                    _state.update { it.copy(isRestoring = false, errorMessage = "Could not read the selected file") }
                    return@launch
                }

                // Validate it's a real backup
                updateProgress("Validating backup...", 0.2f)
                val backupData = try {
                    json.parseToJsonElement(rawJson).jsonObject
                } catch (e: Exception) {
                    _state.update { it.copy(isRestoring = false, errorMessage = "Invalid backup file — not valid JSON") }
                    return@launch
                }

                val backupVersion = backupData["backup_version"]?.jsonPrimitive?.content
                if (backupVersion == null) {
                    _state.update { it.copy(isRestoring = false, errorMessage = "Invalid backup file — missing version header") }
                    return@launch
                }

                val backupUserId = backupData["user_id"]?.jsonPrimitive?.content
                if (backupUserId != null && backupUserId != userId) {
                    _state.update {
                        it.copy(isRestoring = false, errorMessage = "This backup belongs to a different account")
                    }
                    return@launch
                }

                // Save locally so it appears in the backup list
                updateProgress("Importing backup...", 0.3f)
                val importedId = backupData["backup_id"]?.jsonPrimitive?.content
                    ?: "${System.currentTimeMillis()}_imported"
                val sizeBytes = rawJson.toByteArray().size.toLong()

                val dir = getBackupDir()
                File(dir, "backup_$importedId.ncb").writeText(rawJson)

                val metadata = BackupMetadata(
                    backupId = importedId,
                    createdAt = backupData["created_at"]?.jsonPrimitive?.content ?: Instant.now().toString(),
                    appVersion = backupData["app_version"]?.jsonPrimitive?.content ?: "unknown",
                    sizeBytes = sizeBytes,
                    isEncrypted = false,
                    storageLocation = "imported"
                )
                File(dir, "meta_$importedId.json").writeText(
                    json.encodeToString(BackupMetadata.serializer(), metadata)
                )
                loadBackupList()

                // Now restore from the imported backup
                updateProgress("Restoring data...", 0.4f)
                // Reset restoring flag then delegate to restoreBackup
                _state.update { it.copy(isRestoring = false) }
                restoreBackup(importedId)

            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                _state.update {
                    it.copy(
                        isRestoring = false,
                        errorMessage = "Import failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Imports a .ncb backup file from the given SAF URI and saves it locally
     * WITHOUT restoring. The user can then manually restore later.
     */
    fun importBackupFromUri(uri: Uri) {
        if (_state.value.isRestoring) return
        if (!DeviceAuthority.isAuthorizedDevice(getApplication())) {
            _state.update { it.copy(errorMessage = "Import not available on this device") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.update { it.copy(isRestoring = true, errorMessage = null, successMessage = null) }
                updateProgress("Reading backup file...", 0.2f)

                val context = getApplication<Application>()
                val rawJson = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader(Charsets.UTF_8).readText()
                } ?: run {
                    _state.update { it.copy(isRestoring = false, errorMessage = "Could not read the selected file") }
                    return@launch
                }

                updateProgress("Validating...", 0.5f)
                val backupData = try {
                    json.parseToJsonElement(rawJson).jsonObject
                } catch (_: Exception) {
                    _state.update { it.copy(isRestoring = false, errorMessage = "Invalid backup file") }
                    return@launch
                }

                if (backupData["backup_version"]?.jsonPrimitive?.content == null) {
                    _state.update { it.copy(isRestoring = false, errorMessage = "Invalid backup file — missing version header") }
                    return@launch
                }

                val importedId = backupData["backup_id"]?.jsonPrimitive?.content
                    ?: "${System.currentTimeMillis()}_imported"
                val sizeBytes = rawJson.toByteArray().size.toLong()

                val dir = getBackupDir()
                File(dir, "backup_$importedId.ncb").writeText(rawJson)

                val metadata = BackupMetadata(
                    backupId = importedId,
                    createdAt = backupData["created_at"]?.jsonPrimitive?.content ?: Instant.now().toString(),
                    appVersion = backupData["app_version"]?.jsonPrimitive?.content ?: "unknown",
                    sizeBytes = sizeBytes,
                    isEncrypted = false,
                    storageLocation = "imported"
                )
                File(dir, "meta_$importedId.json").writeText(
                    json.encodeToString(BackupMetadata.serializer(), metadata)
                )
                loadBackupList()

                _state.update {
                    it.copy(
                        isRestoring = false,
                        progress = BackupProgress("Complete", 1f, isComplete = true),
                        successMessage = "Backup imported (${"%.1f".format(sizeBytes / 1024.0)} KB) — you can restore it from the list below."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                _state.update { it.copy(isRestoring = false, errorMessage = "Import failed: ${e.message}") }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Builds the backup JSON string (core data gathering logic).
     * Shared by createBackup() and exportBackupToUri().
     */
    private suspend fun buildBackupJson(userId: String): String {
        val scope = _state.value.settings.scope
        val manifest = mutableMapOf<String, Int>()
        val backupData = buildJsonObject {
            put("backup_version", BACKUP_VERSION)
            put("backup_id", "${System.currentTimeMillis()}_$userId")
            put("user_id", userId)
            put("created_at", Instant.now().toString())
            put("app_version", BuildConfig.VERSION_NAME)

            if (scope.includeProfile) {
                updateProgress("Backing up profile...", 0.1f)
                try {
                    val rows = safeSelect("users", "*", "id=eq.$userId")
                    if (rows.isNotEmpty()) { put("profile", rows[0]); manifest["profile"] = 1 }
                } catch (e: Exception) { Log.w(TAG, "Failed to backup profile", e) }
            }
            if (scope.includePosts) {
                updateProgress("Backing up posts...", 0.2f)
                try {
                    val posts = safeSelect("posts", "*", "user_id=eq.$userId")
                    put("posts", posts); manifest["posts"] = posts.size
                    val comments = safeSelect("post_comments", "*", "user_id=eq.$userId")
                    put("post_comments", comments); manifest["post_comments"] = comments.size
                    val likes = safeSelect("post_likes", "*", "user_id=eq.$userId")
                    put("post_likes", likes); manifest["post_likes"] = likes.size
                } catch (e: Exception) { Log.w(TAG, "Failed to backup posts", e) }
            }
            if (scope.includeMessages) {
                updateProgress("Backing up messages...", 0.35f)
                try {
                    val participants = safeSelect("conversation_participants", "conversation_id", "user_id=eq.$userId")
                    val convIds = participants.mapNotNull { it.jsonObject["conversation_id"]?.jsonPrimitive?.content }
                    if (convIds.isNotEmpty()) {
                        val conversations = safeSelect("conversations", "*", "id=in.(${convIds.joinToString(",")})")
                        put("conversations", conversations); manifest["conversations"] = conversations.size
                        val allMessages = buildJsonArray {
                            for (cid in convIds) { safeSelect("messages", "*", "conversation_id=eq.$cid&order=created_at.asc").forEach { add(it) } }
                        }
                        put("messages", allMessages); manifest["messages"] = allMessages.size
                    }
                } catch (e: Exception) { Log.w(TAG, "Failed to backup messages", e) }
            }
            if (scope.includeBookmarks) {
                updateProgress("Backing up bookmarks...", 0.5f)
                try {
                    val bookmarks = safeSelect("bookmarks", "*", "user_id=eq.$userId")
                    put("bookmarks", bookmarks); manifest["bookmarks"] = bookmarks.size
                } catch (e: Exception) { Log.w(TAG, "Failed to backup bookmarks", e) }
            }
            if (scope.includeFollows) {
                updateProgress("Backing up follows...", 0.6f)
                try {
                    val following = safeSelect("follows", "*", "follower_id=eq.$userId")
                    put("following", following); manifest["following"] = following.size
                    val followers = safeSelect("follows", "*", "following_id=eq.$userId")
                    put("followers", followers); manifest["followers"] = followers.size
                } catch (e: Exception) { Log.w(TAG, "Failed to backup follows", e) }
            }
            if (scope.includeNotifications) {
                updateProgress("Backing up notifications...", 0.7f)
                try {
                    val notifications = safeSelect("notifications", "*", "user_id=eq.$userId")
                    put("notifications", notifications); manifest["notifications"] = notifications.size
                } catch (e: Exception) { Log.w(TAG, "Failed to backup notifications", e) }
            }
            if (scope.includeSettings) {
                updateProgress("Backing up settings...", 0.8f)
                try {
                    // Back up ALL user-facing SharedPreferences stores
                    val app = getApplication<Application>()
                    val allPrefsStores = listOf(
                        "neurocomet_persistent_settings",  // SettingsManager (theme, accessibility, notifications, privacy, content)
                        "theme_settings",                   // ThemeSettings (dark mode, dynamic color, fonts, animation prefs)
                        "auth_prefs",                       // StaySignedInSettings
                        "parental_controls",                // ParentalControls (PIN, screen time, bedtime)
                        "neuro_games_prefs",                // GameUnlockManager (achievements, high scores)
                        "neuro_games_tutorial_prefs",       // TutorialManager (tutorial completion)
                        "neurocomet_contacts",              // ContactsManager (sync state)
                        "neurocomet_backup_settings",       // Backup settings (frequency, scope, last backup info)
                    )

                    val allSettingsJson = buildJsonObject {
                        for (storeName in allPrefsStores) {
                            try {
                                val prefs = app.getSharedPreferences(storeName, Context.MODE_PRIVATE)
                                val storeJson = buildJsonObject {
                                    // Store values
                                    put("_values", buildJsonObject {
                                        prefs.all.forEach { (key, value) ->
                                            when (value) {
                                                is Boolean -> put(key, value)
                                                is Int -> put(key, value)
                                                is Long -> put(key, value)
                                                is Float -> put(key, value.toDouble())
                                                is String -> put(key, value)
                                                is Set<*> -> {
                                                    @Suppress("UNCHECKED_CAST")
                                                    put(key, buildJsonArray {
                                                        (value as? Set<String>)?.forEach { add(it) }
                                                    })
                                                }
                                            }
                                        }
                                    })
                                    // Store original types so restore doesn't guess wrong
                                    // (JSON can't distinguish Int/Long/Float/Double)
                                    put("_types", buildJsonObject {
                                        prefs.all.forEach { (key, value) ->
                                            val typeName = when (value) {
                                                is Boolean -> "bool"
                                                is Int -> "int"
                                                is Long -> "long"
                                                is Float -> "float"
                                                is String -> "string"
                                                is Set<*> -> "stringset"
                                                else -> null
                                            }
                                            typeName?.let { put(key, it) }
                                        }
                                    })
                                }
                                if (storeJson["_values"]?.jsonObject?.isNotEmpty() == true) {
                                    put(storeName, storeJson)
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to backup prefs store: $storeName", e)
                            }
                        }
                    }
                    put("all_local_settings", allSettingsJson)
                    manifest["all_local_settings"] = allSettingsJson.size

                    // Also keep legacy "local_settings" key for backward compatibility
                    val legacyPrefs = app.getSharedPreferences("neurocomet_persistent_settings", Context.MODE_PRIVATE)
                    val legacyJson = buildJsonObject {
                        legacyPrefs.all.forEach { (key, value) ->
                            when (value) {
                                is Boolean -> put(key, value); is Int -> put(key, value)
                                is Long -> put(key, value); is Float -> put(key, value.toDouble())
                                is String -> put(key, value)
                            }
                        }
                    }
                    put("local_settings", legacyJson); manifest["local_settings"] = legacyJson.size
                } catch (e: Exception) { Log.w(TAG, "Failed to backup settings", e) }
            }
            put("data_manifest", buildJsonObject { manifest.forEach { (k, v) -> put(k, v) } })
        }
        return backupData.toString()
    }

    /**
     * Restore a single SharedPreferences value using the declared type from
     * the backup's _types map. This avoids Float→Int mismatches caused by
     * JSON number ambiguity.
     */
    private fun restoreTypedPrefValue(
        editor: android.content.SharedPreferences.Editor,
        key: String,
        value: JsonElement,
        declaredType: String?
    ) {
        when (declaredType) {
            "bool" -> editor.putBoolean(key, value.jsonPrimitive.boolean)
            "int" -> editor.putInt(key, value.jsonPrimitive.int)
            "long" -> editor.putLong(key, value.jsonPrimitive.long)
            "float" -> editor.putFloat(key, value.jsonPrimitive.float)
            "string" -> editor.putString(key, value.jsonPrimitive.content)
            "stringset" -> {
                val set = (value as? JsonArray)?.mapNotNull {
                    (it as? JsonPrimitive)?.contentOrNull
                }?.toSet() ?: emptySet()
                editor.putStringSet(key, set)
            }
            else -> restorePrefValue(editor, key, value) // fallback
        }
    }

    /**
     * Best-effort restore without type information (legacy backups).
     *
     * If [existingValue] is provided (the current value in SharedPrefs for
     * this key), we match its Java type exactly. This handles the case where
     * a key was originally Long but JSON parsed it as fitting in Int range.
     *
     * If the key doesn't exist yet, we use heuristics:
     *  - decimal point → Float
     *  - boolean → Boolean
     *  - string → String
     *  - whole number → Long (safer default; getLong on Long works everywhere)
     */
    private fun restorePrefValue(
        editor: android.content.SharedPreferences.Editor,
        key: String,
        value: JsonElement,
        existingValue: Any? = null
    ) {
        try {
            when {
                value is JsonArray -> {
                    val set = value.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.toSet()
                    editor.putStringSet(key, set)
                }
                value !is JsonPrimitive -> { /* skip objects/nulls */ }
                value.booleanOrNull != null -> editor.putBoolean(key, value.boolean)
                value.isString -> editor.putString(key, value.content)
                // Numbers: if we know the existing type, honour it
                existingValue is Float -> editor.putFloat(key, value.float)
                existingValue is Int -> editor.putInt(key, value.int)
                existingValue is Long -> editor.putLong(key, value.long)
                // No existing value — use heuristics
                value.content.contains('.') -> editor.putFloat(key, value.float)
                else -> editor.putLong(key, value.long)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to restore pref key '$key': ${e.message}")
        }
    }

    private fun getCurrentUserId(): String? {
        // Try real Supabase auth first
        try {
            val supabaseId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id
            if (supabaseId != null) return supabaseId
        } catch (_: Exception) { }

        // Fall back to the local mock user when running in debug/mock mode.
        // In debug builds the user logs in via skipAuth() which sets a local
        // User object but does not create a Supabase session.
        return if (BuildConfig.DEBUG) CURRENT_USER.id else null
    }

    private fun updateProgress(stage: String, progress: Float) {
        _state.update {
            it.copy(progress = BackupProgress(stage = stage, progress = progress))
        }
    }
}

