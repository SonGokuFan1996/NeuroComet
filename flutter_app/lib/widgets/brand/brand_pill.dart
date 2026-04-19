import 'package:flutter/material.dart';

class BrandPill extends StatelessWidget {
  final String text;
  final Color? containerColor;
  final Color? contentColor;

  const BrandPill({
    super.key,
    required this.text,
    this.containerColor,
    this.contentColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
      decoration: BoxDecoration(
        color: containerColor ?? theme.colorScheme.primaryContainer,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        text,
        style: theme.textTheme.labelLarge?.copyWith(
          fontWeight: FontWeight.bold,
          color: contentColor ?? theme.colorScheme.onPrimaryContainer,
        ),
      ),
    );
  }
}
