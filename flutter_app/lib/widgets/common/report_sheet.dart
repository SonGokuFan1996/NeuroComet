import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../l10n/app_localizations.dart';
import '../../services/moderation_service.dart';
import '../../services/supabase_service.dart';

/// Report sheet for reporting content or users
class ReportSheet extends ConsumerStatefulWidget {
  final String? postId;
  final String? userId;
  final String? commentId;

  const ReportSheet({
    super.key,
    this.postId,
    this.userId,
    this.commentId,
  });

  @override
  ConsumerState<ReportSheet> createState() => _ReportSheetState();
}

class _ReportSheetState extends ConsumerState<ReportSheet> {
  String? _selectedReason;
  final _detailsController = TextEditingController();
  bool _isSubmitting = false;

  final List<Map<String, dynamic>> _reportReasons = [
    {
      'id': 'spam',
      'title': 'Spam or misleading',
      'description': 'Fake engagement, scams, or misleading content',
      'icon': Icons.report_gmailerrorred,
    },
    {
      'id': 'harassment',
      'title': 'Harassment or bullying',
      'description': 'Abusive behavior, threats, or targeted attacks',
      'icon': Icons.psychology_alt,
    },
    {
      'id': 'hate_speech',
      'title': 'Hate speech',
      'description': 'Discrimination based on protected characteristics',
      'icon': Icons.do_not_disturb,
    },
    {
      'id': 'violence',
      'title': 'Violence or dangerous content',
      'description': 'Threats of violence, self-harm, or dangerous activities',
      'icon': Icons.warning_amber,
    },
    {
      'id': 'inappropriate',
      'title': 'Inappropriate content',
      'description': 'Adult content, nudity, or explicit material',
      'icon': Icons.visibility_off,
    },
    {
      'id': 'misinformation',
      'title': 'Misinformation',
      'description': 'False or misleading information about health, safety, etc.',
      'icon': Icons.info_outline,
    },
    {
      'id': 'impersonation',
      'title': 'Impersonation',
      'description': 'Pretending to be someone else',
      'icon': Icons.person_off,
    },
    {
      'id': 'other',
      'title': 'Something else',
      'description': 'Other issues not listed above',
      'icon': Icons.more_horiz,
    },
  ];

  @override
  void dispose() {
    _detailsController.dispose();
    super.dispose();
  }

  Future<void> _submitReport() async {
    if (_selectedReason == null) return;

    setState(() => _isSubmitting = true);

    try {
      // Determine content type and ID
      final String contentType;
      final String contentId;
      if (widget.postId != null) {
        contentType = 'post';
        contentId = widget.postId!;
      } else if (widget.commentId != null) {
        contentType = 'comment';
        contentId = widget.commentId!;
      } else if (widget.userId != null) {
        contentType = 'user';
        contentId = widget.userId!;
      } else {
        return;
      }

      final success = await ModerationService().reportContent(
        contentId: contentId,
        contentType: contentType,
        reason: _selectedReason!,
        details: _detailsController.text.isNotEmpty ? _detailsController.text : null,
        reporterId: SupabaseService.currentUser?.id ?? 'anonymous',
      );

      if (mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(success
                ? 'Report submitted. Thank you for helping keep our community safe.'
                : 'Report could not be submitted. Please try again.'),
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to submit report: $e'),
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
    final l10n = AppLocalizations.of(context)!;

    return DraggableScrollableSheet(
      initialChildSize: 0.9,
      minChildSize: 0.5,
      maxChildSize: 0.95,
      expand: false,
      builder: (context, controller) {
        return Container(
          decoration: BoxDecoration(
            color: theme.scaffoldBackgroundColor,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
          ),
          child: Column(
            children: [
              // Handle
              Container(
                margin: const EdgeInsets.symmetric(vertical: 12),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: theme.dividerColor,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),

              // Header
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Row(
                  children: [
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () => Navigator.pop(context),
                    ),
                    Expanded(
                      child: Text(
                        l10n.report,
                        style: theme.textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                    const SizedBox(width: 48), // Balance the close button
                  ],
                ),
              ),

              const Divider(),

              // Content
              Expanded(
                child: _selectedReason == null
                    ? _buildReasonsList(controller)
                    : _buildDetailsForm(controller),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildReasonsList(ScrollController controller) {
    final theme = Theme.of(context);

    return ListView(
      controller: controller,
      padding: const EdgeInsets.all(16),
      children: [
        Text(
          'Why are you reporting this?',
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'Your report is anonymous. We won\'t tell anyone you reported this.',
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 16),
        ..._reportReasons.map((reason) => _ReasonTile(
          title: reason['title'] as String,
          description: reason['description'] as String,
          icon: reason['icon'] as IconData,
          onTap: () {
            setState(() => _selectedReason = reason['id'] as String);
          },
        )),
      ],
    );
  }

  Widget _buildDetailsForm(ScrollController controller) {
    final theme = Theme.of(context);
    final selectedReason = _reportReasons.firstWhere(
      (r) => r['id'] == _selectedReason,
    );

    return ListView(
      controller: controller,
      padding: const EdgeInsets.all(16),
      children: [
        // Back button
        TextButton.icon(
          onPressed: () => setState(() => _selectedReason = null),
          icon: const Icon(Icons.arrow_back),
          label: const Text('Back'),
          style: TextButton.styleFrom(
            alignment: Alignment.centerLeft,
            padding: EdgeInsets.zero,
          ),
        ),
        const SizedBox(height: 16),

        // Selected reason
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: theme.colorScheme.primaryContainer.withAlpha(50),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: theme.colorScheme.primary.withAlpha(50),
            ),
          ),
          child: Row(
            children: [
              Icon(
                selectedReason['icon'] as IconData,
                color: theme.colorScheme.primary,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      selectedReason['title'] as String,
                      style: theme.textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    Text(
                      selectedReason['description'] as String,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 24),

        // Additional details
        Text(
          'Additional details (optional)',
          style: theme.textTheme.titleSmall,
        ),
        const SizedBox(height: 8),
        TextField(
          controller: _detailsController,
          maxLines: 4,
          maxLength: 500,
          decoration: InputDecoration(
            hintText: 'Provide any additional context that might help us understand the issue...',
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
        ),
        const SizedBox(height: 24),

        // Submit button
        FilledButton(
          onPressed: _isSubmitting ? null : _submitReport,
          child: _isSubmitting
              ? const SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : const Text('Submit Report'),
        ),
        const SizedBox(height: 16),

        // Info text
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: theme.colorScheme.surfaceContainerHighest.withAlpha(100),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Row(
            children: [
              Icon(
                Icons.info_outline,
                size: 20,
                color: theme.colorScheme.outline,
              ),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  'We take all reports seriously and will review this within 24 hours.',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _ReasonTile extends StatelessWidget {
  final String title;
  final String description;
  final IconData icon;
  final VoidCallback onTap;

  const _ReasonTile({
    required this.title,
    required this.description,
    required this.icon,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: theme.dividerColor),
      ),
      child: ListTile(
        onTap: onTap,
        leading: Icon(icon, color: theme.colorScheme.primary),
        title: Text(title),
        subtitle: Text(
          description,
          style: theme.textTheme.bodySmall,
        ),
        trailing: const Icon(Icons.chevron_right),
      ),
    );
  }
}

/// Block confirmation dialog
class BlockUserDialog extends StatelessWidget {
  final String userId;
  final String userName;

  const BlockUserDialog({
    super.key,
    required this.userId,
    required this.userName,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return AlertDialog(
      title: Text('Block $userName?'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('When you block someone:'),
          const SizedBox(height: 12),
          _buildBullet(context, 'They won\'t be able to see your profile or posts'),
          _buildBullet(context, 'They won\'t be able to message you'),
          _buildBullet(context, 'They won\'t be notified that you blocked them'),
          _buildBullet(context, 'Any existing conversation will be removed'),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context, false),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: () => Navigator.pop(context, true),
          style: FilledButton.styleFrom(
            backgroundColor: theme.colorScheme.error,
          ),
          child: const Text('Block'),
        ),
      ],
    );
  }

  Widget _buildBullet(BuildContext context, String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('• '),
          Expanded(
            child: Text(
              text,
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ),
        ],
      ),
    );
  }
}

/// Mute options sheet
class MuteOptionsSheet extends StatefulWidget {
  final String userId;
  final String userName;

  const MuteOptionsSheet({
    super.key,
    required this.userId,
    required this.userName,
  });

  @override
  State<MuteOptionsSheet> createState() => _MuteOptionsSheetState();
}

class _MuteOptionsSheetState extends State<MuteOptionsSheet> {
  bool _mutePosts = true;
  bool _muteStories = true;
  String _duration = 'forever';

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).padding.bottom + 16,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            margin: const EdgeInsets.symmetric(vertical: 12),
            width: 40,
            height: 4,
            decoration: BoxDecoration(
              color: theme.dividerColor,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              'Mute ${widget.userName}',
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          const Divider(),
          CheckboxListTile(
            title: const Text('Mute posts'),
            subtitle: const Text('Hide their posts from your feed'),
            value: _mutePosts,
            onChanged: (value) {
              setState(() => _mutePosts = value ?? false);
            },
          ),
          CheckboxListTile(
            title: const Text('Mute stories'),
            subtitle: const Text('Hide their stories'),
            value: _muteStories,
            onChanged: (value) {
              setState(() => _muteStories = value ?? false);
            },
          ),
          const Divider(),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Duration',
                  style: theme.textTheme.titleSmall,
                ),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 8,
                  children: [
                    ChoiceChip(
                      label: const Text('24 hours'),
                      selected: _duration == '24h',
                      onSelected: (selected) {
                        if (selected) setState(() => _duration = '24h');
                      },
                    ),
                    ChoiceChip(
                      label: const Text('7 days'),
                      selected: _duration == '7d',
                      onSelected: (selected) {
                        if (selected) setState(() => _duration = '7d');
                      },
                    ),
                    ChoiceChip(
                      label: const Text('Forever'),
                      selected: _duration == 'forever',
                      onSelected: (selected) {
                        if (selected) setState(() => _duration = 'forever');
                      },
                    ),
                  ],
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: () {
                  Navigator.pop(context, {
                    'mutePosts': _mutePosts,
                    'muteStories': _muteStories,
                    'duration': _duration,
                  });
                },
                child: const Text('Mute'),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Content warning overlay
class ContentWarningOverlay extends StatelessWidget {
  final String warningType;
  final VoidCallback onShow;
  final VoidCallback? onHide;

  const ContentWarningOverlay({
    super.key,
    required this.warningType,
    required this.onShow,
    this.onHide,
  });

  String get _warningTitle {
    switch (warningType) {
      case 'sensitive':
        return 'Sensitive Content';
      case 'spoiler':
        return 'Spoiler Warning';
      case 'trigger':
        return 'Trigger Warning';
      default:
        return 'Content Warning';
    }
  }

  String get _warningDescription {
    switch (warningType) {
      case 'sensitive':
        return 'This content may be sensitive for some viewers.';
      case 'spoiler':
        return 'This post contains spoilers.';
      case 'trigger':
        return 'This content discusses topics that may be triggering.';
      default:
        return 'This content has been flagged with a warning.';
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      color: theme.colorScheme.surfaceContainerHighest,
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                Icons.visibility_off,
                size: 40,
                color: theme.colorScheme.outline,
              ),
              const SizedBox(height: 16),
              Text(
                _warningTitle,
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                _warningDescription,
                textAlign: TextAlign.center,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 16),
              OutlinedButton(
                onPressed: onShow,
                child: const Text('Show Content'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

