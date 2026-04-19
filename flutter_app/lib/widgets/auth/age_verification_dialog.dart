import 'package:flutter/material.dart';
import '../../core/theme/m3e_design_system.dart';
import '../../models/dev_options.dart';

class AgeVerificationDialog extends StatefulWidget {
  final void Function(Audience?) onConfirm;
  final VoidCallback onDismiss;
  final VoidCallback? onSkip;

  const AgeVerificationDialog({
    super.key,
    required this.onConfirm,
    required this.onDismiss,
    this.onSkip,
  });

  @override
  State<AgeVerificationDialog> createState() => _AgeVerificationDialogState();
}

class _AgeVerificationDialogState extends State<AgeVerificationDialog> {
  int? _day;
  int? _month;
  int? _year;

  final List<String> _months = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  Audience? _calculateAudience() {
    if (_day == null || _month == null || _year == null) return null;

    final birthDate = DateTime(_year!, _month!, _day!);
    final now = DateTime.now();
    int age = now.year - birthDate.year;
    if (now.month < birthDate.month || (now.month == birthDate.month && now.day < birthDate.day)) {
      age--;
    }

    if (age < 13) return Audience.under13;
    if (age < 18) return Audience.teen;
    return Audience.adult;
  }

  @override
  Widget build(BuildContext context) {

    return AlertDialog(
      shape: RoundedRectangleBorder(borderRadius: M3EDesignSystem.shapeDialog),
      title: const Text('Verify your age', textAlign: TextAlign.center, style: TextStyle(fontWeight: FontWeight.bold)),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Text(
            'We use your date of birth to personalize your experience and keep you safe.',
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 14),
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              Expanded(
                flex: 2,
                child: DropdownButtonFormField<int>(
                  decoration: const InputDecoration(labelText: 'Month', border: OutlineInputBorder()),
                  items: List.generate(12, (i) => DropdownMenuItem(value: i + 1, child: Text(_months[i], style: const TextStyle(fontSize: 12)))),
                  onChanged: (v) => setState(() => _month = v),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                flex: 1,
                child: DropdownButtonFormField<int>(
                  decoration: const InputDecoration(labelText: 'Day', border: OutlineInputBorder()),
                  items: List.generate(31, (i) => DropdownMenuItem(value: i + 1, child: Text('${i + 1}'))),
                  onChanged: (v) => setState(() => _day = v),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                flex: 2,
                child: DropdownButtonFormField<int>(
                  decoration: const InputDecoration(labelText: 'Year', border: OutlineInputBorder()),
                  items: List.generate(100, (i) => DropdownMenuItem(value: DateTime.now().year - i, child: Text('${DateTime.now().year - i}'))),
                  onChanged: (v) => setState(() => _year = v),
                ),
              ),
            ],
          ),
        ],
      ),
      actions: [
        if (widget.onSkip != null)
          TextButton(onPressed: widget.onSkip, child: const Text('Skip (Dev)')),
        TextButton(onPressed: widget.onDismiss, child: const Text('Cancel')),
        FilledButton(
          onPressed: (_day != null && _month != null && _year != null)
              ? () => widget.onConfirm(_calculateAudience())
              : null,
          child: const Text('Confirm'),
        ),
      ],
    );
  }
}
