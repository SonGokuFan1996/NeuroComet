import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Privacy Manager - Handles all privacy-related functionality across the app.
/// This includes account privacy, blocking, muting, and activity status.
/// Mirrors the Kotlin PrivacyManager.kt
class PrivacyService {
  static final PrivacyService _instance = PrivacyService._internal();
  factory PrivacyService() => _instance;
  PrivacyService._internal();
  static const String _keyBlockedUsers = 'blocked_users';
  static const String _keyMutedUsers = 'muted_users';
  static const String _keyMutedWords = 'muted_words';
  static const String _keyHiddenPosts = 'hidden_posts';
  static const String _keyRestrictedUsers = 'restricted_users';

  final ValueNotifier<Set<String>> blockedUsers = ValueNotifier({});
  final ValueNotifier<Set<String>> mutedUsers = ValueNotifier({});
  final ValueNotifier<Set<String>> mutedWords = ValueNotifier({});
  final ValueNotifier<Set<String>> hiddenPosts = ValueNotifier({});
  final ValueNotifier<Set<String>> restrictedUsers = ValueNotifier({});

  SharedPreferences? _prefs;

  /// Initialize privacy manager and load persisted data
  Future<void> initialize() async {
    _prefs = await SharedPreferences.getInstance();
    blockedUsers.value = (_prefs?.getStringList(_keyBlockedUsers) ?? []).toSet();
    mutedUsers.value = (_prefs?.getStringList(_keyMutedUsers) ?? []).toSet();
    mutedWords.value = (_prefs?.getStringList(_keyMutedWords) ?? []).toSet();
    hiddenPosts.value = (_prefs?.getStringList(_keyHiddenPosts) ?? []).toSet();
    restrictedUsers.value = (_prefs?.getStringList(_keyRestrictedUsers) ?? []).toSet();
    debugPrint('PrivacyService initialized');
  }

  // === Blocking ===

  Future<void> blockUser(String userId) async {
    final updated = {...blockedUsers.value, userId};
    blockedUsers.value = updated;
    await _prefs?.setStringList(_keyBlockedUsers, updated.toList());
  }

  Future<void> unblockUser(String userId) async {
    final updated = {...blockedUsers.value}..remove(userId);
    blockedUsers.value = updated;
    await _prefs?.setStringList(_keyBlockedUsers, updated.toList());
  }

  bool isUserBlocked(String userId) => blockedUsers.value.contains(userId);

  // === Muting Users ===

  Future<void> muteUser(String userId) async {
    final updated = {...mutedUsers.value, userId};
    mutedUsers.value = updated;
    await _prefs?.setStringList(_keyMutedUsers, updated.toList());
  }

  Future<void> unmuteUser(String userId) async {
    final updated = {...mutedUsers.value}..remove(userId);
    mutedUsers.value = updated;
    await _prefs?.setStringList(_keyMutedUsers, updated.toList());
  }

  bool isUserMuted(String userId) => mutedUsers.value.contains(userId);

  // === Muting Words ===

  Future<void> addMutedWord(String word) async {
    final normalized = word.trim().toLowerCase();
    if (normalized.isEmpty) return;
    final updated = {...mutedWords.value, normalized};
    mutedWords.value = updated;
    await _prefs?.setStringList(_keyMutedWords, updated.toList());
  }

  Future<void> removeMutedWord(String word) async {
    final normalized = word.trim().toLowerCase();
    final updated = {...mutedWords.value}..remove(normalized);
    mutedWords.value = updated;
    await _prefs?.setStringList(_keyMutedWords, updated.toList());
  }

  bool containsMutedWord(String text) {
    final lowerText = text.toLowerCase();
    return mutedWords.value.any((word) => lowerText.contains(word));
  }

  // === Hidden Posts ===

  Future<void> hidePost(String postId) async {
    final updated = {...hiddenPosts.value, postId};
    hiddenPosts.value = updated;
    await _prefs?.setStringList(_keyHiddenPosts, updated.toList());
  }

  Future<void> unhidePost(String postId) async {
    final updated = {...hiddenPosts.value}..remove(postId);
    hiddenPosts.value = updated;
    await _prefs?.setStringList(_keyHiddenPosts, updated.toList());
  }

  bool isPostHidden(String postId) => hiddenPosts.value.contains(postId);

  // === Restricted Users ===

  Future<void> restrictUser(String userId) async {
    final updated = {...restrictedUsers.value, userId};
    restrictedUsers.value = updated;
    await _prefs?.setStringList(_keyRestrictedUsers, updated.toList());
  }

  Future<void> unrestrictUser(String userId) async {
    final updated = {...restrictedUsers.value}..remove(userId);
    restrictedUsers.value = updated;
    await _prefs?.setStringList(_keyRestrictedUsers, updated.toList());
  }

  bool isUserRestricted(String userId) => restrictedUsers.value.contains(userId);

  // === Content Filtering ===

  /// Check if content from a user should be shown
  bool shouldShowContent({required String userId, String? postId}) {
    if (isUserBlocked(userId)) return false;
    if (isUserMuted(userId)) return false;
    if (postId != null && isPostHidden(postId)) return false;
    return true;
  }

  // === Utility ===

  /// Clear all privacy settings (used on sign out)
  Future<void> clearAll() async {
    blockedUsers.value = {};
    mutedUsers.value = {};
    mutedWords.value = {};
    hiddenPosts.value = {};
    restrictedUsers.value = {};
    await _prefs?.remove(_keyBlockedUsers);
    await _prefs?.remove(_keyMutedUsers);
    await _prefs?.remove(_keyMutedWords);
    await _prefs?.remove(_keyHiddenPosts);
    await _prefs?.remove(_keyRestrictedUsers);
  }
}

