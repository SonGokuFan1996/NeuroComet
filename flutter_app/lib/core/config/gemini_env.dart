import '../utils/security_utils.dart';

/// Obfuscated Gemini credentials that match the native Android build pipeline.
///
/// Regenerate with:
///   `SecurityUtils.encrypt('<plain-text-value>')`
class GeminiEnv {
  GeminiEnv._();

  // Encrypted with SecurityUtils XOR key
  static const String _obfuscatedApiKey =
      '2f2c0f133c1a2b5824423c5100382d3a250d0a0b'
      '1c310a43083e303a08041f0b18035378521e51';

  /// Decrypted Gemini API key.
  static String get apiKey => SecurityUtils.decrypt(_obfuscatedApiKey);
}
