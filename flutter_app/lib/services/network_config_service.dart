import 'dart:io';
import 'package:flutter/foundation.dart';

/// Network configuration and API client for NeuroComet.
/// Mirrors the Kotlin NetworkConfig.kt
///
/// SECURITY DESIGN:
/// - No API keys stored in client code
/// - Authentication handled via secure session tokens
/// - Server-side endpoints handle their own auth requirements
/// - The app can function in offline/mock mode when server is unavailable
class NetworkConfigService {
  static final NetworkConfigService _instance =
      NetworkConfigService._internal();
  factory NetworkConfigService() => _instance;
  NetworkConfigService._internal();

  static const String defaultBaseUrl = 'http://localhost:54321';

  String? _baseUrl;

  /// The resolved base URL for API requests
  String get baseUrl => _baseUrl ?? defaultBaseUrl;

  // Timeout settings (in milliseconds)
  static const int connectTimeout = 10000;
  static const int readTimeout = 30000;

  /// Initialize network config.
  /// Accepts an optional [configuredUrl] (e.g., from environment or remote config).
  void initialize({String? configuredUrl}) {
    try {
      if (configuredUrl != null && configuredUrl.isNotEmpty) {
        _baseUrl = configuredUrl.endsWith('/')
            ? configuredUrl.substring(0, configuredUrl.length - 1)
            : configuredUrl;
        debugPrint('NetworkConfig: Using configured base URL');
      } else {
        debugPrint(
            'NetworkConfig: No base URL configured, using default: $defaultBaseUrl');
      }
    } catch (e) {
      debugPrint('NetworkConfig: Failed to read config, using default: $e');
    }
  }

  /// Check if the server is reachable
  Future<bool> isServerAvailable() async {
    try {
      final uri = Uri.parse('$baseUrl/rest/v1/');
      final client = HttpClient();
      client.connectionTimeout = const Duration(milliseconds: 5000);
      final request = await client.headUrl(uri);
      final response = await request.close();
      client.close();
      return response.statusCode >= 200 && response.statusCode < 400;
    } catch (e) {
      debugPrint('NetworkConfig: Server not available: $e');
      return false;
    }
  }

  /// Build a full API URL from a path
  String buildUrl(String path) {
    final cleanPath = path.startsWith('/') ? path : '/$path';
    return '$baseUrl$cleanPath';
  }

  /// Get default request headers
  Map<String, String> getDefaultHeaders({String? authToken}) {
    final headers = <String, String>{
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'X-Client-Platform': kIsWeb ? 'web' : Platform.operatingSystem,
    };
    if (authToken != null) {
      headers['Authorization'] = 'Bearer $authToken';
    }
    return headers;
  }
}

