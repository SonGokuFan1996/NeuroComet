#!/usr/bin/env python3
"""Find untranslated strings and add more translations."""
import re
import os

def get_untranslated_strings(lang_code):
    """Get strings that appear to be untranslated (still English)."""
    base_path = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')

    # Read English strings
    en_file = os.path.join(base_path, 'values', 'strings.xml')
    with open(en_file, 'r', encoding='utf-8') as f:
        en_content = f.read()

    # Read target language strings
    lang_file = os.path.join(base_path, f'values-{lang_code}', 'strings.xml')
    with open(lang_file, 'r', encoding='utf-8') as f:
        lang_content = f.read()

    # Extract English strings
    en_strings = {}
    for match in re.finditer(r'<string name="([^"]+)"[^>]*>([^<]+)</string>', en_content):
        name, value = match.groups()
        en_strings[name] = value

    # Extract target language strings
    lang_strings = {}
    for match in re.finditer(r'<string name="([^"]+)"[^>]*>([^<]+)</string>', lang_content):
        name, value = match.groups()
        lang_strings[name] = value

    # Find untranslated (same as English)
    untranslated = []
    for name, en_value in en_strings.items():
        if name in lang_strings:
            lang_value = lang_strings[name]
            # Check if it's still English (same as original)
            if lang_value == en_value:
                untranslated.append((name, en_value))

    return untranslated

if __name__ == '__main__':
    import sys
    lang = sys.argv[1] if len(sys.argv) > 1 else 'ko'
    untranslated = get_untranslated_strings(lang)
    print(f"\n{lang}: {len(untranslated)} untranslated strings")
    print("\nFirst 50 untranslated strings:")
    for name, value in untranslated[:50]:
        print(f"  {name}: {value[:60]}...")

