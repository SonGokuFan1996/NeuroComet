import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

/// Contact information returned from the system contacts picker.
class PickedContact {
  final String displayName;
  final String? phoneNumber;
  final String? email;
  final String? contactUri;

  const PickedContact({
    required this.displayName,
    this.phoneNumber,
    this.email,
    this.contactUri,
  });

  factory PickedContact.fromMap(Map<String, dynamic> map) {
    return PickedContact(
      displayName: map['displayName'] as String? ?? 'Contact',
      phoneNumber: map['phoneNumber'] as String?,
      email: map['email'] as String?,
      contactUri: map['contactUri'] as String?,
    );
  }

  Map<String, dynamic> toMap() => {
        'displayName': displayName,
        'phoneNumber': phoneNumber,
        'email': email,
        'contactUri': contactUri,
      };
}

/// Service for picking contacts using the platform-native contacts picker.
///
/// On Android 17 (CinnamonBun), this uses [ContactsPickerSessionContract]
/// — an `ActivityResultContract<Intent, Uri?>` — with
/// `ACTION_PICK_CONTACTS` and `EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS`.
/// The system handles user consent through a privacy-preserving UI and
/// returns a session-scoped `content://` URI whose cursor exposes
/// `display_name`, `mimetype` and `data1` columns.
/// No `READ_CONTACTS` permission is needed.
///
/// On older Android versions, falls back to the standard ACTION_PICK intent,
/// which requires READ_CONTACTS permission.
///
/// On iOS, uses CNContactPickerViewController.
class ContactsPickerService {
  static const MethodChannel _channel =
      MethodChannel('com.kyilmaz.neurocomet/contacts_picker');

  static final ContactsPickerService _instance =
      ContactsPickerService._internal();
  factory ContactsPickerService() => _instance;
  ContactsPickerService._internal();

  /// Whether the device supports the privacy-preserving contacts picker
  /// (Android API 37+). Returns false on iOS and older Android.
  Future<bool> supportsPrivacyPicker() async {
    if (!Platform.isAndroid) return false;
    try {
      final result = await _channel.invokeMethod<bool>('supportsPrivacyPicker');
      return result ?? false;
    } catch (e) {
      debugPrint('ContactsPickerService: supportsPrivacyPicker failed: $e');
      return false;
    }
  }

  /// Launch the contacts picker and return the selected contact.
  /// Returns null if the user cancelled or an error occurred.
  Future<PickedContact?> pickContact() async {
    try {
      final result = await _channel.invokeMapMethod<String, dynamic>('pickContact');
      if (result == null) return null;
      return PickedContact.fromMap(result);
    } on PlatformException catch (e) {
      debugPrint('ContactsPickerService: pickContact failed: ${e.message}');
      return null;
    } catch (e) {
      debugPrint('ContactsPickerService: pickContact error: $e');
      return null;
    }
  }

  /// Check if contacts permission is needed (pre-API 37 only).
  /// On API 37+ this always returns false since the system picker
  /// doesn't require the permission.
  Future<bool> needsContactsPermission() async {
    if (!Platform.isAndroid) return false;
    try {
      final result =
          await _channel.invokeMethod<bool>('needsContactsPermission');
      return result ?? true;
    } catch (e) {
      // If the channel isn't set up yet, assume permission is needed
      return true;
    }
  }
}

