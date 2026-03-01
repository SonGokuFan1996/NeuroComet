#!/usr/bin/env python3
"""Find all missing translations."""
import os
import re

base_path = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')
en_file = os.path.join(base_path, 'values', 'strings.xml')

with open(en_file, 'r', encoding='utf-8') as f:
    en_content = f.read()

# Get all English string names and values
en_strings = {}
for match in re.finditer(r'<string name="([^"]+)"[^>]*>([^<]+)</string>', en_content):
    en_strings[match.group(1)] = match.group(2)

output_lines = []
output_lines.append(f'Total English strings: {len(en_strings)}')
output_lines.append('')

# Check each language
lang_dirs = [d for d in os.listdir(base_path) if d.startswith('values-') and os.path.isdir(os.path.join(base_path, d))]
lang_dirs.sort()

all_missing = {}

for lang_dir in lang_dirs:
    lang_file = os.path.join(base_path, lang_dir, 'strings.xml')
    if os.path.exists(lang_file):
        with open(lang_file, 'r', encoding='utf-8') as f:
            lang_content = f.read()

        # Get translated strings (where value differs from English)
        lang_strings = {}
        for match in re.finditer(r'<string name="([^"]+)"[^>]*>([^<]+)</string>', lang_content):
            lang_strings[match.group(1)] = match.group(2)

        # Count translated (different from English)
        translated = 0
        untranslated = []
        for name, en_val in en_strings.items():
            if name in lang_strings:
                if lang_strings[name] != en_val:
                    translated += 1
                else:
                    untranslated.append(name)
            else:
                untranslated.append(name)

        missing = len(en_strings) - translated
        pct = (translated / len(en_strings)) * 100 if len(en_strings) > 0 else 0

        all_missing[lang_dir] = untranslated

        status = 'OK' if pct >= 50 else 'NEED'
        bar = '#' * int(pct / 5) + '-' * (20 - int(pct / 5))
        output_lines.append(f'{status} {lang_dir:12} [{bar}] {pct:5.1f}% ({translated}/{len(en_strings)}) - {missing} need work')

output_lines.append('')

# Show sample missing strings for worst languages
worst = sorted(all_missing.items(), key=lambda x: len(x[1]), reverse=True)[:5]
output_lines.append("Sample missing strings from languages needing most work:")
for lang, missing in worst:
    output_lines.append(f"\n{lang}: {len(missing)} untranslated")
    for s in missing[:10]:
        output_lines.append(f"  - {s}")

# Write to file
output_file = os.path.join(os.path.dirname(__file__), 'trans_status.txt')
with open(output_file, 'w', encoding='utf-8') as f:
    f.write('\n'.join(output_lines))

print("Output written to trans_status.txt")

