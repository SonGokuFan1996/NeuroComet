import '../utils/security_utils.dart';

/// Obfuscated Supabase credentials that match the native Android build pipeline.
///
/// These are XOR-encrypted with the same key used in `build.gradle.kts`.
/// The anon key is designed to be client-side (Row Level Security protects data),
/// but we still obfuscate to prevent trivial string-scanning of the binary.
///
/// Regenerate with:
///   `SecurityUtils.encrypt('<plain-text-value>')`
class SupabaseEnv {
  SupabaseEnv._();

  // Encrypted with SecurityUtils XOR key
  static const String _obfuscatedUrl =
      '061101021c59404206103e0c0719100103140a28150c1b11'
      '02061109711810093e5051415040061a';

  static const String _obfuscatedAnonKey =
      '0b1c3f1a0d240c042a1d15203b0e2c432008252c3a0b3140'
      '112a3d4f1600152109717a0b1b0b1c3f020c5022042a1d15'
      '130a2c271a370c2a2529362a063b073e1505022c4f165f7e'
      '596c393305103735150f3d22320d5c2e151729331b3d4133'
      '143c1b1e1d1a325216230c7b067b58281017405b0a232e2f'
      '0406313f1d2a182b52223526502e1f3b10392d1a182c1409'
      '0653717c58281f335b2e3b3c503b1b2016393d4240545a2a'
      '4a37133b3f271d48371e54172b735f75731910042d3e4e1b'
      '35213c151e59262d3f5c115d263d2636';

  /// Decrypted Supabase project URL.
  static String get url => SecurityUtils.decrypt(_obfuscatedUrl);

  /// Decrypted Supabase anon key.
  static String get anonKey => SecurityUtils.decrypt(_obfuscatedAnonKey);
}

