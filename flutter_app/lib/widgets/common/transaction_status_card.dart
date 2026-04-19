import 'package:flutter/material.dart';

/// Possible outcomes shown on the transaction banking card.
enum TransactionResult { success, declined, timedOut }

/// Data holder for the transaction card visuals.
class _TransactionCardData {
  final IconData icon;
  final Color iconTint;
  final Color containerColor;
  final Color borderColor;
  final String title;
  final String description;

  const _TransactionCardData({
    required this.icon,
    required this.iconTint,
    required this.containerColor,
    required this.borderColor,
    required this.title,
    required this.description,
  });
}

/// Shows the outcome of a purchase attempt as a styled card
/// with appropriate color, icon, and messaging.
///
/// Mirrors the Android `TransactionStatusCard` composable from
/// `SubscriptionScreen.kt`.
class TransactionStatusCard extends StatelessWidget {
  final TransactionResult result;
  final VoidCallback onDismiss;
  final VoidCallback? onRetry;

  const TransactionStatusCard({
    super.key,
    required this.result,
    required this.onDismiss,
    this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final data = _resolveData(theme);

    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: data.containerColor,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: data.borderColor),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              children: [
                // Icon circle
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: data.iconTint.withAlpha(50),
                  ),
                  child: Icon(data.icon, color: data.iconTint, size: 22),
                ),
                const SizedBox(width: 12),
                // Title + description
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        data.title,
                        style: theme.textTheme.titleSmall?.copyWith(
                          fontWeight: FontWeight.w600,
                          color: data.iconTint,
                        ),
                      ),
                      Text(
                        data.description,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            // Buttons row
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(
                  onPressed: onDismiss,
                  child: const Text('Dismiss'),
                ),
                if (onRetry != null) ...[
                  const SizedBox(width: 8),
                  FilledButton(
                    onPressed: onRetry,
                    style: FilledButton.styleFrom(
                      backgroundColor: data.iconTint,
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      padding: const EdgeInsets.symmetric(
                          horizontal: 16, vertical: 8),
                    ),
                    child: Text(
                      'Retry',
                      style: theme.textTheme.labelLarge?.copyWith(
                        fontWeight: FontWeight.w600,
                        color: Colors.white,
                      ),
                    ),
                  ),
                ],
              ],
            ),
          ],
        ),
      ),
    );
  }

  _TransactionCardData _resolveData(ThemeData theme) {
    switch (result) {
      case TransactionResult.success:
        return _TransactionCardData(
          icon: Icons.check_circle,
          iconTint: const Color(0xFF2E7D32),
          containerColor: const Color(0xFF2E7D32).withAlpha(20),
          borderColor: const Color(0xFF2E7D32).withAlpha(82),
          title: 'Payment Successful',
          description:
              'Your premium access has been activated. Enjoy NeuroComet!',
        );
      case TransactionResult.declined:
        return _TransactionCardData(
          icon: Icons.cancel,
          iconTint: theme.colorScheme.error,
          containerColor: theme.colorScheme.errorContainer.withAlpha(180),
          borderColor: theme.colorScheme.error.withAlpha(160),
          title: 'Payment Declined',
          description:
              'Your payment could not be processed. Please check your payment method and try again.',
        );
      case TransactionResult.timedOut:
        return _TransactionCardData(
          icon: Icons.schedule,
          iconTint: const Color(0xFFE65100),
          containerColor: const Color(0xFFFFE0B2),
          borderColor: const Color(0xFFE65100).withAlpha(160),
          title: 'Transaction Timed Out',
          description:
              'We didn\'t receive a response in time. Please check your connection and try again.',
        );
    }
  }
}

