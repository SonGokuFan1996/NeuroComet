import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Phone number formatting utility.
/// Mirrors the Kotlin PhoneNumberFormatter.kt
///
/// Supports multiple format styles:
/// - US: (XXX) XXX-XXXX
/// - International: +X XXX XXX XXXX
/// - Simple: XXX-XXX-XXXX
/// - UK, German, French, Japanese, Australian, Indian, Brazilian, Turkish, Korean
///
/// Neurodivergent-friendly features:
/// - Clear visual grouping reduces cognitive load
/// - Predictable formatting pattern
/// - Numbers are easier to verify when formatted
enum PhoneFormat {
  us,
  international,
  simple,
  uk,
  german,
  french,
  japanese,
  australian,
  indian,
  brazilian,
  turkish,
  korean,
}

class PhoneNumberFormatter extends TextInputFormatter {
  final PhoneFormat format;

  PhoneNumberFormatter({this.format = PhoneFormat.us});

  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    final digits = newValue.text.replaceAll(RegExp(r'\D'), '');
    final formatted = _formatPhoneNumber(digits, format);

    return TextEditingValue(
      text: formatted,
      selection: TextSelection.collapsed(offset: formatted.length),
    );
  }

  /// Format a phone number string according to the given format
  static String formatNumber(String digits, PhoneFormat format) {
    final cleanDigits = digits.replaceAll(RegExp(r'\D'), '');
    return _formatPhoneNumber(cleanDigits, format);
  }

  static String _formatPhoneNumber(String digits, PhoneFormat format) {
    switch (format) {
      case PhoneFormat.us:
        return _formatUS(digits);
      case PhoneFormat.international:
        return _formatInternational(digits);
      case PhoneFormat.simple:
        return _formatSimple(digits);
      case PhoneFormat.uk:
        return _formatUK(digits);
      case PhoneFormat.german:
        return _formatGerman(digits);
      case PhoneFormat.french:
        return _formatFrench(digits);
      case PhoneFormat.japanese:
        return _formatJapanese(digits);
      case PhoneFormat.australian:
        return _formatAustralian(digits);
      case PhoneFormat.indian:
        return _formatIndian(digits);
      case PhoneFormat.brazilian:
        return _formatBrazilian(digits);
      case PhoneFormat.turkish:
        return _formatTurkish(digits);
      case PhoneFormat.korean:
        return _formatKorean(digits);
    }
  }

  static String _formatUS(String digits) {
    // Format: (XXX) XXX-XXXX
    final d = digits.length > 10 ? digits.substring(0, 10) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      switch (i) {
        case 0:
          buf.write('(${d[i]}');
          break;
        case 2:
          buf.write('${d[i]}) ');
          break;
        case 5:
          buf.write('${d[i]}-');
          break;
        default:
          buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatInternational(String digits) {
    // Format: +X XXX XXX XXXX
    final d = digits.length > 12 ? digits.substring(0, 12) : digits;
    final buf = StringBuffer();
    if (d.isNotEmpty) buf.write('+');
    for (int i = 0; i < d.length; i++) {
      if (i == 1 || i == 4 || i == 7) {
        buf.write(' ${d[i]}');
      } else {
        buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatSimple(String digits) {
    // Format: XXX-XXX-XXXX
    final d = digits.length > 10 ? digits.substring(0, 10) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      if (i == 3 || i == 6) {
        buf.write('-${d[i]}');
      } else {
        buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatUK(String digits) {
    // Format: XXXX XXX XXXX
    final d = digits.length > 11 ? digits.substring(0, 11) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      if (i == 4 || i == 7) {
        buf.write(' ${d[i]}');
      } else {
        buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatGerman(String digits) {
    // Format: XXXX XXXXXXX
    final d = digits.length > 11 ? digits.substring(0, 11) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      if (i == 4) {
        buf.write(' ${d[i]}');
      } else {
        buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatFrench(String digits) {
    // Format: XX XX XX XX XX
    final d = digits.length > 10 ? digits.substring(0, 10) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      if (i > 0 && i % 2 == 0) {
        buf.write(' ${d[i]}');
      } else {
        buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatJapanese(String digits) {
    // Format: XXX-XXXX-XXXX
    final d = digits.length > 11 ? digits.substring(0, 11) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      if (i == 3 || i == 7) {
        buf.write('-${d[i]}');
      } else {
        buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatAustralian(String digits) {
    // Format: XXXX XXX XXX
    final d = digits.length > 10 ? digits.substring(0, 10) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      if (i == 4 || i == 7) {
        buf.write(' ${d[i]}');
      } else {
        buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatIndian(String digits) {
    // Format: XXXXX XXXXX
    final d = digits.length > 10 ? digits.substring(0, 10) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      if (i == 5) {
        buf.write(' ${d[i]}');
      } else {
        buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatBrazilian(String digits) {
    // Format: (XX) XXXXX-XXXX
    final d = digits.length > 11 ? digits.substring(0, 11) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      switch (i) {
        case 0:
          buf.write('(${d[i]}');
          break;
        case 1:
          buf.write('${d[i]}) ');
          break;
        case 6:
          buf.write('${d[i]}-');
          break;
        default:
          buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatTurkish(String digits) {
    // Format: (XXX) XXX XX XX
    final d = digits.length > 10 ? digits.substring(0, 10) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      switch (i) {
        case 0:
          buf.write('(${d[i]}');
          break;
        case 2:
          buf.write('${d[i]}) ');
          break;
        case 5:
          buf.write('${d[i]} ');
          break;
        case 7:
          buf.write('${d[i]} ');
          break;
        default:
          buf.write(d[i]);
      }
    }
    return buf.toString();
  }

  static String _formatKorean(String digits) {
    // Format: XXX-XXXX-XXXX
    final d = digits.length > 11 ? digits.substring(0, 11) : digits;
    final buf = StringBuffer();
    for (int i = 0; i < d.length; i++) {
      if (i == 3 || i == 7) {
        buf.write('-${d[i]}');
      } else {
        buf.write(d[i]);
      }
    }
    return buf.toString();
  }
}

/// A phone number text field widget with built-in formatting
class PhoneNumberField extends StatelessWidget {
  final TextEditingController? controller;
  final PhoneFormat format;
  final String? labelText;
  final String? hintText;
  final ValueChanged<String>? onChanged;
  final FormFieldValidator<String>? validator;

  const PhoneNumberField({
    super.key,
    this.controller,
    this.format = PhoneFormat.us,
    this.labelText,
    this.hintText,
    this.onChanged,
    this.validator,
  });

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: controller,
      keyboardType: TextInputType.phone,
      inputFormatters: [
        FilteringTextInputFormatter.digitsOnly,
        PhoneNumberFormatter(format: format),
      ],
      decoration: InputDecoration(
        labelText: labelText ?? 'Phone Number',
        hintText: hintText ?? _getHint(format),
        prefixIcon: const Icon(Icons.phone),
      ),
      onChanged: onChanged,
      validator: validator,
    );
  }

  String _getHint(PhoneFormat format) {
    switch (format) {
      case PhoneFormat.us:
        return '(555) 123-4567';
      case PhoneFormat.international:
        return '+1 234 567 8901';
      case PhoneFormat.simple:
        return '555-123-4567';
      case PhoneFormat.uk:
        return '0123 456 7890';
      default:
        return 'Enter phone number';
    }
  }
}

