import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/theme/app_colors.dart';

/// Social Settings screen.
/// Mirrors the Kotlin SocialSettings.kt
///
/// All settings are designed with sensory sensitivities and cognitive needs in mind.
class SocialSettingsScreen extends StatefulWidget {
  final int initialTabIndex;
  const SocialSettingsScreen({super.key, this.initialTabIndex = 0});

  @override
  State<SocialSettingsScreen> createState() => _SocialSettingsScreenState();
}

class _SocialSettingsScreenState extends State<SocialSettingsScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  // Privacy
  bool _isAccountPrivate = false;
  bool _showOnlineStatus = true;
  bool _showReadReceipts = true;
  bool _hideFromSearch = false;
  bool _twoFactorEnabled = false;

  // Notifications
  bool _pushEnabled = true;
  bool _likesEnabled = true;
  bool _commentsEnabled = true;
  bool _followsEnabled = true;
  bool _mentionsEnabled = true;
  bool _dmEnabled = true;
  bool _storyRepliesEnabled = true;
  bool _quietHoursEnabled = false;
  bool _groupNotifications = true;
  bool _soundEnabled = true;
  bool _vibrationEnabled = true;

  // Content
  String _autoplayVideos = 'wifi_only';
  bool _dataSaverMode = false;
  bool _showSensitiveContent = false;
  bool _hideViewCounts = false;
  bool _hideLikeCounts = false;

  // Accessibility
  bool _reduceMotion = false;
  bool _largerText = false;
  bool _simplifiedUI = false;
  bool _focusMode = false;
  bool _hapticFeedback = true;
  bool _dyslexiaFont = false;

  // Wellbeing
  bool _breakRemindersEnabled = false;
  bool _showUsageStats = true;
  bool _bedtimeModeEnabled = false;
  bool _positivityBoostEnabled = true;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 5, vsync: this, initialIndex: widget.initialTabIndex);
    _loadSettings();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _isAccountPrivate = prefs.getBool('account_private') ?? false;
      _showOnlineStatus = prefs.getBool('show_online') ?? true;
      _showReadReceipts = prefs.getBool('read_receipts') ?? true;
      _hideFromSearch = prefs.getBool('hide_search') ?? false;
      _twoFactorEnabled = prefs.getBool('two_factor') ?? false;
      _pushEnabled = prefs.getBool('push_enabled') ?? true;
      _likesEnabled = prefs.getBool('likes_notif') ?? true;
      _commentsEnabled = prefs.getBool('comments_notif') ?? true;
      _followsEnabled = prefs.getBool('follows_notif') ?? true;
      _mentionsEnabled = prefs.getBool('mentions_notif') ?? true;
      _dmEnabled = prefs.getBool('dm_notif') ?? true;
      _storyRepliesEnabled = prefs.getBool('story_replies_notif') ?? true;
      _quietHoursEnabled = prefs.getBool('quiet_hours') ?? false;
      _groupNotifications = prefs.getBool('group_notifs') ?? true;
      _soundEnabled = prefs.getBool('sound') ?? true;
      _vibrationEnabled = prefs.getBool('vibration') ?? true;
      _autoplayVideos = prefs.getString('autoplay') ?? 'wifi_only';
      _dataSaverMode = prefs.getBool('data_saver') ?? false;
      _showSensitiveContent = prefs.getBool('sensitive_content') ?? false;
      _hideViewCounts = prefs.getBool('hide_views') ?? false;
      _hideLikeCounts = prefs.getBool('hide_likes') ?? false;
      _reduceMotion = prefs.getBool('reduce_motion') ?? false;
      _largerText = prefs.getBool('larger_text') ?? false;
      _simplifiedUI = prefs.getBool('simplified_ui') ?? false;
      _focusMode = prefs.getBool('focus_mode') ?? false;
      _hapticFeedback = prefs.getBool('haptic') ?? true;
      _dyslexiaFont = prefs.getBool('dyslexia_font') ?? false;
      _breakRemindersEnabled = prefs.getBool('break_reminders') ?? false;
      _showUsageStats = prefs.getBool('show_usage_stats') ?? true;
      _bedtimeModeEnabled = prefs.getBool('bedtime_mode') ?? false;
      _positivityBoostEnabled = prefs.getBool('positivity_boost') ?? true;
    });
  }

  Future<void> _save(String key, dynamic value) async {
    final prefs = await SharedPreferences.getInstance();
    if (value is bool) {
      await prefs.setBool(key, value);
    } else if (value is String) {
      await prefs.setString(key, value);
    } else if (value is int) {
      await prefs.setInt(key, value);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Social Settings'),
        bottom: TabBar(
          controller: _tabController,
          isScrollable: true,
          tabs: const [
            Tab(text: 'Privacy'),
            Tab(text: 'Notifications'),
            Tab(text: 'Content'),
            Tab(text: 'Accessibility'),
            Tab(text: 'Wellbeing'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildPrivacyTab(theme),
          _buildNotificationsTab(theme),
          _buildContentTab(theme),
          _buildAccessibilityTab(theme),
          _buildWellbeingTab(theme),
        ],
      ),
    );
  }

  // ═══════════════════════════════════════════════════════════════
  // PRIVACY TAB
  // ═══════════════════════════════════════════════════════════════

  Widget _buildPrivacyTab(ThemeData theme) {
    return ListView(
      children: [
        _sectionHeader(theme, 'Account Privacy'),
        SwitchListTile(
          title: const Text('Private Account'),
          subtitle: const Text('Only approved followers can see your posts'),
          secondary: const Icon(Icons.lock_outline),
          value: _isAccountPrivate,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _isAccountPrivate = v);
            _save('account_private', v);
          },
        ),
        SwitchListTile(
          title: const Text('Show Online Status'),
          subtitle: const Text('Let others see when you\'re active'),
          secondary: const Icon(Icons.circle, color: AppColors.success, size: 20),
          value: _showOnlineStatus,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _showOnlineStatus = v);
            _save('show_online', v);
          },
        ),
        SwitchListTile(
          title: const Text('Read Receipts'),
          subtitle: const Text('Let others know when you\'ve read messages'),
          secondary: const Icon(Icons.done_all),
          value: _showReadReceipts,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _showReadReceipts = v);
            _save('read_receipts', v);
          },
        ),
        SwitchListTile(
          title: const Text('Hide from Search'),
          subtitle: const Text('Your profile won\'t appear in search results'),
          secondary: const Icon(Icons.search_off),
          value: _hideFromSearch,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _hideFromSearch = v);
            _save('hide_search', v);
          },
        ),
        _sectionHeader(theme, 'Security'),
        SwitchListTile(
          title: const Text('Two-Factor Authentication'),
          subtitle: const Text('Add an extra layer of security'),
          secondary: const Icon(Icons.security),
          value: _twoFactorEnabled,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _twoFactorEnabled = v);
            _save('two_factor', v);
          },
        ),
        const SizedBox(height: 32),
      ],
    );
  }

  // ═══════════════════════════════════════════════════════════════
  // NOTIFICATIONS TAB
  // ═══════════════════════════════════════════════════════════════

  Widget _buildNotificationsTab(ThemeData theme) {
    return ListView(
      children: [
        _sectionHeader(theme, 'Push Notifications'),
        SwitchListTile(
          title: const Text('Push Notifications'),
          subtitle: const Text('Receive notifications on your device'),
          secondary: const Icon(Icons.notifications_outlined),
          value: _pushEnabled,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _pushEnabled = v);
            _save('push_enabled', v);
          },
        ),
        _sectionHeader(theme, 'Notification Types'),
        SwitchListTile(
          title: const Text('Likes'),
          value: _likesEnabled,
          onChanged: (v) {
            setState(() => _likesEnabled = v);
            _save('likes_notif', v);
          },
        ),
        SwitchListTile(
          title: const Text('Comments'),
          value: _commentsEnabled,
          onChanged: (v) {
            setState(() => _commentsEnabled = v);
            _save('comments_notif', v);
          },
        ),
        SwitchListTile(
          title: const Text('Follows'),
          value: _followsEnabled,
          onChanged: (v) {
            setState(() => _followsEnabled = v);
            _save('follows_notif', v);
          },
        ),
        SwitchListTile(
          title: const Text('Mentions'),
          value: _mentionsEnabled,
          onChanged: (v) {
            setState(() => _mentionsEnabled = v);
            _save('mentions_notif', v);
          },
        ),
        SwitchListTile(
          title: const Text('Direct Messages'),
          value: _dmEnabled,
          onChanged: (v) {
            setState(() => _dmEnabled = v);
            _save('dm_notif', v);
          },
        ),
        SwitchListTile(
          title: const Text('Story Replies'),
          value: _storyRepliesEnabled,
          onChanged: (v) {
            setState(() => _storyRepliesEnabled = v);
            _save('story_replies_notif', v);
          },
        ),
        _sectionHeader(theme, 'Quiet Hours'),
        SwitchListTile(
          title: const Text('Quiet Hours'),
          subtitle: const Text('Silence notifications during set hours'),
          secondary: const Icon(Icons.bedtime_outlined),
          value: _quietHoursEnabled,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _quietHoursEnabled = v);
            _save('quiet_hours', v);
          },
        ),
        _sectionHeader(theme, 'Preferences'),
        SwitchListTile(
          title: const Text('Group Notifications'),
          subtitle: const Text('Bundle similar notifications together'),
          value: _groupNotifications,
          onChanged: (v) {
            setState(() => _groupNotifications = v);
            _save('group_notifs', v);
          },
        ),
        SwitchListTile(
          title: const Text('Sound'),
          value: _soundEnabled,
          onChanged: (v) {
            setState(() => _soundEnabled = v);
            _save('sound', v);
          },
        ),
        SwitchListTile(
          title: const Text('Vibration'),
          value: _vibrationEnabled,
          onChanged: (v) {
            setState(() => _vibrationEnabled = v);
            _save('vibration', v);
          },
        ),
        const SizedBox(height: 32),
      ],
    );
  }

  // ═══════════════════════════════════════════════════════════════
  // CONTENT TAB
  // ═══════════════════════════════════════════════════════════════

  Widget _buildContentTab(ThemeData theme) {
    return ListView(
      children: [
        _sectionHeader(theme, 'Media'),
        ListTile(
          title: const Text('Autoplay Videos'),
          subtitle: Text(_autoplayLabel(_autoplayVideos)),
          leading: const Icon(Icons.play_circle_outline),
          trailing: const Icon(Icons.chevron_right),
          onTap: () => _showAutoplayDialog(),
        ),
        SwitchListTile(
          title: const Text('Data Saver Mode'),
          subtitle: const Text('Reduce data usage by loading lower quality media'),
          secondary: const Icon(Icons.data_saver_on),
          value: _dataSaverMode,
          onChanged: (v) {
            setState(() => _dataSaverMode = v);
            _save('data_saver', v);
          },
        ),
        _sectionHeader(theme, 'Content Display'),
        SwitchListTile(
          title: const Text('Show Sensitive Content'),
          subtitle: const Text('Show content marked as sensitive'),
          value: _showSensitiveContent,
          onChanged: (v) {
            setState(() => _showSensitiveContent = v);
            _save('sensitive_content', v);
          },
        ),
        SwitchListTile(
          title: const Text('Hide View Counts'),
          subtitle: const Text('Hide view counts on all posts'),
          value: _hideViewCounts,
          onChanged: (v) {
            setState(() => _hideViewCounts = v);
            _save('hide_views', v);
          },
        ),
        SwitchListTile(
          title: const Text('Hide Like Counts'),
          subtitle: const Text('Hide like counts on all posts'),
          value: _hideLikeCounts,
          onChanged: (v) {
            setState(() => _hideLikeCounts = v);
            _save('hide_likes', v);
          },
        ),
        const SizedBox(height: 32),
      ],
    );
  }

  // ═══════════════════════════════════════════════════════════════
  // ACCESSIBILITY TAB
  // ═══════════════════════════════════════════════════════════════

  Widget _buildAccessibilityTab(ThemeData theme) {
    return ListView(
      children: [
        _sectionHeader(theme, 'Visual'),
        SwitchListTile(
          title: const Text('Reduce Motion'),
          subtitle: const Text('Minimize animations throughout the app'),
          secondary: const Icon(Icons.animation),
          value: _reduceMotion,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _reduceMotion = v);
            _save('reduce_motion', v);
          },
        ),
        SwitchListTile(
          title: const Text('Larger Text'),
          subtitle: const Text('Increase text size for better readability'),
          secondary: const Icon(Icons.text_fields),
          value: _largerText,
          onChanged: (v) {
            setState(() => _largerText = v);
            _save('larger_text', v);
          },
        ),
        SwitchListTile(
          title: const Text('Dyslexia-Friendly Font'),
          subtitle: const Text('Use a font designed for easier reading'),
          secondary: const Icon(Icons.font_download),
          value: _dyslexiaFont,
          onChanged: (v) {
            setState(() => _dyslexiaFont = v);
            _save('dyslexia_font', v);
          },
        ),
        _sectionHeader(theme, 'Interface'),
        SwitchListTile(
          title: const Text('Simplified UI'),
          subtitle: const Text('Reduce visual complexity'),
          secondary: const Icon(Icons.dashboard_customize),
          value: _simplifiedUI,
          onChanged: (v) {
            setState(() => _simplifiedUI = v);
            _save('simplified_ui', v);
          },
        ),
        SwitchListTile(
          title: const Text('Focus Mode'),
          subtitle: const Text('Hide distracting elements'),
          secondary: const Icon(Icons.center_focus_strong),
          value: _focusMode,
          onChanged: (v) {
            setState(() => _focusMode = v);
            _save('focus_mode', v);
          },
        ),
        SwitchListTile(
          title: const Text('Haptic Feedback'),
          subtitle: const Text('Gentle vibrations for interactions'),
          secondary: const Icon(Icons.vibration),
          value: _hapticFeedback,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _hapticFeedback = v);
            _save('haptic', v);
          },
        ),
        const SizedBox(height: 32),
      ],
    );
  }

  // ═══════════════════════════════════════════════════════════════
  // WELLBEING TAB
  // ═══════════════════════════════════════════════════════════════

  Widget _buildWellbeingTab(ThemeData theme) {
    return ListView(
      children: [
        _sectionHeader(theme, 'Reminders'),
        SwitchListTile(
          title: const Text('Break Reminders'),
          subtitle: const Text('Get reminded to take breaks'),
          secondary: const Icon(Icons.coffee),
          value: _breakRemindersEnabled,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _breakRemindersEnabled = v);
            _save('break_reminders', v);
          },
        ),
        _sectionHeader(theme, 'Usage'),
        SwitchListTile(
          title: const Text('Show Usage Stats'),
          subtitle: const Text('See how much time you spend on the app'),
          secondary: const Icon(Icons.bar_chart),
          value: _showUsageStats,
          onChanged: (v) {
            setState(() => _showUsageStats = v);
            _save('show_usage_stats', v);
          },
        ),
        _sectionHeader(theme, 'Bedtime'),
        SwitchListTile(
          title: const Text('Bedtime Mode'),
          subtitle: const Text('Limit app access during sleep hours'),
          secondary: const Icon(Icons.nights_stay),
          value: _bedtimeModeEnabled,
          onChanged: (v) {
            HapticFeedback.selectionClick();
            setState(() => _bedtimeModeEnabled = v);
            _save('bedtime_mode', v);
          },
        ),
        _sectionHeader(theme, 'Positivity'),
        SwitchListTile(
          title: const Text('Positivity Boost'),
          subtitle: const Text('Show encouraging messages throughout the app'),
          secondary: const Icon(Icons.favorite, color: Color(0xFFE91E63)),
          value: _positivityBoostEnabled,
          onChanged: (v) {
            setState(() => _positivityBoostEnabled = v);
            _save('positivity_boost', v);
          },
        ),
        const SizedBox(height: 32),
      ],
    );
  }

  // ═══════════════════════════════════════════════════════════════
  // HELPERS
  // ═══════════════════════════════════════════════════════════════

  Widget _sectionHeader(ThemeData theme, String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 4),
      child: Text(
        title,
        style: theme.textTheme.titleSmall?.copyWith(
          fontWeight: FontWeight.bold,
          color: AppColors.primaryPurple,
        ),
      ),
    );
  }

  String _autoplayLabel(String value) {
    switch (value) {
      case 'always':
        return 'Always';
      case 'wifi_only':
        return 'Wi-Fi Only';
      case 'never':
        return 'Never';
      default:
        return 'Wi-Fi Only';
    }
  }

  void _showAutoplayDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Autoplay Videos'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            RadioListTile<String>(
              title: const Text('Always'),
              value: 'always',
              groupValue: _autoplayVideos,
              onChanged: (v) {
                setState(() => _autoplayVideos = v!);
                _save('autoplay', v!);
                Navigator.pop(context);
              },
            ),
            RadioListTile<String>(
              title: const Text('Wi-Fi Only'),
              value: 'wifi_only',
              groupValue: _autoplayVideos,
              onChanged: (v) {
                setState(() => _autoplayVideos = v!);
                _save('autoplay', v!);
                Navigator.pop(context);
              },
            ),
            RadioListTile<String>(
              title: const Text('Never'),
              value: 'never',
              groupValue: _autoplayVideos,
              onChanged: (v) {
                setState(() => _autoplayVideos = v!);
                _save('autoplay', v!);
                Navigator.pop(context);
              },
            ),
          ],
        ),
      ),
    );
  }
}

