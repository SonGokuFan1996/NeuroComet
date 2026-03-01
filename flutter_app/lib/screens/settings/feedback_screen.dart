import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/theme/app_colors.dart';
import '../../services/supabase_service.dart';

/// Feedback Screen - Submit bugs, feature requests, and general feedback
class FeedbackScreen extends ConsumerStatefulWidget {
  const FeedbackScreen({super.key});

  @override
  ConsumerState<FeedbackScreen> createState() => _FeedbackScreenState();
}

class _FeedbackScreenState extends ConsumerState<FeedbackScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Feedback'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(icon: Icon(Icons.bug_report), text: 'Bug'),
            Tab(icon: Icon(Icons.lightbulb), text: 'Feature'),
            Tab(icon: Icon(Icons.feedback), text: 'General'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: const [
          _BugReportTab(),
          _FeatureRequestTab(),
          _GeneralFeedbackTab(),
        ],
      ),
    );
  }
}

class _BugReportTab extends StatefulWidget {
  const _BugReportTab();

  @override
  State<_BugReportTab> createState() => _BugReportTabState();
}

class _BugReportTabState extends State<_BugReportTab> {
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

    setState(() => _isSubmitting = true);

    try {
      final feedbackData = {
        'type': 'bug_report',
        'title': _titleController.text.trim(),
        'description': _descriptionController.text.trim(),
        'severity': _severity,
        'include_device_info': _includeDeviceInfo,
        'user_id': SupabaseService.currentUser?.id,
        'submitted_at': DateTime.now().toIso8601String(),
      };

      // Attempt to send to Supabase, fall back to local storage
      try {
        await SupabaseService.client.from('feedback').insert(feedbackData);
      } catch (_) {
        // Store locally if network fails
        final prefs = await SharedPreferences.getInstance();
        final existing = prefs.getStringList('pending_feedback') ?? [];
        existing.add(jsonEncode(feedbackData));
        await prefs.setStringList('pending_feedback', existing);
      }

      if (mounted) {
        _titleController.clear();
        _descriptionController.clear();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Bug report submitted! Thank you for helping improve NeuroComet.'),
            behavior: SnackBarBehavior.floating,
          ),
        );
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

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Info card
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

            // Severity
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

            // Title
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

            // Description
            TextFormField(
              controller: _descriptionController,
              decoration: const InputDecoration(
                labelText: 'Steps to Reproduce',
                hintText: 'Describe what you were doing when the bug occurred',
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

            // Device info toggle
            SwitchListTile(
              title: const Text('Include device info'),
              subtitle: const Text('Helps us diagnose the issue'),
              value: _includeDeviceInfo,
              onChanged: (value) {
                setState(() => _includeDeviceInfo = value);
              },
            ),
            const SizedBox(height: 24),

            // Submit
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
    );
  }
}

class _FeatureRequestTab extends StatefulWidget {
  const _FeatureRequestTab();

  @override
  State<_FeatureRequestTab> createState() => _FeatureRequestTabState();
}

class _FeatureRequestTabState extends State<_FeatureRequestTab> {
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

    setState(() => _isSubmitting = true);

    try {
      final feedbackData = {
        'type': 'feature_request',
        'title': _titleController.text.trim(),
        'description': _descriptionController.text.trim(),
        'category': _category,
        'user_id': SupabaseService.currentUser?.id,
        'submitted_at': DateTime.now().toIso8601String(),
      };

      try {
        await SupabaseService.client.from('feedback').insert(feedbackData);
      } catch (_) {
        final prefs = await SharedPreferences.getInstance();
        final existing = prefs.getStringList('pending_feedback') ?? [];
        existing.add(jsonEncode(feedbackData));
        await prefs.setStringList('pending_feedback', existing);
      }

      if (mounted) {
        _titleController.clear();
        _descriptionController.clear();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Feature request submitted! We\'ll review your idea.'),
            behavior: SnackBarBehavior.floating,
          ),
        );
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

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Info card
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

            // Category
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

            // Title
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

            // Description
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

            // Submit
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
    );
  }
}

class _GeneralFeedbackTab extends StatefulWidget {
  const _GeneralFeedbackTab();

  @override
  State<_GeneralFeedbackTab> createState() => _GeneralFeedbackTabState();
}

class _GeneralFeedbackTabState extends State<_GeneralFeedbackTab> {
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
      final feedbackData = {
        'type': 'general_feedback',
        'description': _feedbackController.text.trim(),
        'rating': _rating,
        'user_id': SupabaseService.currentUser?.id,
        'submitted_at': DateTime.now().toIso8601String(),
      };

      try {
        await SupabaseService.client.from('feedback').insert(feedbackData);
      } catch (_) {
        final prefs = await SharedPreferences.getInstance();
        final existing = prefs.getStringList('pending_feedback') ?? [];
        existing.add(jsonEncode(feedbackData));
        await prefs.setStringList('pending_feedback', existing);
      }

      if (mounted) {
        _feedbackController.clear();
        setState(() => _rating = 0);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Feedback submitted! Thank you for sharing.'),
            behavior: SnackBarBehavior.floating,
          ),
        );
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

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Info card
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

            // Rating
            Text('How are you enjoying NeuroComet?', style: theme.textTheme.titleSmall),
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
                      starIndex <= _rating ? Icons.star : Icons.star_border,
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

            // Feedback
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

            // Submit
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

