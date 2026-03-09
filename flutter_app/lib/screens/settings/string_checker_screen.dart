import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../l10n/app_localizations.dart';

/// Comprehensive String Checker for Developers
///
/// This utility helps identify which strings exist in each language
/// and which are still missing (falling back to English).
/// Matches the Android LanguageStringChecker functionality.

/// Supported languages in the app
const List<(String, String)> supportedLanguages = [
  ('en', 'English'),
  ('es', 'Español'),
  ('de', 'Deutsch'),
  ('fr', 'Français'),
  ('pt', 'Português'),
  ('it', 'Italiano'),
  ('nl', 'Nederlands'),
  ('pl', 'Polski'),
  ('ru', 'Русский'),
  ('ja', '日本語'),
  ('ko', '한국어'),
  ('zh', '中文'),
  ('ar', 'العربية'),
  ('hi', 'हिन्दी'),
  ('tr', 'Türkçe'),
  ('sv', 'Svenska'),
  ('da', 'Dansk'),
  ('nb', 'Norsk'),
  ('fi', 'Suomi'),
  ('uk', 'Українська'),
  ('vi', 'Tiếng Việt'),
  ('th', 'ไทย'),
  ('id', 'Indonesia'),
  ('ms', 'Bahasa Melayu'),
  ('he', 'עברית'),
  ('el', 'Ελληνικά'),
  ('cs', 'Čeština'),
  ('hu', 'Magyar'),
  ('ro', 'Română'),
];

/// String categories for organized checking
enum StringCategory {
  all('All', 'all_', Icons.translate),
  navigation('Navigation', 'nav', Icons.navigation),
  auth('Authentication', 'auth', Icons.login),
  settings('Settings', 'setting', Icons.settings),
  explore('Explore', 'explore', Icons.explore),
  games('Games', 'game', Icons.sports_esports),
  messages('Messages', 'message', Icons.message),
  neuroState('Neuro States', 'neuro', Icons.psychology),
  mood('Mood', 'mood', Icons.emoji_emotions),
  profile('Profile', 'profile', Icons.person),
  accessibility('Accessibility', 'accessibility', Icons.accessibility),
  common('Common', '', Icons.more_horiz);

  final String title;
  final String prefix;
  final IconData icon;

  const StringCategory(this.title, this.prefix, this.icon);
}

/// Status of language translation
class LanguageStatus {
  final String languageCode;
  final String languageName;
  final int totalStrings;
  final int translatedStrings;
  final List<String> missingStrings;

  LanguageStatus({
    required this.languageCode,
    required this.languageName,
    required this.totalStrings,
    required this.translatedStrings,
    required this.missingStrings,
  });

  double get percentage => totalStrings > 0 ? translatedStrings / totalStrings : 0;
  bool get isComplete => percentage >= 1.0;
}

/// Get all string keys from the localization system
List<String> getAllStringKeys() {
  // Get the English strings as the base
  final englishStrings = AppLocalizationsStringChecker.getStringsForLocale('en');
  return englishStrings.keys.toList()..sort();
}

/// Get strings filtered by category
List<String> getStringsByCategory(StringCategory category) {
  final allKeys = getAllStringKeys();

  if (category == StringCategory.all) {
    return allKeys;
  }

  if (category == StringCategory.common) {
    // Get strings that don't match any other category
    final allPrefixes = StringCategory.values
        .where((c) => c != StringCategory.all && c != StringCategory.common)
        .map((c) => c.prefix)
        .toList();

    return allKeys.where((key) {
      final lowerKey = key.toLowerCase();
      return !allPrefixes.any((prefix) =>
          prefix.isNotEmpty && lowerKey.startsWith(prefix.toLowerCase()));
    }).toList();
  }

  return allKeys.where((key) {
    final lowerKey = key.toLowerCase();
    return lowerKey.startsWith(category.prefix.toLowerCase()) ||
           lowerKey.contains(category.prefix.toLowerCase());
  }).toList();
}

/// Check translation status for a specific language
LanguageStatus checkLanguageStrings(
  String languageCode,
  List<String> stringsToCheck,
) {
  final languageName = supportedLanguages
      .firstWhere((l) => l.$1 == languageCode, orElse: () => (languageCode, languageCode))
      .$2;

  // English is always complete
  if (languageCode == 'en') {
    return LanguageStatus(
      languageCode: languageCode,
      languageName: languageName,
      totalStrings: stringsToCheck.length,
      translatedStrings: stringsToCheck.length,
      missingStrings: [],
    );
  }

  final englishStrings = AppLocalizationsStringChecker.getStringsForLocale('en');
  final localizedStrings = AppLocalizationsStringChecker.getStringsForLocale(languageCode);

  final missingStrings = <String>[];
  var translatedCount = 0;

  // International terms that are often the same across languages
  final internationalTerms = {
    'appName',
    'email',
    'gameFidgetSpinner',
    'feedTitle',
    'ok',
    'cancel',
  };

  for (final key in stringsToCheck) {
    final englishValue = englishStrings[key] ?? '';
    final localizedValue = localizedStrings[key];

    if (localizedValue == null || localizedValue.isEmpty) {
      missingStrings.add(key);
    } else if (localizedValue != englishValue) {
      translatedCount++;
    } else if (internationalTerms.contains(key)) {
      // These can be the same
      translatedCount++;
    } else if (_isSymbolicOrEmpty(englishValue)) {
      // Symbolic strings can be the same
      translatedCount++;
    } else {
      missingStrings.add(key);
    }
  }

  return LanguageStatus(
    languageCode: languageCode,
    languageName: languageName,
    totalStrings: stringsToCheck.length,
    translatedStrings: translatedCount,
    missingStrings: missingStrings,
  );
}

/// Check if a string contains only symbols, numbers, or emojis
bool _isSymbolicOrEmpty(String value) {
  return value.runes.every((rune) {
    final char = String.fromCharCode(rune);
    return char.trim().isEmpty ||
           !RegExp(r'[a-zA-Z]').hasMatch(char) ||
           _isEmoji(rune);
  });
}

/// Check if a code point is an emoji
bool _isEmoji(int codePoint) {
  return (codePoint >= 0x1F600 && codePoint <= 0x1F64F) || // Emoticons
         (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) || // Misc Symbols
         (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) || // Transport
         (codePoint >= 0x1F1E0 && codePoint <= 0x1F1FF) || // Flags
         (codePoint >= 0x2600 && codePoint <= 0x26FF) ||   // Misc symbols
         (codePoint >= 0x2700 && codePoint <= 0x27BF) ||   // Dingbats
         (codePoint >= 0x1F900 && codePoint <= 0x1F9FF);   // Supplemental
}

/// Comprehensive String Checker Screen
class StringCheckerScreen extends ConsumerStatefulWidget {
  const StringCheckerScreen({super.key});

  @override
  ConsumerState<StringCheckerScreen> createState() => _StringCheckerScreenState();
}

class _StringCheckerScreenState extends ConsumerState<StringCheckerScreen> {
  StringCategory _selectedCategory = StringCategory.all;
  final Map<StringCategory, List<LanguageStatus>> _languageStatuses = {};
  bool _isLoading = false;

  int get _totalStringCount => getAllStringKeys().length;

  Map<StringCategory, int> get _categoryCounts {
    return {
      for (final category in StringCategory.values)
        category: getStringsByCategory(category).length,
    };
  }

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    if (_languageStatuses.containsKey(_selectedCategory)) return;

    setState(() => _isLoading = true);

    await Future.delayed(const Duration(milliseconds: 100)); // Allow UI to update

    final stringsToCheck = getStringsByCategory(_selectedCategory);
    final statuses = supportedLanguages.map((lang) {
      return checkLanguageStrings(lang.$1, stringsToCheck);
    }).toList();

    setState(() {
      _languageStatuses[_selectedCategory] = statuses;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final currentStatuses = _languageStatuses[_selectedCategory];

    return Scaffold(
      appBar: AppBar(
        title: const Text('String Checker'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              setState(() {
                _languageStatuses.clear();
              });
              _loadData();
            },
            tooltip: 'Refresh',
          ),
        ],
      ),
      body: Column(
        children: [
          // Header info
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(
                      Icons.translate,
                      color: theme.colorScheme.primary,
                      size: 28,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Comprehensive String Checker',
                            style: theme.textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Text(
                            '$_totalStringCount total strings · ${supportedLanguages.length} languages · ${StringCategory.values.length} categories',
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // Category chips
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.all(12),
            child: Row(
              children: StringCategory.values.map((category) {
                final count = _categoryCounts[category] ?? 0;
                final isSelected = _selectedCategory == category;

                if (count == 0 && category != StringCategory.all) {
                  return const SizedBox.shrink();
                }

                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: FilterChip(
                    selected: isSelected,
                    label: Text('${category.title} ($count)'),
                    avatar: Icon(category.icon, size: 18),
                    onSelected: (selected) {
                      setState(() => _selectedCategory = category);
                      _loadData();
                    },
                  ),
                );
              }).toList(),
            ),
          ),

          const Divider(height: 1),

          // Content
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : currentStatuses == null
                    ? const Center(child: Text('Loading...'))
                    : _buildLanguageList(currentStatuses),
          ),
        ],
      ),
    );
  }

  Widget _buildLanguageList(List<LanguageStatus> statuses) {

    // Summary counts
    final completeCount = statuses.where((s) => s.isComplete).length;
    final partialCount = statuses.where((s) => s.percentage >= 0.5 && !s.isComplete).length;
    final needsWorkCount = statuses.where((s) => s.percentage < 0.5).length;

    return CustomScrollView(
      slivers: [
        // Summary chips
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _StatusChip(
                  count: completeCount,
                  label: 'Complete',
                  color: Colors.green,
                ),
                _StatusChip(
                  count: partialCount,
                  label: 'Partial',
                  color: Colors.orange,
                ),
                _StatusChip(
                  count: needsWorkCount,
                  label: 'Needs Work',
                  color: Colors.red,
                ),
              ],
            ),
          ),
        ),

        const SliverToBoxAdapter(
          child: Divider(height: 1),
        ),

        // Language list
        SliverList(
          delegate: SliverChildBuilderDelegate(
            (context, index) {
              final status = statuses[index];
              return _LanguageStatusRow(
                status: status,
                onTap: () => _showLanguageDetail(status),
              );
            },
            childCount: statuses.length,
          ),
        ),
      ],
    );
  }

  void _showLanguageDetail(LanguageStatus status) {
    showDialog(
      context: context,
      builder: (context) => _LanguageDetailDialog(status: status),
    );
  }
}

/// Status summary chip
class _StatusChip extends StatelessWidget {
  final int count;
  final String label;
  final Color color;

  const _StatusChip({
    required this.count,
    required this.label,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        children: [
          Text(
            '$count',
            style: theme.textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
          Text(
            label,
            style: theme.textTheme.labelSmall?.copyWith(
              color: color,
            ),
          ),
        ],
      ),
    );
  }
}

/// Language status row
class _LanguageStatusRow extends StatelessWidget {
  final LanguageStatus status;
  final VoidCallback onTap;

  const _LanguageStatusRow({
    required this.status,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final statusColor = status.isComplete
        ? Colors.green
        : status.percentage >= 0.5
            ? Colors.orange
            : Colors.red;

    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            // Language code badge
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: theme.colorScheme.primaryContainer,
                borderRadius: BorderRadius.circular(12),
              ),
              alignment: Alignment.center,
              child: Text(
                status.languageCode.toUpperCase(),
                style: theme.textTheme.labelLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.colorScheme.onPrimaryContainer,
                ),
              ),
            ),
            const SizedBox(width: 16),

            // Language info and progress
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    status.languageName,
                    style: theme.textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 6),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(4),
                    child: LinearProgressIndicator(
                      value: status.percentage,
                      backgroundColor: theme.colorScheme.surfaceContainerHighest,
                      valueColor: AlwaysStoppedAnimation(statusColor),
                      minHeight: 6,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${status.translatedStrings}/${status.totalStrings} strings',
                    style: theme.textTheme.labelSmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),

            // Status icon
            Icon(
              status.isComplete
                  ? Icons.check_circle
                  : status.percentage >= 0.5
                      ? Icons.warning_amber_rounded
                      : Icons.error_outline,
              color: statusColor,
              size: 28,
            ),
          ],
        ),
      ),
    );
  }
}

/// Language detail dialog showing missing strings
class _LanguageDetailDialog extends StatelessWidget {
  final LanguageStatus status;

  const _LanguageDetailDialog({required this.status});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final statusColor = status.isComplete
        ? Colors.green
        : status.percentage >= 0.5
            ? Colors.orange
            : Colors.red;

    return AlertDialog(
      title: Row(
        children: [
          Text(
            status.languageName,
            style: theme.textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(width: 8),
          Text(
            '(${status.languageCode})',
            style: theme.textTheme.bodyMedium?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
      content: SizedBox(
        width: double.maxFinite,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Progress summary
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Translated:'),
                Text(
                  '${(status.percentage * 100).toInt()}%',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: statusColor,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            ClipRRect(
              borderRadius: BorderRadius.circular(6),
              child: LinearProgressIndicator(
                value: status.percentage,
                backgroundColor: theme.colorScheme.surfaceContainerHighest,
                valueColor: AlwaysStoppedAnimation(statusColor),
                minHeight: 10,
              ),
            ),

            if (status.missingStrings.isNotEmpty) ...[
              const SizedBox(height: 20),
              Text(
                'Missing Strings (${status.missingStrings.length}):',
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 12),
              Container(
                constraints: const BoxConstraints(maxHeight: 250),
                decoration: BoxDecoration(
                  color: theme.colorScheme.surfaceContainerHighest,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: ListView.builder(
                  shrinkWrap: true,
                  padding: const EdgeInsets.all(12),
                  itemCount: status.missingStrings.length,
                  itemBuilder: (context, index) {
                    return Padding(
                      padding: const EdgeInsets.symmetric(vertical: 2),
                      child: Text(
                        '• ${status.missingStrings[index]}',
                        style: theme.textTheme.bodySmall?.copyWith(
                          fontFamily: 'monospace',
                          color: theme.colorScheme.error,
                        ),
                      ),
                    );
                  },
                ),
              ),
            ] else ...[
              const SizedBox(height: 20),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.check_circle, color: Colors.green),
                  const SizedBox(width: 8),
                  Text(
                    'All key strings translated!',
                    style: theme.textTheme.titleSmall?.copyWith(
                      color: Colors.green,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ],
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Close'),
        ),
      ],
    );
  }
}



