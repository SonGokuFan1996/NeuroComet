#!/usr/bin/env python3
"""Analyze translations status across all language files."""

import os
import sys
import re

# Add the translations directory to path
TOOLS_DIR = os.path.dirname(__file__)
RES_DIR = os.path.join(TOOLS_DIR, '..', 'app', 'src', 'main', 'res')
TRANSLATIONS_DIR = os.path.join(TOOLS_DIR, 'translations')


def count_translations_in_module(module_path):
    """Count translations defined in a Python module file."""
    with open(module_path, 'r', encoding='utf-8') as f:
        text = f.read()
    # Count lines that match translation entries
    matches = re.findall(r'^\s+"[^"]+"\s*:', text, re.MULTILINE)
    return len(matches)


def main():
    print("Translation Analysis Report")
    print("=" * 60)
    print()
    sys.stdout.flush()

    # Check for translation files in translations directory
    if os.path.isdir(TRANSLATIONS_DIR):
        translation_files = [f for f in os.listdir(TRANSLATIONS_DIR)
                            if f.endswith('_translations.py')]

        print(f"Found {len(translation_files)} translation modules in {TRANSLATIONS_DIR}")
        print("-" * 60)

        total_translations = 0
        for tf in sorted(translation_files):
            module_path = os.path.join(TRANSLATIONS_DIR, tf)
            count = count_translations_in_module(module_path)
            lang_code = tf.replace('_translations.py', '')
            print(f"  {lang_code.upper():4}: {count:5} translations ({tf})")
            total_translations += count

        print("-" * 60)
        print(f"Total translations defined: {total_translations}")
    else:
        print(f"⚠️  Translations directory not found: {TRANSLATIONS_DIR}")

    print()

    # Also run the translation status check
    print("Running translation status check...")
    print("=" * 60)

    # Import and run the status analyzer
    try:
        sys.path.insert(0, TOOLS_DIR)
        from translate_strings import analyze_translation_status
        analyze_translation_status()
    except ImportError as e:
        print(f"Could not import translate_strings: {e}")
        print("Run translate_strings.py directly for full status.")


if __name__ == '__main__':
    main()
