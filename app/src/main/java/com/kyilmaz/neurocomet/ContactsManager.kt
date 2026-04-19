package com.kyilmaz.neurocomet

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Centralized contacts manager for NeuroComet.
 *
 * Handles:
 *  - Permission checks for READ_CONTACTS
 *  - Reading device contacts when permission is granted
 *  - Matching device contacts against app users for "Find Friends"
 *  - Persisting contacts-sync preference
 */
object ContactsManager {

    private const val TAG = "ContactsManager"
    private const val PREFS_NAME = "neurocomet_contacts"
    private const val KEY_SYNC_ENABLED = "contacts_sync_enabled"
    private const val KEY_LAST_SYNC = "last_contacts_sync"
    private const val KEY_SYNCED_COUNT = "synced_contact_count"

    private data class MutableDeviceContact(
        var displayName: String? = null,
        var photoUri: String? = null,
        val phoneNumbers: MutableSet<String> = linkedSetOf(),
        val emails: MutableSet<String> = linkedSetOf()
    )

    // ── Data classes ──

    data class DeviceContact(
        val displayName: String,
        val phoneNumbers: List<String> = emptyList(),
        val emails: List<String> = emptyList(),
        val photoUri: String? = null
    )

    data class MatchedContact(
        val deviceContact: DeviceContact,
        val appUser: User
    )

    data class ContactsSnapshot(
        val deviceContacts: List<DeviceContact> = emptyList(),
        val matchedContacts: List<MatchedContact> = emptyList()
    )

    // ── Preferences ──

    fun isContactsSyncEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SYNC_ENABLED, false)
    }

    fun setContactsSyncEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_SYNC_ENABLED, enabled)
        }
    }

    fun getLastSyncTimestamp(context: Context): Long {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_LAST_SYNC, 0L)
        } catch (_: ClassCastException) { 0L }
    }

    fun getSyncedContactCount(context: Context): Int {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_SYNCED_COUNT, 0)
        } catch (_: ClassCastException) { 0 }
    }

    private fun updateSyncMetadata(context: Context, count: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            putInt(KEY_SYNCED_COUNT, count)
        }
    }

    fun clearSyncData(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_LAST_SYNC)
            remove(KEY_SYNCED_COUNT)
            putBoolean(KEY_SYNC_ENABLED, false)
        }
    }

    // ── Permission ──

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Whether the device supports the privacy-preserving system contacts picker
     * (API 37+) which does NOT require READ_CONTACTS.
     */
    fun supportsPrivacyPicker(): Boolean = Build.VERSION.SDK_INT >= 37

    private fun normalizePhoneNumber(value: String?): String {
        return value.orEmpty().filter { it.isDigit() || it == '+' }
    }

    private fun normalizeEmail(value: String?): String {
        return value.orEmpty().trim().lowercase()
    }

    private fun resolveDisplayName(contact: MutableDeviceContact): String {
        return contact.displayName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: contact.phoneNumbers.firstOrNull()
            ?: contact.emails.firstOrNull()
            ?: "Unnamed contact"
    }

    private fun buildMatchedContacts(deviceContacts: List<DeviceContact>): List<MatchedContact> {
        val appUsers = MOCK_USERS.filter { it.id != "me" }

        val matches = mutableListOf<MatchedContact>()
        for (contact in deviceContacts) {
            val nameLower = contact.displayName.lowercase()
            for (user in appUsers) {
                val userNameLower = user.name.lowercase()
                if (nameLower.contains(userNameLower) || userNameLower.contains(nameLower)) {
                    matches.add(MatchedContact(contact, user))
                    break
                }
            }
        }
        return matches
    }

    // ── Reading device contacts ──

    /**
     * Read all device contacts. Requires READ_CONTACTS permission.
     * Returns an empty list if permission is not granted.
     */
    suspend fun readDeviceContacts(context: Context): List<DeviceContact> =
        withContext(Dispatchers.IO) {
            if (!hasContactsPermission(context)) return@withContext emptyList()

            val contacts = linkedMapOf<Long, MutableDeviceContact>()
            val cr: ContentResolver = context.contentResolver

            try {
                // Phase 1: Get all contact IDs + display names + photo URIs
                cr.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    arrayOf(
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
                    ),
                    null, null,
                    "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                    val altNameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE)
                    val photoIdx = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)

                    while (cursor.moveToNext()) {
                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else continue
                        val name = sequenceOf(
                            if (nameIdx >= 0) cursor.getString(nameIdx) else null,
                            if (altNameIdx >= 0) cursor.getString(altNameIdx) else null
                        ).mapNotNull { it?.trim()?.takeIf { value -> value.isNotEmpty() } }
                            .firstOrNull()
                        val photo = if (photoIdx >= 0) cursor.getString(photoIdx) else null
                        contacts.getOrPut(id) { MutableDeviceContact() }.apply {
                            if (!name.isNullOrBlank()) {
                                displayName = name
                            }
                            photoUri = photo ?: photoUri
                        }
                    }
                }

                // Phase 2: Attach phone numbers
                cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null, null, null
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    while (cursor.moveToNext()) {
                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else continue
                        val number = normalizePhoneNumber(if (numIdx >= 0) cursor.getString(numIdx) else continue)
                        if (number.isBlank()) continue
                        contacts.getOrPut(id) { MutableDeviceContact() }.phoneNumbers += number
                    }
                }

                // Phase 3: Attach email addresses
                cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Email.ADDRESS
                    ),
                    null, null, null
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
                    val emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)

                    while (cursor.moveToNext()) {
                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else continue
                        val email = normalizeEmail(if (emailIdx >= 0) cursor.getString(emailIdx) else continue)
                        if (email.isBlank()) continue
                        contacts.getOrPut(id) { MutableDeviceContact() }.emails += email
                    }
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "READ_CONTACTS permission revoked mid-read", e)
                return@withContext emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error reading device contacts", e)
                return@withContext emptyList()
            }
            contacts.values
                .map { contact ->
                    DeviceContact(
                        displayName = resolveDisplayName(contact),
                        phoneNumbers = contact.phoneNumbers.toList(),
                        emails = contact.emails.toList(),
                        photoUri = contact.photoUri
                    )
                }
                .sortedBy { it.displayName.lowercase() }
        }

    suspend fun loadContactsSnapshot(context: Context): ContactsSnapshot =
        withContext(Dispatchers.Default) {
            val deviceContacts = readDeviceContacts(context)
            val matchedContacts = buildMatchedContacts(deviceContacts)
            updateSyncMetadata(context, deviceContacts.size)
            ContactsSnapshot(
                deviceContacts = deviceContacts,
                matchedContacts = matchedContacts
            )
        }

    /**
     * Match device contacts against known app users by name similarity.
     * In a real app this would query the server; here we match against MOCK_USERS.
     */
    suspend fun findFriendsOnApp(context: Context): List<MatchedContact> =
        loadContactsSnapshot(context).matchedContacts

    fun findFriendsOnApp(deviceContacts: List<DeviceContact>): List<MatchedContact> {
        return buildMatchedContacts(deviceContacts)
    }

    /**
     * Quick sync: read contacts count and update metadata.
     */
    suspend fun syncContacts(context: Context): Int = withContext(Dispatchers.IO) {
        loadContactsSnapshot(context).deviceContacts.size
    }
}

