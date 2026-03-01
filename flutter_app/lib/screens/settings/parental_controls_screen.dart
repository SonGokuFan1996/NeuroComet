import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../l10n/app_localizations.dart';

/// Parental Controls Screen for managing child-safe features
class ParentalControlsScreen extends ConsumerStatefulWidget {
  const ParentalControlsScreen({super.key});

  @override
  ConsumerState<ParentalControlsScreen> createState() => _ParentalControlsScreenState();
}

class _ParentalControlsScreenState extends ConsumerState<ParentalControlsScreen> {
  bool _parentalControlsEnabled = false;
  bool _isUnlocked = false;
  final _pinController = TextEditingController();
  String? _savedPin;

  // Content filters
  bool _filterMatureContent = true;
  bool _filterSensitiveTopics = true;
  bool _safeSearchEnabled = true;

  // Communication
  bool _restrictMessages = false;
  bool _restrictComments = false;
  bool _approveFollowers = false;

  // Screen time
  bool _screenTimeEnabled = false;
  int _dailyLimitMinutes = 60;
  TimeOfDay _bedtime = const TimeOfDay(hour: 21, minute: 0);
  TimeOfDay _wakeTime = const TimeOfDay(hour: 7, minute: 0);

  // Usage reports
  bool _weeklyReports = false;

  @override
  void dispose() {
    _pinController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.parentalControls),
        actions: [
          if (_parentalControlsEnabled && _isUnlocked)
            IconButton(
              icon: const Icon(Icons.lock),
              onPressed: () => setState(() => _isUnlocked = false),
              tooltip: 'Lock Settings',
            ),
        ],
      ),
      body: _parentalControlsEnabled && !_isUnlocked
          ? _buildLockedScreen(context)
          : _buildSettingsScreen(context),
    );
  }

  Widget _buildLockedScreen(BuildContext context) {
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: theme.colorScheme.primaryContainer,
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.lock,
                size: 48,
                color: theme.colorScheme.primary,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              'Parental Controls Locked',
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Enter your PIN to access settings',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 32),
            SizedBox(
              width: 200,
              child: TextField(
                controller: _pinController,
                keyboardType: TextInputType.number,
                obscureText: true,
                maxLength: 4,
                textAlign: TextAlign.center,
                decoration: const InputDecoration(
                  hintText: 'Enter PIN',
                  counterText: '',
                ),
                onSubmitted: (_) => _verifyPin(),
              ),
            ),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: _verifyPin,
              child: const Text('Unlock'),
            ),
            const SizedBox(height: 16),
            TextButton(
              onPressed: () => _showForgotPinDialog(context),
              child: const Text('Forgot PIN?'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSettingsScreen(BuildContext context) {

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        // Enable/Disable toggle
        _SettingsCard(
          child: SwitchListTile(
            title: const Text('Enable Parental Controls'),
            subtitle: const Text('Protect this account with a PIN'),
            secondary: const Icon(Icons.family_restroom),
            value: _parentalControlsEnabled,
            onChanged: (value) {
              if (value) {
                _showSetPinDialog(context);
              } else {
                _showDisableDialog(context);
              }
            },
          ),
        ),

        if (_parentalControlsEnabled) ...[
          const SizedBox(height: 24),

          // Content Filtering
          _buildSectionHeader(context, 'Content Filtering'),
          _SettingsCard(
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('Filter Mature Content'),
                  subtitle: const Text('Hide content marked as mature'),
                  value: _filterMatureContent,
                  onChanged: (value) {
                    setState(() => _filterMatureContent = value);
                  },
                ),
                const Divider(height: 1),
                SwitchListTile(
                  title: const Text('Filter Sensitive Topics'),
                  subtitle: const Text('Hide discussions about sensitive topics'),
                  value: _filterSensitiveTopics,
                  onChanged: (value) {
                    setState(() => _filterSensitiveTopics = value);
                  },
                ),
                const Divider(height: 1),
                SwitchListTile(
                  title: const Text('Safe Search'),
                  subtitle: const Text('Enable safe search in Explore'),
                  value: _safeSearchEnabled,
                  onChanged: (value) {
                    setState(() => _safeSearchEnabled = value);
                  },
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Communication Controls
          _buildSectionHeader(context, 'Communication'),
          _SettingsCard(
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('Restrict Messages'),
                  subtitle: const Text('Only allow messages from followers'),
                  value: _restrictMessages,
                  onChanged: (value) {
                    setState(() => _restrictMessages = value);
                  },
                ),
                const Divider(height: 1),
                SwitchListTile(
                  title: const Text('Restrict Comments'),
                  subtitle: const Text('Disable commenting on posts'),
                  value: _restrictComments,
                  onChanged: (value) {
                    setState(() => _restrictComments = value);
                  },
                ),
                const Divider(height: 1),
                SwitchListTile(
                  title: const Text('Approve Followers'),
                  subtitle: const Text('Manually approve new followers'),
                  value: _approveFollowers,
                  onChanged: (value) {
                    setState(() => _approveFollowers = value);
                  },
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Screen Time
          _buildSectionHeader(context, 'Screen Time'),
          _SettingsCard(
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('Enable Screen Time Limits'),
                  subtitle: const Text('Set daily usage limits'),
                  value: _screenTimeEnabled,
                  onChanged: (value) {
                    setState(() => _screenTimeEnabled = value);
                  },
                ),
                if (_screenTimeEnabled) ...[
                  const Divider(height: 1),
                  ListTile(
                    title: const Text('Daily Limit'),
                    subtitle: Text('$_dailyLimitMinutes minutes'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => _showDailyLimitDialog(context),
                  ),
                  const Divider(height: 1),
                  ListTile(
                    title: const Text('Bedtime'),
                    subtitle: Text(_formatTime(_bedtime)),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => _selectBedtime(context),
                  ),
                  const Divider(height: 1),
                  ListTile(
                    title: const Text('Wake Time'),
                    subtitle: Text(_formatTime(_wakeTime)),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => _selectWakeTime(context),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Reports
          _buildSectionHeader(context, 'Activity Reports'),
          _SettingsCard(
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('Weekly Activity Reports'),
                  subtitle: const Text('Receive email reports of app usage'),
                  value: _weeklyReports,
                  onChanged: (value) {
                    setState(() => _weeklyReports = value);
                  },
                ),
                const Divider(height: 1),
                ListTile(
                  title: const Text('View Activity Log'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => _showActivityLog(context),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // PIN Management
          _buildSectionHeader(context, 'Security'),
          _SettingsCard(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.pin),
                  title: const Text('Change PIN'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => _showChangePinDialog(context),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.email),
                  title: const Text('Recovery Email'),
                  subtitle: const Text('j***@email.com'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => _showRecoveryEmailDialog(context),
                ),
              ],
            ),
          ),
          const SizedBox(height: 32),
        ],
      ],
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

  void _verifyPin() {
    if (_pinController.text == _savedPin) {
      setState(() {
        _isUnlocked = true;
        _pinController.clear();
      });
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Incorrect PIN'),
          behavior: SnackBarBehavior.floating,
        ),
      );
      _pinController.clear();
    }
  }

  void _showSetPinDialog(BuildContext context) {
    final pinController = TextEditingController();
    final confirmController = TextEditingController();

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        title: const Text('Set PIN'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Create a 4-digit PIN to protect parental controls.'),
            const SizedBox(height: 16),
            TextField(
              controller: pinController,
              keyboardType: TextInputType.number,
              obscureText: true,
              maxLength: 4,
              decoration: const InputDecoration(
                labelText: 'PIN',
                counterText: '',
              ),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: confirmController,
              keyboardType: TextInputType.number,
              obscureText: true,
              maxLength: 4,
              decoration: const InputDecoration(
                labelText: 'Confirm PIN',
                counterText: '',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              if (pinController.text.length == 4 &&
                  pinController.text == confirmController.text) {
                setState(() {
                  _savedPin = pinController.text;
                  _parentalControlsEnabled = true;
                  _isUnlocked = true;
                });
                Navigator.pop(context);
              } else {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('PINs must match and be 4 digits'),
                    behavior: SnackBarBehavior.floating,
                  ),
                );
              }
            },
            child: const Text('Set PIN'),
          ),
        ],
      ),
    );
  }

  void _showDisableDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Disable Parental Controls?'),
        content: const Text(
          'This will remove all parental control restrictions and delete your PIN.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              setState(() {
                _parentalControlsEnabled = false;
                _savedPin = null;
                _isUnlocked = false;
              });
              Navigator.pop(context);
            },
            child: const Text('Disable'),
          ),
        ],
      ),
    );
  }

  void _showForgotPinDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Forgot PIN?'),
        content: const Text(
          'A PIN reset link will be sent to your registered email address.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Reset link sent to your email'),
                  behavior: SnackBarBehavior.floating,
                ),
              );
            },
            child: const Text('Send Reset Link'),
          ),
        ],
      ),
    );
  }

  void _showDailyLimitDialog(BuildContext context) {
    int tempLimit = _dailyLimitMinutes;

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: const Text('Daily Limit'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                '$tempLimit minutes',
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              Slider(
                value: tempLimit.toDouble(),
                min: 15,
                max: 240,
                divisions: 15,
                label: '$tempLimit min',
                onChanged: (value) {
                  setDialogState(() => tempLimit = value.round());
                },
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () {
                setState(() => _dailyLimitMinutes = tempLimit);
                Navigator.pop(context);
              },
              child: const Text('Save'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _selectBedtime(BuildContext context) async {
    final time = await showTimePicker(
      context: context,
      initialTime: _bedtime,
    );
    if (time != null) {
      setState(() => _bedtime = time);
    }
  }

  Future<void> _selectWakeTime(BuildContext context) async {
    final time = await showTimePicker(
      context: context,
      initialTime: _wakeTime,
    );
    if (time != null) {
      setState(() => _wakeTime = time);
    }
  }

  String _formatTime(TimeOfDay time) {
    final hour = time.hourOfPeriod == 0 ? 12 : time.hourOfPeriod;
    final minute = time.minute.toString().padLeft(2, '0');
    final period = time.period == DayPeriod.am ? 'AM' : 'PM';
    return '$hour:$minute $period';
  }

  void _showActivityLog(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.7,
        builder: (context, controller) => Container(
          decoration: BoxDecoration(
            color: Theme.of(context).scaffoldBackgroundColor,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
          ),
          child: Column(
            children: [
              Container(
                margin: const EdgeInsets.symmetric(vertical: 12),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: Theme.of(context).dividerColor,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  'Activity Log',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
              ),
              Expanded(
                child: ListView.builder(
                  controller: controller,
                  itemCount: 10,
                  itemBuilder: (context, index) => ListTile(
                    leading: CircleAvatar(
                      child: Icon(_getActivityIcon(index)),
                    ),
                    title: Text(_getActivityTitle(index)),
                    subtitle: Text(_getActivityTime(index)),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  IconData _getActivityIcon(int index) {
    final icons = [
      Icons.login,
      Icons.article,
      Icons.favorite,
      Icons.chat,
      Icons.search,
    ];
    return icons[index % icons.length];
  }

  String _getActivityTitle(int index) {
    final titles = [
      'App opened',
      'Viewed feed',
      'Liked a post',
      'Sent a message',
      'Used search',
    ];
    return titles[index % titles.length];
  }

  String _getActivityTime(int index) {
    return '${index + 1} hour${index == 0 ? '' : 's'} ago';
  }

  void _showChangePinDialog(BuildContext context) {
    final currentController = TextEditingController();
    final newController = TextEditingController();
    final confirmController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Change PIN'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: currentController,
              keyboardType: TextInputType.number,
              obscureText: true,
              maxLength: 4,
              decoration: const InputDecoration(
                labelText: 'Current PIN',
                counterText: '',
              ),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: newController,
              keyboardType: TextInputType.number,
              obscureText: true,
              maxLength: 4,
              decoration: const InputDecoration(
                labelText: 'New PIN',
                counterText: '',
              ),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: confirmController,
              keyboardType: TextInputType.number,
              obscureText: true,
              maxLength: 4,
              decoration: const InputDecoration(
                labelText: 'Confirm New PIN',
                counterText: '',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              if (currentController.text == _savedPin &&
                  newController.text.length == 4 &&
                  newController.text == confirmController.text) {
                setState(() => _savedPin = newController.text);
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('PIN changed successfully'),
                    behavior: SnackBarBehavior.floating,
                  ),
                );
              } else {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Invalid PIN or PINs don\'t match'),
                    behavior: SnackBarBehavior.floating,
                  ),
                );
              }
            },
            child: const Text('Change'),
          ),
        ],
      ),
    );
  }

  void _showRecoveryEmailDialog(BuildContext context) {
    final emailController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Recovery Email'),
        content: TextField(
          controller: emailController,
          keyboardType: TextInputType.emailAddress,
          decoration: const InputDecoration(
            labelText: 'Email Address',
            hintText: 'Enter recovery email',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Recovery email updated'),
                  behavior: SnackBarBehavior.floating,
                ),
              );
            },
            child: const Text('Save'),
          ),
        ],
      ),
    );
  }
}

class _SettingsCard extends StatelessWidget {
  final Widget child;

  const _SettingsCard({required this.child});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(
          color: Theme.of(context).dividerColor,
        ),
      ),
      child: child,
    );
  }
}

