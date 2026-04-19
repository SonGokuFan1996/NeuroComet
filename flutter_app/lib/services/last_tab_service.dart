import 'package:shared_preferences/shared_preferences.dart';

/// Persists and restores the last-visited bottom navigation tab so the
/// app reopens where the user left off instead of always defaulting to Feed.
class LastTabService {
  static const _key = 'last_tab_route';

  static const _allowedRoutes = {'/', '/explore', '/messages', '/notifications', '/settings'};

  /// Load the last persisted tab route. Returns `'/'` (Feed) if nothing was
  /// saved yet or if the stored value is invalid.
  static Future<String> load() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final route = prefs.getString(_key);
      if (route != null && _allowedRoutes.contains(route)) {
        return route;
      }
    } catch (_) {
      // Corrupted / unavailable → fall back to default
    }
    return '/';
  }

  /// Persist the given route. Only top-level tab routes are accepted.
  static Future<void> save(String route) async {
    if (!_allowedRoutes.contains(route)) return;
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_key, route);
    } catch (_) {
      // Best-effort; don't crash on write failure
    }
  }

  /// Clear the persisted tab (e.g. on full settings reset).
  static Future<void> clear() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove(_key);
    } catch (_) {}
  }
}

