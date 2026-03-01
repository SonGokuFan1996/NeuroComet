#!/usr/bin/env python3
"""Generate complete translations for all languages."""

import os
import re
import sys

TOOLS_DIR = os.path.dirname(__file__)
PROJECT_DIR = os.path.join(TOOLS_DIR, '..')
RES_DIR = os.path.join(PROJECT_DIR, 'app', 'src', 'main', 'res')
TRANSLATIONS_DIR = os.path.join(TOOLS_DIR, 'translations')

# Non-translatable keys (emojis)
NON_TRANSLATABLE = {
    'emoji_thumbs_up', 'emoji_heart', 'emoji_smile', 'emoji_hands', 'emoji_more',
    'auth_email_placeholder'  # Keep as your@email.com
}

def get_english_strings():
    """Get all translatable strings from English."""
    strings_path = os.path.join(RES_DIR, 'values', 'strings.xml')
    with open(strings_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Extract string name and value pairs
    pattern = r'<string name="([^"]+)"[^>]*>([^<]+)</string>'
    matches = re.findall(pattern, content)

    result = {}
    for name, value in matches:
        if name not in NON_TRANSLATABLE:
            result[name] = value

    return result

def get_existing_translations(lang_code):
    """Get existing translations from Python module."""
    module_path = os.path.join(TRANSLATIONS_DIR, f'{lang_code}_translations.py')
    if not os.path.exists(module_path):
        return {}

    with open(module_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Extract translations
    translations = {}
    pattern = r'"([^"]+)"\s*:\s*"([^"]*)"'
    for match in re.finditer(pattern, content):
        translations[match.group(1)] = match.group(2)

    return translations

def find_missing_keys(lang_code, english_strings):
    """Find keys that need translation."""
    existing = get_existing_translations(lang_code)
    missing = []
    for key in english_strings:
        if key not in existing:
            missing.append(key)
    return missing

def main():
    english = get_english_strings()
    print(f"English has {len(english)} translatable strings")

    # Get all language files
    lang_files = [f.replace('_translations.py', '')
                  for f in os.listdir(TRANSLATIONS_DIR)
                  if f.endswith('_translations.py')]

    print(f"\nFound {len(lang_files)} languages")
    print("-" * 60)

    for lang in sorted(lang_files):
        existing = get_existing_translations(lang)
        missing = find_missing_keys(lang, english)
        pct = (len(existing) / len(english)) * 100 if english else 0
        print(f"{lang.upper():4}: {len(existing):5}/{len(english)} ({pct:5.1f}%) - {len(missing)} missing")

        if len(sys.argv) > 1 and sys.argv[1] == lang:
            print(f"\n  Missing keys for {lang.upper()}:")
            for key in sorted(missing)[:50]:
                print(f"    {key}: {english.get(key, '')[:60]}")
            if len(missing) > 50:
                print(f"    ... and {len(missing) - 50} more")

if __name__ == '__main__':
    main()

