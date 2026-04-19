import 'dart:convert';
import 'dart:io';
import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../services/supabase_service.dart';
import '../settings/dev_options_screen.dart';

// ═══════════════════════════════════════════════════════════════
// SHARED HELPERS — device info & offline queue sync
// ═══════════════════════════════════════════════════════════════

/// Collect device information for all feedback types.
Future<Map<String, String>> getDeviceInfoMap() async {
  final plugin = DeviceInfoPlugin();
  try {
    if (Platform.isAndroid) {
      final info = await plugin.androidInfo;
      return {
        'model': '${info.manufacturer} ${info.model}',
        'os': 'Android ${info.version.release} (SDK ${info.version.sdkInt})',
        'app_version': AppConstants.appVersion,
      };
    } else if (Platform.isIOS) {
      final info = await plugin.iosInfo;
      return {
        'model': info.utsname.machine,
        'os': '${info.systemName} ${info.systemVersion}',
        'app_version': AppConstants.appVersion,
      };
    }
  } catch (_) {}
  return {'app_version': AppConstants.appVersion};
}

/// Flush any pending offline feedback to Supabase.
Future<void> flushPendingFeedback() async {
  try {
    if (!SupabaseService.isInitialized) return;
    final prefs = await SharedPreferences.getInstance();
    final pending = prefs.getStringList('pending_feedback') ?? [];
    if (pending.isEmpty) return;

    final remaining = <String>[];
    for (final jsonStr in pending) {
      try {
        final data = jsonDecode(jsonStr) as Map<String, dynamic>;
        await SupabaseService.client.from('feedback').insert(data);
        debugPrint('[Feedback] Flushed pending item');
      } catch (_) {
        remaining.add(jsonStr);
      }
    }
    await prefs.setStringList('pending_feedback', remaining);
  } catch (e) {
    debugPrint('[Feedback] flushPendingFeedback error: $e');
  }
}

// ═══════════════════════════════════════════════════════════════
// FEEDBACK HUB — root settings pane
// ═══════════════════════════════════════════════════════════════

/// Feedback Hub root screen — navigates to individual feedback panes.
class FeedbackScreen extends ConsumerStatefulWidget {
  const FeedbackScreen({super.key});

  @override
  ConsumerState<FeedbackScreen> createState() => _FeedbackScreenState();
}

class _FeedbackScreenState extends ConsumerState<FeedbackScreen> {
  @override
  void initState() {
    super.initState();
    flushPendingFeedback();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('Feedback Hub')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // ── Closed Beta banner ──
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: theme.colorScheme.tertiaryContainer,
              borderRadius: BorderRadius.circular(16),
            ),
            child: Row(
              children: [
                const Text('🧪', style: TextStyle(fontSize: 20)),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Closed Beta',
                        style: theme.textTheme.labelLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: theme.colorScheme.onTertiaryContainer,
                        ),
                      ),
                      Text(
                        'Your feedback shapes NeuroComet! All submissions are reviewed by the dev team.',
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onTertiaryContainer
                              .withAlpha(200),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // ── Navigation cards ──
          _FeedbackNavCard(
            emoji: '🐛',
            title: 'Report a Bug',
            description:
                'Found something broken? Let us know so we can fix it.',
            color: theme.colorScheme.errorContainer,
            contentColor: theme.colorScheme.onErrorContainer,
            onTap: () => context.push('/feedback/bug'),
          ),
          const SizedBox(height: 12),
          _FeedbackNavCard(
            emoji: '💡',
            title: 'Request a Feature',
            description:
                'Have an idea to make NeuroComet better? We\'d love to hear it!',
            color: theme.colorScheme.primaryContainer,
            contentColor: theme.colorScheme.onPrimaryContainer,
            onTap: () => context.push('/feedback/feature'),
          ),
          const SizedBox(height: 12),
          _FeedbackNavCard(
            emoji: '💬',
            title: 'Send Feedback',
            description:
                'Share your thoughts, suggestions, or just say hi!',
            color: theme.colorScheme.secondaryContainer,
            contentColor: theme.colorScheme.onSecondaryContainer,
            onTap: () => context.push('/feedback/general'),
          ),
          const SizedBox(height: 24),

          // ── Footer note ──
          Text(
            'All feedback is reviewed by our team. We prioritize features based on community votes and impact on neurodivergent users.',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}

class _FeedbackNavCard extends StatelessWidget {
  final String emoji;
  final String title;
  final String description;
  final Color color;
  final Color contentColor;
  final VoidCallback onTap;

  const _FeedbackNavCard({
    required this.emoji,
    required this.title,
    required this.description,
    required this.color,
    required this.contentColor,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      color: color,
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Row(
            children: [
              Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: contentColor.withAlpha(38),
                ),
                alignment: Alignment.center,
                child: Text(emoji, style: const TextStyle(fontSize: 28)),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: contentColor,
                          ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      description,
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                            color: contentColor.withAlpha(204),
                          ),
                    ),
                  ],
                ),
              ),
              Icon(Icons.chevron_right, color: contentColor.withAlpha(128)),
            ],
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════
// BUG REPORT SCREEN — standalone settings pane
// ═══════════════════════════════════════════════════════════════

class FeedbackBugReportScreen extends ConsumerStatefulWidget {
  const FeedbackBugReportScreen({super.key});

  @override
  ConsumerState<FeedbackBugReportScreen> createState() => _FeedbackBugReportScreenState();
}

class _FeedbackBugReportScreenState extends ConsumerState<FeedbackBugReportScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  String _severity = 'medium';
  bool _includeDeviceInfo = true;
  bool _isSubmitting = false;

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    // Rate limit check
    final prefs = await SharedPreferences.getInstance();
    final today = DateTime.now().toIso8601String().substring(0, 10);
    final todayCount = prefs.getInt('feedback_count_$today') ?? 0;
    final options = ref.read(devOptionsProvider);
    if (todayCount >= 5 && !options.bypassFeedbackRateLimit) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Daily feedback limit reached (5/day). Try again tomorrow.'), behavior: SnackBarBehavior.floating),
        );
      }
      return;
    }

    setState(() => _isSubmitting = true);

    try {
      final deviceInfo = _includeDeviceInfo ? await getDeviceInfoMap() : null;
      final feedbackData = {
        'type': 'bug_report',
        'title': _titleController.text.trim(),
        'description': _descriptionController.text.trim(),
        'severity': _severity,
        if (deviceInfo != null) 'device_info': jsonEncode(deviceInfo),
        'user_id': SupabaseService.currentUser?.id,
        'app_version': AppConstants.appVersion,
        'submitted_at': DateTime.now().toIso8601String(),
      };

      await flushPendingFeedback();

      try {
        if (options.forceFeedbackSubmitFailure) {
          throw Exception('Forced submission failure (dev option)');
        }
        await SupabaseService.client.from('feedback').insert(feedbackData);
      } catch (_) {
        final existing = prefs.getStringList('pending_feedback') ?? [];
        existing.add(jsonEncode(feedbackData));
        await prefs.setStringList('pending_feedback', existing);
      }

      // Increment daily count
      await prefs.setInt('feedback_count_$today', todayCount + 1);

      if (mounted) {
        _titleController.clear();
        _descriptionController.clear();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text(
                'Bug report submitted! Thank you for helping improve NeuroComet.'),
            behavior: SnackBarBehavior.floating,
          ),
        );
        if (context.canPop()) context.pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to submit: $e'),
            behavior: SnackBarBehavior.floating,
            backgroundColor: Theme.of(context).colorScheme.error,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('Report a Bug')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.error.withAlpha(20),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.bug_report, color: AppColors.error),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        'Found a bug? Let us know so we can fix it!',
                        style: theme.textTheme.bodyMedium,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              Text('Severity', style: theme.textTheme.titleSmall),
              const SizedBox(height: 8),
              SegmentedButton<String>(
                segments: const [
                  ButtonSegment(value: 'low', label: Text('Low')),
                  ButtonSegment(value: 'medium', label: Text('Medium')),
                  ButtonSegment(value: 'high', label: Text('High')),
                  ButtonSegment(value: 'critical', label: Text('Critical')),
                ],
                selected: {_severity},
                onSelectionChanged: (value) {
                  setState(() => _severity = value.first);
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _titleController,
                decoration: const InputDecoration(
                  labelText: 'Title',
                  hintText: 'Brief description of the bug',
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter a title';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _descriptionController,
                decoration: const InputDecoration(
                  labelText: 'Steps to Reproduce',
                  hintText:
                      'Describe what you were doing when the bug occurred',
                  alignLabelWithHint: true,
                ),
                maxLines: 5,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please describe the bug';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              SwitchListTile(
                title: const Text('Include device info'),
                subtitle: const Text('Helps us diagnose the issue'),
                value: _includeDeviceInfo,
                onChanged: (value) {
                  setState(() => _includeDeviceInfo = value);
                },
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                child: FilledButton(
                  onPressed: _isSubmitting ? null : _submit,
                  child: _isSubmitting
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Text('Submit Bug Report'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════
// FEATURE REQUEST SCREEN — standalone settings pane
// ═══════════════════════════════════════════════════════════════

class FeedbackFeatureRequestScreen extends ConsumerStatefulWidget {
  const FeedbackFeatureRequestScreen({super.key});

  @override
  ConsumerState<FeedbackFeatureRequestScreen> createState() => _FeedbackFeatureRequestScreenState();
}

class _FeedbackFeatureRequestScreenState extends ConsumerState<FeedbackFeatureRequestScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  String _category = 'accessibility';
  bool _isSubmitting = false;

  final List<Map<String, dynamic>> _categories = [
    {'id': 'accessibility', 'label': 'Accessibility', 'icon': Icons.accessibility_new},
    {'id': 'social', 'label': 'Social', 'icon': Icons.people},
    {'id': 'safety', 'label': 'Safety', 'icon': Icons.shield},
    {'id': 'customization', 'label': 'Customization', 'icon': Icons.palette},
    {'id': 'messaging', 'label': 'Messaging', 'icon': Icons.chat},
    {'id': 'content', 'label': 'Content', 'icon': Icons.article},
    {'id': 'performance', 'label': 'Performance', 'icon': Icons.speed},
    {'id': 'other', 'label': 'Other', 'icon': Icons.more_horiz},
  ];

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    // Rate limit check
    final prefs = await SharedPreferences.getInstance();
    final today = DateTime.now().toIso8601String().substring(0, 10);
    final todayCount = prefs.getInt('feedback_count_$today') ?? 0;
    final options = ref.read(devOptionsProvider);
    if (todayCount >= 5 && !options.bypassFeedbackRateLimit) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Daily feedback limit reached (5/day). Try again tomorrow.'), behavior: SnackBarBehavior.floating),
        );
      }
      return;
    }

    setState(() => _isSubmitting = true);

    try {
      final deviceInfo = await getDeviceInfoMap();
      final feedbackData = {
        'type': 'feature_request',
        'title': _titleController.text.trim(),
        'description': _descriptionController.text.trim(),
        'category': _category,
        'device_info': jsonEncode(deviceInfo),
        'user_id': SupabaseService.currentUser?.id,
        'app_version': AppConstants.appVersion,
        'submitted_at': DateTime.now().toIso8601String(),
      };

      await flushPendingFeedback();

      try {
        if (options.forceFeedbackSubmitFailure) {
          throw Exception('Forced submission failure (dev option)');
        }
        await SupabaseService.client.from('feedback').insert(feedbackData);
      } catch (_) {
        final existing = prefs.getStringList('pending_feedback') ?? [];
        existing.add(jsonEncode(feedbackData));
        await prefs.setStringList('pending_feedback', existing);
      }

      // Increment daily count
      await prefs.setInt('feedback_count_$today', todayCount + 1);

      if (mounted) {
        _titleController.clear();
        _descriptionController.clear();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content:
                Text('Feature request submitted! We\'ll review your idea.'),
            behavior: SnackBarBehavior.floating,
          ),
        );
        if (context.canPop()) context.pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to submit: $e'),
            behavior: SnackBarBehavior.floating,
            backgroundColor: Theme.of(context).colorScheme.error,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('Request a Feature')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.warning.withAlpha(20),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.lightbulb, color: AppColors.warning),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        'Have an idea? We\'d love to hear it!',
                        style: theme.textTheme.bodyMedium,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              Text('Category', style: theme.textTheme.titleSmall),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: _categories.map((cat) {
                  final isSelected = _category == cat['id'];
                  return ChoiceChip(
                    avatar: Icon(cat['icon'] as IconData, size: 18),
                    label: Text(cat['label'] as String),
                    selected: isSelected,
                    onSelected: (selected) {
                      if (selected) {
                        setState(() => _category = cat['id'] as String);
                      }
                    },
                  );
                }).toList(),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _titleController,
                decoration: const InputDecoration(
                  labelText: 'Feature Title',
                  hintText: 'What feature would you like?',
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter a title';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _descriptionController,
                decoration: const InputDecoration(
                  labelText: 'Description',
                  hintText: 'Describe the feature and how it would help you',
                  alignLabelWithHint: true,
                ),
                maxLines: 5,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please describe the feature';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                child: FilledButton(
                  onPressed: _isSubmitting ? null : _submit,
                  child: _isSubmitting
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Text('Submit Feature Request'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════
// GENERAL FEEDBACK SCREEN — standalone settings pane
// ═══════════════════════════════════════════════════════════════

class FeedbackGeneralScreen extends ConsumerStatefulWidget {
  const FeedbackGeneralScreen({super.key});

  @override
  ConsumerState<FeedbackGeneralScreen> createState() => _FeedbackGeneralScreenState();
}

class _FeedbackGeneralScreenState extends ConsumerState<FeedbackGeneralScreen> {
  final _formKey = GlobalKey<FormState>();
  final _feedbackController = TextEditingController();
  int _rating = 0;
  bool _isSubmitting = false;

  @override
  void dispose() {
    _feedbackController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    if (_rating == 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please select a rating'),
          behavior: SnackBarBehavior.floating,
        ),
      );
      return;
    }

    setState(() => _isSubmitting = true);

    try {
      final prefs = await SharedPreferences.getInstance();
      final today = DateTime.now().toIso8601String().substring(0, 10);
      final todayCount = prefs.getInt('feedback_count_$today') ?? 0;
      final options = ref.read(devOptionsProvider);
      if (todayCount >= 5 && !options.bypassFeedbackRateLimit) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Daily feedback limit reached (5/day). Try again tomorrow.'), behavior: SnackBarBehavior.floating),
          );
        }
        setState(() => _isSubmitting = false);
        return;
      }

      final deviceInfo = await getDeviceInfoMap();
      final feedbackData = {
        'type': 'general_feedback',
        'description': _feedbackController.text.trim(),
        'rating': _rating,
        'device_info': jsonEncode(deviceInfo),
        'user_id': SupabaseService.currentUser?.id,
        'app_version': AppConstants.appVersion,
        'submitted_at': DateTime.now().toIso8601String(),
      };

      await flushPendingFeedback();

      try {
        if (options.forceFeedbackSubmitFailure) {
          throw Exception('Forced submission failure (dev option)');
        }
        await SupabaseService.client.from('feedback').insert(feedbackData);
      } catch (_) {
        final existing = prefs.getStringList('pending_feedback') ?? [];
        existing.add(jsonEncode(feedbackData));
        await prefs.setStringList('pending_feedback', existing);
      }

      // Increment daily count
      await prefs.setInt('feedback_count_$today', todayCount + 1);

      if (mounted) {
        _feedbackController.clear();
        setState(() => _rating = 0);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Feedback submitted! Thank you for sharing.'),
            behavior: SnackBarBehavior.floating,
          ),
        );
        if (context.canPop()) context.pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to submit: $e'),
            behavior: SnackBarBehavior.floating,
            backgroundColor: Theme.of(context).colorScheme.error,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('Send Feedback')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.primaryPurple.withAlpha(20),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.feedback, color: AppColors.primaryPurple),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        'Your feedback helps us make NeuroComet better for everyone.',
                        style: theme.textTheme.bodyMedium,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              Text('How are you enjoying NeuroComet?',
                  style: theme.textTheme.titleSmall),
              const SizedBox(height: 12),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: List.generate(5, (index) {
                  final starIndex = index + 1;
                  return GestureDetector(
                    onTap: () => setState(() => _rating = starIndex),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 4),
                      child: Icon(
                        starIndex <= _rating
                            ? Icons.star
                            : Icons.star_border,
                        size: 40,
                        color: starIndex <= _rating
                            ? AppColors.warning
                            : theme.colorScheme.outline,
                      ),
                    ),
                  );
                }),
              ),
              if (_rating > 0)
                Center(
                  child: Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: Text(
                      _getRatingText(),
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: AppColors.warning,
                      ),
                    ),
                  ),
                ),
              const SizedBox(height: 24),
              TextFormField(
                controller: _feedbackController,
                decoration: const InputDecoration(
                  labelText: 'Your Feedback',
                  hintText: 'Tell us what you think...',
                  alignLabelWithHint: true,
                ),
                maxLines: 5,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter your feedback';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                child: FilledButton(
                  onPressed: _isSubmitting ? null : _submit,
                  child: _isSubmitting
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Text('Submit Feedback'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _getRatingText() {
    switch (_rating) {
      case 1:
        return 'We can do better!';
      case 2:
        return 'Room for improvement';
      case 3:
        return 'It\'s okay';
      case 4:
        return 'Pretty good!';
      case 5:
        return 'Love it! 💜';
      default:
        return '';
    }
  }
}

