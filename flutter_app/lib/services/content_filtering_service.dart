import 'package:flutter/foundation.dart';

/// Content filtering and audience-based post visibility.
/// Mirrors the Kotlin ContentFiltering.kt
class ContentFilteringService {
  static final ContentFilteringService _instance =
      ContentFilteringService._internal();
  factory ContentFilteringService() => _instance;
  ContentFilteringService._internal();

  /// Check if a post can be viewed by the given audience.
  /// UNDER_13 can only see UNDER_13 posts.
  /// TEEN can see UNDER_13 and TEEN posts.
  /// ADULT can see all posts.
  bool canViewPost(Audience postAudience, Audience userAudience) {
    switch (userAudience) {
      case Audience.under13:
        return postAudience == Audience.under13;
      case Audience.teen:
        return postAudience == Audience.under13 ||
            postAudience == Audience.teen;
      case Audience.adult:
        return true;
    }
  }

  /// Filters a list of posts by audience level
  List<T> filterByAudience<T>(
    List<T> items,
    Audience Function(T) getAudience,
    Audience userAudience,
  ) {
    return items.where((item) => canViewPost(getAudience(item), userAudience)).toList();
  }

  /// Check if text should be hidden for kids mode
  bool shouldHideTextForKids(String content, KidsFilterLevel level) {
    if (content.length < 5) return true;
    if (level == KidsFilterLevel.strict &&
        content.toLowerCase().contains('violence')) return true;
    return false;
  }

  /// Sanitize content for kids mode
  String sanitizeForKids(String content, KidsFilterLevel level) {
    switch (level) {
      case KidsFilterLevel.strict:
        return content.replaceAll(
          RegExp('bad word', caseSensitive: false),
          'good word',
        );
      case KidsFilterLevel.moderate:
        return content;
    }
  }

  /// Check content for sensitive material
  ContentCheckResult checkContent(String content) {
    final lowerContent = content.toLowerCase();
    final warnings = <String>[];

    if (_containsSelfHarmContent(lowerContent)) {
      warnings.add('self_harm');
    }
    if (_containsViolenceContent(lowerContent)) {
      warnings.add('violence');
    }
    if (_containsSensitiveContent(lowerContent)) {
      warnings.add('sensitive');
    }

    return ContentCheckResult(
      isClean: warnings.isEmpty,
      warnings: warnings,
    );
  }

  bool _containsSelfHarmContent(String text) {
    const triggers = ['self harm', 'cutting', 'suicide'];
    return triggers.any((t) => text.contains(t));
  }

  bool _containsViolenceContent(String text) {
    const triggers = ['violence', 'attack', 'hurt'];
    return triggers.any((t) => text.contains(t));
  }

  bool _containsSensitiveContent(String text) {
    const triggers = ['trigger warning', 'tw', 'cw'];
    return triggers.any((t) => text.contains(t));
  }
}

/// Audience levels for content filtering
enum Audience {
  under13,
  teen,
  adult,
}

/// Kids filter strictness levels
enum KidsFilterLevel {
  strict,
  moderate,
}

/// Result of a content check
class ContentCheckResult {
  final bool isClean;
  final List<String> warnings;

  const ContentCheckResult({
    required this.isClean,
    required this.warnings,
  });
}

