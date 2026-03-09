import 'package:flutter/foundation.dart';
import 'package:flutter_contacts/flutter_contacts.dart';
import 'webrtc_call_service.dart';

/// Unified contact that can be called — merges device contacts with app users.
class CallableContact {
  final String id;
  final String name;
  final String? phoneNumber;
  final String? photoUri;
  final Uint8List? photoBytes;
  final bool isAppUser;

  const CallableContact({
    required this.id,
    required this.name,
    this.phoneNumber,
    this.photoUri,
    this.photoBytes,
    this.isAppUser = false,
  });
}

/// Service that bridges device contacts with the WebRTC calling system.
/// Reads the user's real phone contacts and makes them available for
/// voice/video calling within the app.
class ContactsCallService extends ChangeNotifier {
  static final ContactsCallService _instance = ContactsCallService._();
  static ContactsCallService get instance => _instance;
  ContactsCallService._();

  bool _hasPermission = false;
  bool get hasPermission => _hasPermission;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  List<Contact> _deviceContacts = [];
  List<Contact> get deviceContacts => _deviceContacts;

  List<CallableContact> _callableContacts = [];
  List<CallableContact> get callableContacts => _callableContacts;

  /// Check if contacts permission is granted.
  Future<bool> checkPermission() async {
    _hasPermission = await FlutterContacts.requestPermission(readonly: true);
    notifyListeners();
    return _hasPermission;
  }

  /// Request contacts permission and load contacts if granted.
  Future<bool> requestPermissionAndLoad() async {
    _hasPermission = await FlutterContacts.requestPermission(readonly: true);
    if (_hasPermission) {
      await loadContacts();
    }
    notifyListeners();
    return _hasPermission;
  }

  /// Load device contacts and merge with app users.
  Future<void> loadContacts() async {
    if (!_hasPermission) return;

    _isLoading = true;
    notifyListeners();

    try {
      _deviceContacts = await FlutterContacts.getContacts(
        withProperties: true,
        withPhoto: true,
        withThumbnail: true,
      );

      _buildCallableContacts();
    } catch (e) {
      debugPrint('ContactsCallService: Error loading contacts: $e');
    }

    _isLoading = false;
    notifyListeners();
  }

  /// Build the unified callable contacts list.
  void _buildCallableContacts() {
    final result = <CallableContact>[];
    final addedNames = <String>{};

    // Device contacts with phone numbers
    for (final contact in _deviceContacts) {
      if (contact.phones.isEmpty) continue;
      final name = contact.displayName;
      if (name.isEmpty) continue;
      if (addedNames.contains(name.toLowerCase())) continue;

      addedNames.add(name.toLowerCase());
      result.add(CallableContact(
        id: 'device_${contact.id}',
        name: name,
        phoneNumber: contact.phones.first.number,
        photoBytes: contact.thumbnail,
        isAppUser: false, // Could cross-reference with Supabase users
      ));
    }

    // Sort alphabetically
    result.sort((a, b) => a.name.compareTo(b.name));

    _callableContacts = result;
  }

  /// Start a voice call to a contact.
  void startVoiceCall(CallableContact contact) {
    WebRTCCallService.instance.startCall(
      recipientId: contact.id,
      recipientName: contact.name,
      recipientAvatar: contact.photoUri ?? '',
      callType: CallType.voice,
    );
  }

  /// Start a video call to a contact.
  void startVideoCall(CallableContact contact) {
    WebRTCCallService.instance.startCall(
      recipientId: contact.id,
      recipientName: contact.name,
      recipientAvatar: contact.photoUri ?? '',
      callType: CallType.video,
    );
  }
}

