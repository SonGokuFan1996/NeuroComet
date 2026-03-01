import 'package:flutter/foundation.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Secure Credential Storage for NeuroComet.
/// Mirrors the Kotlin CredentialStorage.kt using flutter_secure_storage.
///
/// Features:
/// - Secure token storage (encrypted)
/// - Session management with expiry
/// - Credential caching
/// - Simple API that reduces cognitive load
class CredentialStorageService {
  static final CredentialStorageService _instance =
      CredentialStorageService._internal();
  factory CredentialStorageService() => _instance;
  CredentialStorageService._internal();

  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
    iOptions: IOSOptions(accessibility: KeychainAccessibility.first_unlock),
  );

  // Credential keys
  static const _keyAuthToken = 'auth_token';
  static const _keyRefreshToken = 'refresh_token';
  static const _keyUserId = 'user_id';
  static const _keySessionExpiry = 'session_expiry';
  static const _keyBiometricEnrolled = 'biometric_enrolled';
  static const _keyPremiumStatus = 'premium_status';
  static const _keyLastSync = 'last_sync';

  bool _initialized = false;

  /// Initialize credential storage
  Future<void> initialize() async {
    if (_initialized) return;
    try {
      // Verify storage is accessible
      await _storage.read(key: _keyAuthToken);
      _initialized = true;
      debugPrint('CredentialStorage initialized successfully');
    } catch (e) {
      debugPrint('Failed to initialize credential storage: $e');
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // AUTH TOKEN MANAGEMENT
  // ═══════════════════════════════════════════════════════════════

  /// Store authentication token
  Future<void> saveAuthToken(String token) async {
    await _storage.write(key: _keyAuthToken, value: token);
  }

  /// Get stored authentication token
  Future<String?> getAuthToken() async {
    return await _storage.read(key: _keyAuthToken);
  }

  /// Store refresh token
  Future<void> saveRefreshToken(String token) async {
    await _storage.write(key: _keyRefreshToken, value: token);
  }

  /// Get stored refresh token
  Future<String?> getRefreshToken() async {
    return await _storage.read(key: _keyRefreshToken);
  }

  // ═══════════════════════════════════════════════════════════════
  // SESSION MANAGEMENT
  // ═══════════════════════════════════════════════════════════════

  /// Save session with expiry
  Future<void> saveSession({
    required String authToken,
    required String refreshToken,
    required String userId,
    Duration expiry = const Duration(hours: 24),
  }) async {
    final expiryTime = DateTime.now().add(expiry).toIso8601String();
    await Future.wait([
      _storage.write(key: _keyAuthToken, value: authToken),
      _storage.write(key: _keyRefreshToken, value: refreshToken),
      _storage.write(key: _keyUserId, value: userId),
      _storage.write(key: _keySessionExpiry, value: expiryTime),
    ]);
    debugPrint('Session saved, expires: $expiryTime');
  }

  /// Check if session is valid (not expired)
  Future<bool> isSessionValid() async {
    final expiryString = await _storage.read(key: _keySessionExpiry);
    if (expiryString == null) return false;

    try {
      final expiry = DateTime.parse(expiryString);
      return DateTime.now().isBefore(expiry);
    } catch (e) {
      return false;
    }
  }

  /// Get the stored user ID
  Future<String?> getUserId() async {
    return await _storage.read(key: _keyUserId);
  }

  // ═══════════════════════════════════════════════════════════════
  // PREMIUM / BIOMETRIC
  // ═══════════════════════════════════════════════════════════════

  /// Save premium status
  Future<void> savePremiumStatus(bool isPremium) async {
    await _storage.write(
      key: _keyPremiumStatus,
      value: isPremium.toString(),
    );
  }

  /// Get premium status
  Future<bool> isPremium() async {
    final value = await _storage.read(key: _keyPremiumStatus);
    return value == 'true';
  }

  /// Save biometric enrollment status
  Future<void> saveBiometricEnrolled(bool enrolled) async {
    await _storage.write(
      key: _keyBiometricEnrolled,
      value: enrolled.toString(),
    );
  }

  /// Check biometric enrollment
  Future<bool> isBiometricEnrolled() async {
    final value = await _storage.read(key: _keyBiometricEnrolled);
    return value == 'true';
  }

  // ═══════════════════════════════════════════════════════════════
  // SYNC
  // ═══════════════════════════════════════════════════════════════

  /// Save last sync timestamp
  Future<void> saveLastSync() async {
    await _storage.write(
      key: _keyLastSync,
      value: DateTime.now().toIso8601String(),
    );
  }

  /// Get last sync time
  Future<DateTime?> getLastSync() async {
    final value = await _storage.read(key: _keyLastSync);
    if (value == null) return null;
    try {
      return DateTime.parse(value);
    } catch (e) {
      return null;
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // GENERIC SECURE STORAGE
  // ═══════════════════════════════════════════════════════════════

  /// Store arbitrary secure value
  Future<void> saveSecure(String key, String value) async {
    await _storage.write(key: key, value: value);
  }

  /// Read arbitrary secure value
  Future<String?> readSecure(String key) async {
    return await _storage.read(key: key);
  }

  /// Delete arbitrary secure value
  Future<void> deleteSecure(String key) async {
    await _storage.delete(key: key);
  }

  // ═══════════════════════════════════════════════════════════════
  // CLEANUP
  // ═══════════════════════════════════════════════════════════════

  /// Clear all session data (used on sign out)
  Future<void> clearSession() async {
    await Future.wait([
      _storage.delete(key: _keyAuthToken),
      _storage.delete(key: _keyRefreshToken),
      _storage.delete(key: _keyUserId),
      _storage.delete(key: _keySessionExpiry),
    ]);
    debugPrint('Session cleared');
  }

  /// Clear all stored credentials
  Future<void> clearAll() async {
    await _storage.deleteAll();
    debugPrint('All credentials cleared');
  }
}

