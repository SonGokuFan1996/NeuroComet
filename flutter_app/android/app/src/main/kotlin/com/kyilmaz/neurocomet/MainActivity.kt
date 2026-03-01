package com.kyilmaz.neurocomet

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsPickerSessionContract
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

/**
 * Flutter host activity.
 *
 * [ContactsPickerSessionContract] is an `ActivityResultContract<Intent, Uri?>`
 * (see https://developer.android.com/reference/kotlin/android/provider/ContactsPickerSessionContract).
 * Ideally it is registered via `registerForActivityResult(ContactsPickerSessionContract())`.
 *
 * However, [FlutterActivity] extends [android.app.Activity] — not
 * `ComponentActivity` — so the Activity Result API is unavailable here.
 * We therefore fall back to [startActivityForResult] / [onActivityResult]
 * while still using the official [ContactsPickerSessionContract.ACTION_PICK_CONTACTS]
 * and [ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS] constants.
 */
class MainActivity : FlutterActivity() {

    companion object {
        private const val TAG = "ContactsPicker"
        private const val CONTACTS_PICKER_CHANNEL = "com.kyilmaz.neurocomet/contacts_picker"
        private const val PICK_CONTACT_REQUEST_LEGACY = 9001
        private const val PICK_CONTACT_REQUEST_CINNAMON_BUN = 9002
    }

    private var pendingResult: MethodChannel.Result? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 17+ (CinnamonBun): Enable cross-device handoff.
        // See https://developer.android.com/reference/kotlin/android/app/Activity#sethandoffenabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            setHandoffEnabled(true)
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CONTACTS_PICKER_CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "supportsPrivacyPicker" -> {
                        // CinnamonBun+ supports the ContactsPickerSessionContract
                        result.success(Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN)
                    }
                    "needsContactsPermission" -> {
                        // On CinnamonBun+ the session-based picker doesn't need READ_CONTACTS
                        result.success(Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN)
                    }
                    "pickContact" -> {
                        pendingResult = result
                        launchContactPicker()
                    }
                    else -> result.notImplemented()
                }
            }
    }

    /**
     * Launch the correct contacts picker based on API level.
     *
     * CinnamonBun+ (Android 17): Builds an `Intent` with
     * [ContactsPickerSessionContract.ACTION_PICK_CONTACTS] and populates
     * [ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS]
     * with the MIME types for phone and email.  The system contacts picker
     * returns a session-scoped `content://` URI whose cursor exposes
     * `display_name`, `mimetype` and `data1` columns.
     * No `READ_CONTACTS` permission is required.
     *
     * Pre-CinnamonBun: Falls back to [Intent.ACTION_PICK] with
     * [ContactsContract], which requires `READ_CONTACTS`.
     */
    @Suppress("NewApi", "DEPRECATION")
    private fun launchContactPicker() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                // Android 17 (CinnamonBun) — ContactsPickerSessionContract
                val dataFields = arrayListOf(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                )
                val intent = Intent(ContactsPickerSessionContract.ACTION_PICK_CONTACTS).apply {
                    putStringArrayListExtra(
                        ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,
                        dataFields
                    )
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                }
                startActivityForResult(intent, PICK_CONTACT_REQUEST_CINNAMON_BUN)
            } else {
                // Legacy path (pre-CinnamonBun)
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = ContactsContract.Contacts.CONTENT_TYPE
                }
                startActivityForResult(intent, PICK_CONTACT_REQUEST_LEGACY)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch contacts picker", e)
            pendingResult?.error("PICKER_ERROR", "Failed to launch contacts picker: ${e.message}", null)
            pendingResult = null
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_CONTACT_REQUEST_CINNAMON_BUN -> handleCinnamonBunResult(resultCode, data)
            PICK_CONTACT_REQUEST_LEGACY -> handleLegacyResult(resultCode, data)
        }
    }

    // ── CinnamonBun (Android 17) result handler ──────────────────

    /**
     * Handle the result from the CinnamonBun [ContactsPickerSessionContract].
     *
     * The session-scoped `content://` URI can be queried for rows containing
     * `display_name`, `mimetype` and `data1` columns.
     */
    private fun handleCinnamonBunResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            val sessionUri = data.data!!
            val contactMap = mutableMapOf<String, Any?>()
            contactMap["contactUri"] = sessionUri.toString()

            try {
                contentResolver.query(
                    sessionUri,
                    arrayOf("display_name", "mimetype", "data1"),
                    null, null, null
                )?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex("display_name")
                    val mimeIdx = cursor.getColumnIndex("mimetype")
                    val dataIdx = cursor.getColumnIndex("data1")

                    while (cursor.moveToNext()) {
                        val name = if (nameIdx >= 0) cursor.getString(nameIdx) else null
                        val mime = if (mimeIdx >= 0) cursor.getString(mimeIdx) else null
                        val value = if (dataIdx >= 0) cursor.getString(dataIdx) else null

                        if (name != null && !contactMap.containsKey("displayName")) {
                            contactMap["displayName"] = name
                        }
                        when (mime) {
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE ->
                                if (!contactMap.containsKey("phoneNumber")) contactMap["phoneNumber"] = value
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ->
                                if (!contactMap.containsKey("email")) contactMap["email"] = value
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to query contacts picker session", e)
            }

            if (!contactMap.containsKey("displayName")) {
                contactMap["displayName"] = "Contact"
            }

            pendingResult?.success(contactMap)
        } else {
            pendingResult?.success(null)
        }
        pendingResult = null
    }

    // ── Legacy (pre-CinnamonBun) result handler ──────────────────

    /**
     * Handle the result from the legacy [Intent.ACTION_PICK] contacts picker.
     * Requires READ_CONTACTS permission and queries [ContactsContract] directly.
     */
    private fun handleLegacyResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            val contactUri = data.data!!
            val contactMap = mutableMapOf<String, Any?>()
            contactMap["contactUri"] = contactUri.toString()

            try {
                contentResolver.query(
                    contactUri,
                    arrayOf(
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts._ID
                    ),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                        val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                        if (nameIdx >= 0) contactMap["displayName"] = cursor.getString(nameIdx)

                        if (idIdx >= 0) {
                            val contactId = cursor.getString(idIdx)
                            resolvePhoneNumber(contactId)?.let { contactMap["phoneNumber"] = it }
                            resolveEmail(contactId)?.let { contactMap["email"] = it }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read contact details", e)
            }

            if (!contactMap.containsKey("displayName")) {
                contactMap["displayName"] = "Contact"
            }

            pendingResult?.success(contactMap)
        } else {
            pendingResult?.success(null)
        }
        pendingResult = null
    }

    // ── Legacy contact detail resolvers ──────────────────────────

    private fun resolvePhoneNumber(contactId: String): String? {
        return try {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        } catch (e: Exception) { null }
    }

    private fun resolveEmail(contactId: String): String? {
        return try {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        } catch (e: Exception) { null }
    }
}
