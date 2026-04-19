import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class DeviceAuthorizationService {
  DeviceAuthorizationService._();

  static const MethodChannel _channel =
      MethodChannel('com.kyilmaz.neurocomet/device_authority');

  static Future<bool> canSkipAuth() async {
    if (kDebugMode) return true;
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) return false;

    try {
      return await _channel.invokeMethod<bool>('canSkipAuth') ?? false;
    } catch (_) {
      return false;
    }
  }

  static Future<bool> canUseDeveloperTools() async {
    if (kDebugMode) return true;
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) return false;

    try {
      return await _channel.invokeMethod<bool>('canUseDeveloperTools') ?? false;
    } catch (_) {
      return false;
    }
  }

  static Future<bool> isHouseholdAuthorizedDevice() async {
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) return false;

    try {
      return await _channel.invokeMethod<bool>('isHouseholdAuthorizedDevice') ??
          false;
    } catch (_) {
      return false;
    }
  }

  static Future<String?> getDeviceHash() async {
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) return null;

    try {
      final hash = await _channel.invokeMethod<String>('getDeviceHash');
      return (hash?.isNotEmpty ?? false) ? hash : null;
    } catch (_) {
      return null;
    }
  }

  static Future<String?> getAppSignatureHash() async {
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) return null;

    try {
      final hash = await _channel.invokeMethod<String>('getAppSignatureHash');
      return (hash?.isNotEmpty ?? false) ? hash : null;
    } catch (_) {
      return null;
    }
  }
}

