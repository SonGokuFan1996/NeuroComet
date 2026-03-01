#!/usr/bin/env python3
"""Apply translations and print status."""
import sys
import os

# Add tools directory to path
sys.path.insert(0, os.path.dirname(__file__))

# Run apply_translations
print("=" * 60)
print("APPLYING TRANSLATIONS")
print("=" * 60)

try:
    import apply_translations
    print("\n✅ Translations applied successfully!\n")
except Exception as e:
    print(f"\n❌ Error: {e}\n")

# Run status check
print("=" * 60)
print("TRANSLATION STATUS")
print("=" * 60)

try:
    import batch_translate_api
    # Call status function if it exists
    if hasattr(batch_translate_api, 'print_status'):
        batch_translate_api.print_status()
except Exception as e:
    print(f"Status check error: {e}")

# Manual status check
import re

base_path = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')
en_file = os.path.join(base_path, 'values', 'strings.xml')

with open(en_file, 'r', encoding='utf-8') as f:
    en_content = f.read()

en_count = len(re.findall(r'<string name="([^"]+)"', en_content))
print(f"\nEnglish base: {en_count} strings")
print()

langs = [
    ('ko', 'Korean'), ('zh', 'Chinese'), ('ja', 'Japanese'),
    ('el', 'Greek'), ('it', 'Italian'), ('hu', 'Hungarian'),
    ('pl', 'Polish'), ('vi', 'Vietnamese'), ('ro', 'Romanian')
]

for code, name in langs:
    try:
        lang_file = os.path.join(base_path, f'values-{code}', 'strings.xml')
        with open(lang_file, 'r', encoding='utf-8') as f:
            lang_content = f.read()

        # Count non-English strings
        translated = 0
        for match in re.finditer(r'<string name="([^"]+)"[^>]*>([^<]+)</string>', lang_content):
            name_attr, value = match.groups()
            # Check if in English file with same value
            en_match = re.search(rf'<string name="{name_attr}"[^>]*>([^<]+)</string>', en_content)
            if en_match and en_match.group(1) != value:
                translated += 1

        pct = (translated / en_count) * 100 if en_count > 0 else 0
        bar = '█' * int(pct / 5) + '░' * (20 - int(pct / 5))
        status = '🟢' if pct >= 50 else '🔴'
        print(f"{status} {name:15} [{bar}] {pct:5.1f}% ({translated}/{en_count})")
    except Exception as e:
        print(f"❌ {name}: Error - {e}")

print()

