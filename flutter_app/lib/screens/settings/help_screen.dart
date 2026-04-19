import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../core/theme/app_colors.dart';

/// Help and Support screen
class HelpScreen extends ConsumerWidget {
  const HelpScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Help & Support'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Quick help section
          _buildQuickHelpCard(context),
          const SizedBox(height: 24),

          // FAQ section
          _buildSectionHeader(context, 'Frequently Asked Questions'),
          const _FAQSection(),
          const SizedBox(height: 24),

          // Contact section
          _buildSectionHeader(context, 'Contact Us'),
          Card(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.email_outlined),
                  title: const Text('Email Support'),
                  subtitle: const Text('support@getneurocomet.com'),
                  trailing: const Icon(Icons.copy),
                  onTap: () {
                    Clipboard.setData(
                      const ClipboardData(text: 'support@getneurocomet.com'),
                    );
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Email copied to clipboard'),
                        behavior: SnackBarBehavior.floating,
                      ),
                    );
                  },
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.chat_outlined),
                  title: const Text('Live Chat'),
                  subtitle: const Text('Available 9am-6pm EST'),
                  trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                  onTap: () => _openLiveChat(context),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.bug_report_outlined),
                  title: const Text('Report a Bug'),
                  trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                  onTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const BugReportScreen(),
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Resources section
          _buildSectionHeader(context, 'Resources'),
          Card(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.menu_book_outlined),
                  title: const Text('User Guide'),
                  trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                  onTap: () => _openUrl('https://getneurocomet.com/guide'),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.video_library_outlined),
                  title: const Text('Video Tutorials'),
                  trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                  onTap: () => _openUrl('https://getneurocomet.com/tutorials'),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.groups_outlined),
                  title: const Text('Community Forum'),
                  trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                  onTap: () => _openUrl('https://community.getneurocomet.com'),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Crisis resources
          _buildSectionHeader(context, 'Crisis Resources'),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.calmBlue.withAlpha(30),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(
                color: AppColors.calmBlue.withAlpha(100),
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(
                      Icons.favorite,
                      color: AppColors.calmBlue,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      'You\'re not alone',
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  'If you\'re struggling, please reach out to one of these resources:',
                  style: theme.textTheme.bodySmall,
                ),
                const SizedBox(height: 12),
                _buildCrisisResource(
                  context,
                  'National Suicide Prevention Lifeline',
                  '988',
                ),
                _buildCrisisResource(
                  context,
                  'Crisis Text Line',
                  'Text HOME to 741741',
                ),
                _buildCrisisResource(
                  context,
                  'International Association for Suicide Prevention',
                  'https://www.iasp.info/resources/Crisis_Centres/',
                ),
              ],
            ),
          ),
          const SizedBox(height: 32),
        ],
      ),
    );
  }

  Widget _buildQuickHelpCard(BuildContext context) {

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.primaryPurple,
            AppColors.secondaryTeal,
          ],
        ),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(Icons.help_outline, color: Colors.white, size: 32),
          const SizedBox(height: 12),
          const Text(
            'How can we help?',
            style: TextStyle(
              color: Colors.white,
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          const Text(
            'Search for answers or browse topics below',
            style: TextStyle(color: Colors.white70),
          ),
          const SizedBox(height: 16),
          TextField(
            decoration: InputDecoration(
              hintText: 'Search help topics...',
              filled: true,
              fillColor: Colors.white,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide.none,
              ),
              prefixIcon: const Icon(Icons.search),
              contentPadding: const EdgeInsets.symmetric(
                horizontal: 16,
                vertical: 12,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleSmall?.copyWith(
          fontWeight: FontWeight.w600,
          color: Theme.of(context).colorScheme.primary,
        ),
      ),
    );
  }

  Widget _buildCrisisResource(
    BuildContext context,
    String name,
    String contact,
  ) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          const Icon(Icons.phone, size: 16),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  contact,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: AppColors.calmBlue,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _openLiveChat(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) => const _LiveChatSheet(),
    );
  }

  Future<void> _openUrl(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri);
    }
  }
}

class _FAQSection extends StatefulWidget {
  const _FAQSection();

  @override
  State<_FAQSection> createState() => _FAQSectionState();
}

class _FAQSectionState extends State<_FAQSection> {
  final List<_FAQItem> _faqs = [
    _FAQItem(
      question: 'How do I change my accessibility settings?',
      answer: 'Go to Settings > Accessibility to customize motion, fonts, colors, and more to make NeuroComet comfortable for you.',
    ),
    _FAQItem(
      question: 'How do I report inappropriate content?',
      answer: 'Tap the three dots (...) on any post or profile, then select "Report". Your report is anonymous and will be reviewed by our moderation team.',
    ),
    _FAQItem(
      question: 'How do I block someone?',
      answer: 'Visit their profile and tap the three dots menu, then select "Block". Blocked users can\'t see your profile or contact you.',
    ),
    _FAQItem(
      question: 'What are sensory games?',
      answer: 'Sensory games are calming activities designed to help you relax and regulate. They include bubble popping, breathing exercises, and more.',
    ),
    _FAQItem(
      question: 'How do I set up parental controls?',
      answer: 'Go to Settings > Parental Controls. You\'ll create a PIN to protect these settings and can then configure content filters, screen time limits, and more.',
    ),
    _FAQItem(
      question: 'Is my data private?',
      answer: 'Yes! We take privacy seriously. You control who can see your profile and contact you. Read our Privacy Policy for full details.',
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ExpansionPanelList(
        elevation: 0,
        expandedHeaderPadding: EdgeInsets.zero,
        expansionCallback: (index, isExpanded) {
          setState(() {
            _faqs[index].isExpanded = !_faqs[index].isExpanded;
          });
        },
        children: _faqs.map((faq) {
          return ExpansionPanel(
            headerBuilder: (context, isExpanded) {
              return ListTile(
                title: Text(
                  faq.question,
                  style: const TextStyle(fontWeight: FontWeight.w500),
                ),
              );
            },
            body: Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
              child: Text(
                faq.answer,
                style: Theme.of(context).textTheme.bodyMedium,
              ),
            ),
            isExpanded: faq.isExpanded,
          );
        }).toList(),
      ),
    );
  }
}

class _FAQItem {
  final String question;
  final String answer;
  bool isExpanded = false;

  _FAQItem({
    required this.question,
    required this.answer,
  });
}

/// Bug report screen
class BugReportScreen extends StatefulWidget {
  const BugReportScreen({super.key});

  @override
  State<BugReportScreen> createState() => _BugReportScreenState();
}

class _BugReportScreenState extends State<BugReportScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  String _category = 'bug';
  bool _includeDeviceInfo = true;
  bool _isSubmitting = false;

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _submitReport() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSubmitting = true);

    try {
      await Future.delayed(const Duration(seconds: 1));

      if (mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Report submitted successfully!'),
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
    return Scaffold(
      appBar: AppBar(
        title: const Text('Report a Bug'),
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            DropdownButtonFormField<String>(
              initialValue: _category,
              decoration: const InputDecoration(
                labelText: 'Category',
              ),
              items: const [
                DropdownMenuItem(value: 'bug', child: Text('Bug')),
                DropdownMenuItem(value: 'crash', child: Text('Crash')),
                DropdownMenuItem(value: 'performance', child: Text('Performance')),
                DropdownMenuItem(value: 'ui', child: Text('UI Issue')),
                DropdownMenuItem(value: 'other', child: Text('Other')),
              ],
              onChanged: (value) {
                setState(() => _category = value!);
              },
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _titleController,
              decoration: const InputDecoration(
                labelText: 'Title',
                hintText: 'Brief description of the issue',
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
                hintText: 'Describe what happened and how to reproduce it',
                alignLabelWithHint: true,
              ),
              maxLines: 6,
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Please enter a description';
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
          ],
        ),
      ),
    );
  }
}

class _LiveChatSheet extends StatefulWidget {
  const _LiveChatSheet();

  @override
  State<_LiveChatSheet> createState() => _LiveChatSheetState();
}

class _LiveChatSheetState extends State<_LiveChatSheet> {
  final _messageController = TextEditingController();
  final _scrollController = ScrollController();
  final List<_ChatMessage> _messages = [
    _ChatMessage(
      text: 'Hi there! 👋 Welcome to NeuroComet Support. How can I help you today?',
      isUser: false,
      timestamp: DateTime.now().subtract(const Duration(seconds: 5)),
    ),
  ];
  bool _isTyping = false;

  final List<String> _quickReplies = [
    'I need help with my account',
    'How do I change my theme?',
    'Report a bug',
    'I have a feature request',
  ];

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _sendMessage(String text) {
    if (text.trim().isEmpty) return;

    setState(() {
      _messages.add(_ChatMessage(text: text.trim(), isUser: true, timestamp: DateTime.now()));
      _messageController.clear();
      _isTyping = true;
    });

    _scrollToBottom();

    // Simulate bot response
    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) {
        setState(() {
          _isTyping = false;
          _messages.add(_ChatMessage(
            text: _getBotResponse(text.trim()),
            isUser: false,
            timestamp: DateTime.now(),
          ));
        });
        _scrollToBottom();
      }
    });
  }

  String _getBotResponse(String message) {
    final lower = message.toLowerCase();
    if (lower.contains('account')) {
      return 'For account-related issues, you can go to Settings → Profile to manage your account. If you need to reset your password, use the "Forgot Password" option on the login screen. Is there anything specific I can help with?';
    } else if (lower.contains('theme') || lower.contains('color')) {
      return 'You can change your theme by going to Settings → NeuroState. We offer many themes designed for different neurodivergent needs, including ADHD-optimized, autism-friendly, and various accessibility themes. 🎨';
    } else if (lower.contains('bug') || lower.contains('crash') || lower.contains('error')) {
      return 'Sorry to hear you\'re experiencing an issue! You can submit a detailed bug report at Settings → Feedback → Bug Report. Please include steps to reproduce the issue and your device info for a faster resolution. 🐛';
    } else if (lower.contains('feature') || lower.contains('request') || lower.contains('suggest')) {
      return 'We love hearing your ideas! You can submit feature requests at Settings → Feedback → Feature Request. Our team reviews every suggestion. Thank you for helping make NeuroComet better! 💡';
    } else if (lower.contains('game') || lower.contains('play')) {
      return 'Our sensory games can be found in the Games Hub! You unlock more games by earning achievements. Each game is designed to help with regulation, focus, or relaxation. 🎮';
    } else {
      return 'Thank you for reaching out! Our support team is available 9am-6pm EST on weekdays. For faster help, you can also email us at support@getneurocomet.com. Is there anything else I can assist with? 😊';
    }
  }

  void _scrollToBottom() {
    Future.delayed(const Duration(milliseconds: 100), () {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return SizedBox(
      height: MediaQuery.of(context).size.height * 0.75,
      child: Column(
        children: [
          // Header
          Padding(
            padding: const EdgeInsets.only(top: 12, bottom: 8),
            child: Container(width: 40, height: 4,
              decoration: BoxDecoration(color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.4), borderRadius: BorderRadius.circular(2)),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Row(
              children: [
                CircleAvatar(
                  radius: 18,
                  backgroundColor: theme.colorScheme.primaryContainer,
                  child: const Text('🤖', style: TextStyle(fontSize: 18)),
                ),
                const SizedBox(width: 12),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('NeuroComet Support', style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                    Row(
                      children: [
                        Container(width: 8, height: 8,
                          decoration: const BoxDecoration(color: AppColors.success, shape: BoxShape.circle)),
                        const SizedBox(width: 4),
                        Text('Online', style: theme.textTheme.labelSmall?.copyWith(color: AppColors.success)),
                      ],
                    ),
                  ],
                ),
                const Spacer(),
                IconButton(icon: const Icon(Icons.close), onPressed: () => Navigator.pop(context)),
              ],
            ),
          ),
          const Divider(height: 1),

          // Messages
          Expanded(
            child: ListView.builder(
              controller: _scrollController,
              padding: const EdgeInsets.all(16),
              itemCount: _messages.length + (_isTyping ? 1 : 0),
              itemBuilder: (context, index) {
                if (index == _messages.length && _isTyping) {
                  return Align(
                    alignment: Alignment.centerLeft,
                    child: Container(
                      margin: const EdgeInsets.only(top: 8),
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.surfaceContainer,
                        borderRadius: BorderRadius.circular(16),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: const [
                          SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2)),
                          SizedBox(width: 8),
                          Text('Typing...', style: TextStyle(fontSize: 12)),
                        ],
                      ),
                    ),
                  );
                }

                final msg = _messages[index];
                return Align(
                  alignment: msg.isUser ? Alignment.centerRight : Alignment.centerLeft,
                  child: Container(
                    margin: const EdgeInsets.only(top: 8),
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                    constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.75),
                    decoration: BoxDecoration(
                      color: msg.isUser ? theme.colorScheme.primary : theme.colorScheme.surfaceContainer,
                      borderRadius: BorderRadius.circular(16),
                    ),
                    child: Text(
                      msg.text,
                      style: TextStyle(
                        color: msg.isUser ? theme.colorScheme.onPrimary : theme.colorScheme.onSurface,
                        fontSize: 14,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),

          // Quick replies
          if (_messages.length <= 2)
            SizedBox(
              height: 44,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                itemCount: _quickReplies.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (context, index) => ActionChip(
                  label: Text(_quickReplies[index], style: const TextStyle(fontSize: 12)),
                  onPressed: () => _sendMessage(_quickReplies[index]),
                ),
              ),
            ),
          if (_messages.length <= 2) const SizedBox(height: 8),

          // Input
          Padding(
            padding: EdgeInsets.only(
              left: 12, right: 12, bottom: MediaQuery.of(context).viewInsets.bottom + 12, top: 4,
            ),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _messageController,
                    decoration: InputDecoration(
                      hintText: 'Type your message...',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(24)),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                      isDense: true,
                    ),
                    onSubmitted: _sendMessage,
                  ),
                ),
                const SizedBox(width: 8),
                FloatingActionButton.small(
                  onPressed: () => _sendMessage(_messageController.text),
                  child: const Icon(Icons.send),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ChatMessage {
  final String text;
  final bool isUser;
  final DateTime timestamp;

  const _ChatMessage({required this.text, required this.isUser, required this.timestamp});
}
