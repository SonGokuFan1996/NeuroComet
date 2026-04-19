import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../core/theme/app_colors.dart';
import '../../services/subscription_service.dart';
import '../../widgets/common/transaction_status_card.dart';
import '../../l10n/app_localizations.dart';

/// Subscription screen for premium features
class SubscriptionScreen extends ConsumerStatefulWidget {
  const SubscriptionScreen({super.key});

  @override
  ConsumerState<SubscriptionScreen> createState() => _SubscriptionScreenState();
}

class _SubscriptionScreenState extends ConsumerState<SubscriptionScreen> {
  int _selectedPlanIndex = 1; // Default to annual
  bool _isLoading = false;

  // ── Transaction status card state ──
  TransactionResult? _transactionResult;
  bool _purchaseInFlight = false;
  Timer? _timeoutTimer;

  final _subscriptionService = SubscriptionService.instance;

  final List<Map<String, dynamic>> _plans = [
    {
      'id': 'monthly',
      'name': 'Monthly',
      'price': 4.99,
      'period': 'month',
      'savings': null,
    },
    {
      'id': 'annual',
      'name': 'Annual',
      'price': 39.99,
      'period': 'year',
      'savings': '33% off',
    },
    {
      'id': 'lifetime',
      'name': 'Lifetime',
      'price': 99.99,
      'period': 'once',
      'savings': 'Best value',
    },
  ];

  final List<Map<String, dynamic>> _features = [
    {
      'icon': Icons.block,
      'title': 'Ad-Free Experience',
      'description': 'Enjoy NeuroComet without any interruptions',
    },
    {
      'icon': Icons.palette,
      'title': 'Custom Themes',
      'description': 'Create your own color schemes and themes',
    },
    {
      'icon': Icons.games,
      'title': 'All Sensory Games',
      'description': 'Unlock all calming games and activities',
    },
    {
      'icon': Icons.verified,
      'title': 'Verified Badge',
      'description': 'Show your support with a special badge',
    },
    {
      'icon': Icons.analytics,
      'title': 'Mood Analytics',
      'description': 'Track your emotional journey over time',
    },
    {
      'icon': Icons.priority_high,
      'title': 'Priority Support',
      'description': 'Get faster responses from our support team',
    },
    {
      'icon': Icons.cloud_upload,
      'title': 'Cloud Backup',
      'description': 'Backup your data and preferences',
    },
    {
      'icon': Icons.people,
      'title': 'Exclusive Community',
      'description': 'Access premium-only discussion groups',
    },
  ];

  @override
  void initState() {
    super.initState();
    // Fetch offerings when screen opens
    _subscriptionService.fetchOfferings();
    _subscriptionService.checkPremiumStatus();
    // Listen for external state changes (success / error from service)
    _subscriptionService.stateNotifier.addListener(_onSubscriptionStateChanged);
  }

  @override
  void dispose() {
    _timeoutTimer?.cancel();
    _subscriptionService.stateNotifier.removeListener(_onSubscriptionStateChanged);
    super.dispose();
  }

  void _onSubscriptionStateChanged() {
    final state = _subscriptionService.state;
    if (!mounted) return;

    // Handle purchase success
    if (state.purchaseSuccess && _purchaseInFlight) {
      _timeoutTimer?.cancel();
      setState(() {
        _purchaseInFlight = false;
        _isLoading = false;
        _transactionResult = TransactionResult.success;
      });
      _subscriptionService.clearPurchaseSuccess();
      return;
    }

    // Handle error → declined card
    if (state.error != null && _purchaseInFlight) {
      _timeoutTimer?.cancel();
      setState(() {
        _purchaseInFlight = false;
        _isLoading = false;
        _transactionResult = TransactionResult.declined;
      });
      return;
    }

    // Sync loading flag
    if (state.isLoading != _isLoading) {
      setState(() => _isLoading = state.isLoading);
    }
  }

  void _startTimeoutTimer() {
    _timeoutTimer?.cancel();
    _timeoutTimer = Timer(const Duration(seconds: 30), () {
      if (_purchaseInFlight && mounted) {
        setState(() {
          _purchaseInFlight = false;
          _isLoading = false;
          _transactionResult = TransactionResult.timedOut;
        });
      }
    });
  }

  Future<void> _subscribe() async {
    if (_purchaseInFlight) return; // Prevent double-clicks

    setState(() {
      _isLoading = true;
      _transactionResult = null; // reset previous result
      _purchaseInFlight = true;
    });
    _startTimeoutTimer();

    final planId = _plans[_selectedPlanIndex]['id'] as String;

    try {
      if (planId == 'monthly') {
        await _subscriptionService.purchaseMonthly(
          onSuccess: () {
            if (mounted) setState(() => _purchaseInFlight = false);
          },
          onError: (_) {
            if (mounted) setState(() => _purchaseInFlight = false);
          },
        );
      } else if (planId == 'lifetime') {
        await _subscriptionService.purchaseLifetime(
          onSuccess: () {
            if (mounted) setState(() => _purchaseInFlight = false);
          },
          onError: (_) {
            if (mounted) setState(() => _purchaseInFlight = false);
          },
        );
      } else {
        // Annual plan
        await _subscriptionService.purchaseMonthly(
          onSuccess: () {
            if (mounted) setState(() => _purchaseInFlight = false);
          },
          onError: (_) {
            if (mounted) setState(() => _purchaseInFlight = false);
          },
        );
      }
    } catch (_) {
      if (mounted) {
        setState(() {
          _purchaseInFlight = false;
          _isLoading = false;
        });
      }
    }
  }

  void _retryPurchase() {
    setState(() {
      _transactionResult = null;
    });
    _subscribe();
  }

  /// Show a test transaction status card for debugging.
  void _showTestTransactionResult(TransactionResult result) {
    setState(() {
      _transactionResult = result;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = context.l10n;

    return Scaffold(
      body: CustomScrollView(
        slivers: [
          // App bar with gradient
          SliverAppBar(
            expandedHeight: 200,
            pinned: true,
            backgroundColor: theme.colorScheme.surface,
            surfaceTintColor: Colors.transparent,
            scrolledUnderElevation: 0,
            flexibleSpace: FlexibleSpaceBar(
              title: Text(l10n.get('premiumTitle')),
              background: Container(
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [
                      AppColors.primaryPurple,
                      AppColors.secondaryTeal,
                    ],
                  ),
                ),
                child: Center(
                  child: Icon(
                    Icons.workspace_premium,
                    size: 64,
                    color: Colors.white.withAlpha(100),
                  ),
                ),
              ),
            ),
          ),

          // Content
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Tagline
                  Text(
                    l10n.get('premiumTagline'),
                    style: theme.textTheme.headlineSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    l10n.get('premiumDescription'),
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 24),

                  // Features
                  Text(
                    l10n.get('premiumFeatures'),
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 12),
                  ..._features.map((feature) => _FeatureTile(
                    icon: feature['icon'] as IconData,
                    title: feature['title'] as String,
                    description: feature['description'] as String,
                  )),
                  const SizedBox(height: 24),

                  // Plans
                  Text(
                    l10n.get('chooseYourPlan'),
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 12),
                  ...List.generate(_plans.length, (index) {
                    final plan = _plans[index];
                    return _PlanCard(
                      name: plan['name'] as String,
                      price: plan['price'] as double,
                      period: plan['period'] as String,
                      savings: plan['savings'] as String?,
                      isSelected: _selectedPlanIndex == index,
                      onTap: () {
                        setState(() => _selectedPlanIndex = index);
                      },
                    );
                  }),
                  const SizedBox(height: 16),

                  // ── Transaction status banking card ──
                  if (_transactionResult != null)
                    TransactionStatusCard(
                      result: _transactionResult!,
                      onDismiss: () {
                        setState(() => _transactionResult = null);
                        _subscriptionService.clearError();
                      },
                      onRetry: _transactionResult != TransactionResult.success
                          ? _retryPurchase
                          : null,
                    ),

                  const SizedBox(height: 24),

                  // Subscribe button
                  SizedBox(
                    width: double.infinity,
                    child: FilledButton(
                      onPressed: _isLoading ? null : _subscribe,
                      style: FilledButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        backgroundColor: AppColors.primaryPurple,
                      ),
                      child: _isLoading
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Colors.white,
                              ),
                            )
                          : Text(
                              l10n.get('subscribeNow'),
                              style: TextStyle(fontSize: 16),
                            ),
                    ),
                  ),
                  const SizedBox(height: 12),

                  // Restore purchases
                  Center(
                    child: TextButton(
                      onPressed: () {
                        _subscriptionService.restorePurchases(
                          onSuccess: (isPremium) {
                            if (isPremium && mounted) {
                              setState(() {
                                _transactionResult = TransactionResult.success;
                              });
                            }
                          },
                          onError: (_) {},
                        );
                      },
                      child: Text(
                        l10n.get('restorePurchases'),
                        style: TextStyle(color: theme.colorScheme.primary),
                      ),
                    ),
                  ),
                  const SizedBox(height: 8),

                  // Legal text
                  Text(
                    l10n.get('subscriptionLegal'),
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.outline,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 16),

                  // Links
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      TextButton(
                        onPressed: () => launchUrl(Uri.parse('https://getneurocomet.com/terms')),
                        child: Text(l10n.termsOfService),
                      ),
                      const Text('•'),
                      TextButton(
                        onPressed: () => launchUrl(Uri.parse('https://getneurocomet.com/privacy')),
                        child: Text(l10n.privacyPolicy),
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),

                  // ── Debug-only: Payment test dialogs ──
                  if (kDebugMode) ...[
                    const Divider(),
                    const SizedBox(height: 12),
                    Text(
                      '🧪 Payment Test Dialogs',
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.tertiary,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Tap a button to preview each transaction status card.',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: _TestDialogButton(
                            label: '✅ Success',
                            color: const Color(0xFF2E7D32),
                            onTap: () => _showTestTransactionResult(
                                TransactionResult.success),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: _TestDialogButton(
                            label: '❌ Declined',
                            color: theme.colorScheme.error,
                            onTap: () => _showTestTransactionResult(
                                TransactionResult.declined),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: _TestDialogButton(
                            label: '⏱ Timed Out',
                            color: const Color(0xFFE65100),
                            onTap: () => _showTestTransactionResult(
                                TransactionResult.timedOut),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    SizedBox(
                      width: double.infinity,
                      child: OutlinedButton.icon(
                        onPressed: () {
                          setState(() => _transactionResult = null);
                          _subscriptionService.resetTestPurchase();
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Transaction state cleared'),
                              behavior: SnackBarBehavior.floating,
                            ),
                          );
                        },
                        icon: const Icon(Icons.refresh, size: 18),
                        label: const Text('Reset State'),
                      ),
                    ),
                    const SizedBox(height: 16),
                  ],
                  const SizedBox(height: 16),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _FeatureTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;

  const _FeatureTile({
    required this.icon,
    required this.title,
    required this.description,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: AppColors.primaryPurple.withAlpha(30),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              icon,
              color: AppColors.primaryPurple,
              size: 24,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: theme.textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  description,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          Icon(
            Icons.check_circle,
            color: AppColors.success,
            size: 20,
          ),
        ],
      ),
    );
  }
}

class _PlanCard extends StatelessWidget {
  final String name;
  final double price;
  final String period;
  final String? savings;
  final bool isSelected;
  final VoidCallback onTap;

  const _PlanCard({
    required this.name,
    required this.price,
    required this.period,
    this.savings,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isSelected ? AppColors.primaryPurple : theme.dividerColor,
            width: isSelected ? 2 : 1,
          ),
          color: isSelected
              ? AppColors.primaryPurple.withAlpha(20)
              : theme.colorScheme.surface,
        ),
        child: Row(
          children: [
            // Radio
            Container(
              width: 24,
              height: 24,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(
                  color: isSelected ? AppColors.primaryPurple : theme.dividerColor,
                  width: 2,
                ),
              ),
              child: isSelected
                  ? Center(
                      child: Container(
                        width: 12,
                        height: 12,
                        decoration: const BoxDecoration(
                          shape: BoxShape.circle,
                          color: AppColors.primaryPurple,
                        ),
                      ),
                    )
                  : null,
            ),
            const SizedBox(width: 16),

            // Plan info
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        name,
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      if (savings != null) ...[
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 2,
                          ),
                          decoration: BoxDecoration(
                            color: AppColors.success.withAlpha(30),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Text(
                            savings!,
                            style: theme.textTheme.labelSmall?.copyWith(
                              color: AppColors.success,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ],
                    ],
                  ),
                  Text(
                    period == 'once'
                        ? 'One-time purchase'
                        : 'Billed ${period}ly',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),

            // Price
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  '\$${price.toStringAsFixed(2)}',
                  style: theme.textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: AppColors.primaryPurple,
                  ),
                ),
                if (period != 'once')
                  Text(
                    '/$period',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                   ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

/// Debug-only button used in the payment test panel.
class _TestDialogButton extends StatelessWidget {
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _TestDialogButton({
    required this.label,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return OutlinedButton(
      onPressed: onTap,
      style: OutlinedButton.styleFrom(
        side: BorderSide(color: color),
        foregroundColor: color,
        padding: const EdgeInsets.symmetric(vertical: 12),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
      ),
      child: Text(
        label,
        style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
        textAlign: TextAlign.center,
      ),
    );
  }
}
